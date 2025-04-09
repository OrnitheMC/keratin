package net.ornithemc.keratin.api.task.unpick;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;

import daomephsta.unpick.constantmappers.datadriven.parser.FieldKey;
import daomephsta.unpick.constantmappers.datadriven.parser.MethodKey;
import daomephsta.unpick.constantmappers.datadriven.parser.v2.UnpickV2Reader;
import daomephsta.unpick.constantmappers.datadriven.parser.v2.UnpickV2Remapper;
import daomephsta.unpick.constantmappers.datadriven.parser.v2.UnpickV2Writer;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;

import net.ornithemc.mappingutils.MappingUtils;

public interface UnpickDefinitions {

	String FILE_EXTENSION = ".unpick";

	default Set<File> collectUnpickDefinitions(KeratinGradleExtension keratin, MinecraftVersion minecraftVersion, File dir) {
		Set<File> unpickDefinitions = new HashSet<>();

		if (dir.isDirectory()) {
			System.out.println("collecting unpick definitions from " + dir.getName());
			collectUnpickDefinitions(keratin, minecraftVersion, dir, unpickDefinitions);
		}

		return unpickDefinitions;
	}

	default void collectUnpickDefinitions(KeratinGradleExtension keratin, MinecraftVersion minecraftVersion, File dir, Set<File> unpickDefinitions) {
		for (File file : dir.listFiles()) {
			if (file.isFile() && file.getName().endsWith(FILE_EXTENSION)) {
				System.out.println(" adding " + file.getName());
				unpickDefinitions.add(file);
			} else if (file.isFile()) {
				System.out.println(" ignoring " + file.getName());
			}
			if (file.isDirectory()) {
				String fileName = file.getName();
				String[] parts = fileName.split("mc");

				if (parts.length != 3) {
					System.out.println(" ignoring " + file.getName() + ": not formatted correctly: " + String.join(", ", parts));
					return; // directory does not hold unpick files or is formatted in an unknown way
				}

				String versionA = parts[1];
				String versionB = parts[2];

				if (versionA.charAt(versionA.length() - 1) == '-') {
					versionA = versionA.substring(0, versionA.length() - 1);
				}

				MinecraftVersion minecraftVersionA = keratin.getMinecraftVersion(versionA);
				MinecraftVersion minecraftVersionB = keratin.getMinecraftVersion(versionB);

				if (minecraftVersionA == null || minecraftVersionB == null) {
					System.out.println(" ignoring " + file.getName() + ": unknown mc version(s) " + versionA + "/" + versionB);
					return; // one of the mc versions is unknown
				}
				if (minecraftVersion.isBefore(minecraftVersionA) || minecraftVersion.isAfter(minecraftVersionB)) {
					System.out.println(" ignoring " + file.getName() + ": mc version " + minecraftVersion.id() + " not in range " + minecraftVersionA.id() + " - " + minecraftVersionB.id());
					return; // mc version is not contained in the version range covered by this dir
				}

				collectUnpickDefinitions(keratin, minecraftVersion, file, unpickDefinitions);
			}
		}
	}

	default void unnestUnpickDefinitions(File input, File output, File nests) throws IOException {
		_unnestUnpickDefinitions(input, output, nests);
	}

	static void _unnestUnpickDefinitions(File input, File output, File nests) throws IOException {
		Map<String, String> mappings = MappingUtils.buildUnnestingTranslations(nests.toPath());

		try (UnpickV2Reader reader = new UnpickV2Reader(new FileInputStream(input))) {
			UnpickV2Writer writer = new UnpickV2Writer();
			reader.accept(new UnpickV2Remapper(mappings, Collections.emptyMap(), Collections.emptyMap(), writer));
			Files.writeString(output.toPath(), writer.getOutput());
		}
	}

	interface CombineUnpickDefinitionsParameters extends WorkParameters {

		SetProperty<File> getInputs();

		Property<File> getOutput();

	}

	abstract class CombineUnpickDefinitionsAction implements WorkAction<CombineUnpickDefinitionsParameters> {

		@Override
		public void execute() {
			Set<File> inputs = getParameters().getInputs().get();
			File output = getParameters().getOutput().get();

			try {
				if (KeratinGradleExtension.validateOutput(output, true)) {
					return;
				}

				List<File> inputFiles = new ArrayList<>(inputs);
				inputFiles.sort(Comparator.comparing(File::getName));

				UnpickV2Writer writer = new UnpickV2Writer();

				for (File input : inputFiles) {
					if (!input.getName().endsWith(FILE_EXTENSION)) {
						continue;
					}

					try (UnpickV2Reader reader = new UnpickV2Reader(new FileInputStream(input))) {
						reader.accept(writer);
					}
				}

				Files.writeString(output.toPath(), writer.getOutput());
			} catch (IOException e) {
				throw new RuntimeException("error while combining unpick definitions", e);
			}
		}
	}

	interface MapUnpickDefinitionsParameters extends WorkParameters {

		Property<File> getInput();

		Property<File> getMappings();

		Property<String> getSourceNamespace();

		Property<String> getTargetNamespace();

		Property<File> getOutput();

	}

	abstract class MapUnpickDefinitionsAction implements WorkAction<MapUnpickDefinitionsParameters> {

		@Override
		public void execute() {
			File input = getParameters().getInput().get();
			File mappings = getParameters().getMappings().get();
			String srcNs = getParameters().getSourceNamespace().get();
			String dstNs = getParameters().getTargetNamespace().get();
			File output = getParameters().getOutput().get();

			try {
				if (KeratinGradleExtension.validateOutput(output, true)) {
					return;
				}

				Map<String, String> classMappings = new HashMap<>();
				Map<MethodKey, String> methodMappings = new HashMap<>();
				Map<FieldKey, String> fieldMappings = new HashMap<>();

				MemoryMappingTree mappingTree = new MemoryMappingTree();
				MappingReader.read(mappings.toPath(), mappingTree);

				for (MappingTree.ClassMapping cls : mappingTree.getClasses()) {
					String className = cls.getName(srcNs);

					if (className == null) {
						continue;
					}

					classMappings.put(className, cls.getName(dstNs));

					for (MappingTree.MethodMapping mtd : cls.getMethods()) {
						methodMappings.put(new MethodKey(className, mtd.getName(srcNs), mtd.getDesc(srcNs)), mtd.getName(dstNs));
					}

					for (MappingTree.FieldMapping fld : cls.getFields()) {
						fieldMappings.put(new FieldKey(className, fld.getName(srcNs)), fld.getName(dstNs));
					}
				}

				try (UnpickV2Reader reader = new UnpickV2Reader(new FileInputStream(input))) {
					UnpickV2Writer writer = new UnpickV2Writer();
					reader.accept(new UnpickV2Remapper(classMappings, methodMappings, fieldMappings, writer));
					Files.writeString(output.toPath(), writer.getOutput());
				}
			} catch (IOException e) {
				throw new RuntimeException("error while remapping unpick definitions", e);
			}
		}
	}
}
