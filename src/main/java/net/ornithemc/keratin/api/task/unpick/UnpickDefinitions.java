package net.ornithemc.keratin.api.task.unpick;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import org.gradle.api.provider.Property;
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
import net.ornithemc.keratin.util.Patterns;
import net.ornithemc.mappingutils.MappingUtils;

public interface UnpickDefinitions {

	String FILE_EXTENSION = ".unpick";

	default void collectUnpickDefinitions(KeratinGradleExtension keratin, MinecraftVersion[] minecraftVersions, File dir, File[] files) throws Exception {
		BufferedWriter[] writers = new BufferedWriter[minecraftVersions.length];

		try {
			for (int i = 0; i < minecraftVersions.length; i++) {
				writers[i] = new BufferedWriter(new FileWriter(files[i]));

				// headers from src files are skipped
				// (we don't want headers in the middle of the file)
				writers[i].write("v2");
				writers[i].newLine();
			}

			collectUnpickDefinitions(keratin, minecraftVersions, dir, writers);
		} finally {
			for (BufferedWriter writer : writers) {
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException ioe) {
						keratin.getProject().getLogger().warn("error closing file writer", ioe);
					}
				}
			}
		}
	}

	private static void collectUnpickDefinitions(KeratinGradleExtension keratin, MinecraftVersion[] minecraftVersions, File dir, BufferedWriter[] writers) throws Exception {
		for (File file : dir.listFiles()) {
			if (file.isFile() && file.getName().endsWith(FILE_EXTENSION)) {
				exportUnpickDefinitions(keratin, minecraftVersions, file, writers);
			}
			if (file.isDirectory()) {
				collectUnpickDefinitions(keratin, minecraftVersions, file, writers);
			}
		}
	}

	private static void exportUnpickDefinitions(KeratinGradleExtension keratin, MinecraftVersion[] minecraftVersions, File file, BufferedWriter[] writers) throws Exception {
		boolean[] acceptsLines = new boolean[minecraftVersions.length];
		Arrays.fill(acceptsLines, true);

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line = reader.readLine(); // skip header

			while ((line = reader.readLine()) != null) {
				if (line.isBlank()) {
					continue; // do not export empty lines
				}

				if (line.charAt(0) == '#') {
					// special comments define mc version ranges where
					// the lines below them are allowed to go
					if (line.startsWith("###")) {
						Arrays.fill(acceptsLines, true); // accept lines by default

						line = line.substring(3);
						line = line.trim();

						Matcher mcVersionRange = Patterns.OPTIONAL_MC_VERSION_RANGE.matcher(line);

						if (mcVersionRange.matches()) {
							String versionA = mcVersionRange.group(1);
							String versionB = mcVersionRange.group(2);

							MinecraftVersion minecraftVersionA = (versionA == null) ? null : keratin.getMinecraftVersion(versionA);
							MinecraftVersion minecraftVersionB = (versionB == null) ? null : keratin.getMinecraftVersion(versionB);

							for (int i = 0; i < minecraftVersions.length; i++) {
								MinecraftVersion minecraftVersion = minecraftVersions[i];

								// if neither of two versions has shared versioning
								// a common side must exist for comparison to be possible
								if (!minecraftVersion.hasSharedVersioning()) {
									if (minecraftVersionA != null && !minecraftVersionA.hasSharedVersioning() && !minecraftVersion.hasCommonSide(minecraftVersionA)) {
										acceptsLines[i] = false;
									}
									if (minecraftVersionB != null && !minecraftVersionB.hasSharedVersioning() && !minecraftVersion.hasCommonSide(minecraftVersionB)) {
										acceptsLines[i] = false;
									}
								}
		
								if (acceptsLines[i]) {
									// skip this check if one of the above already failed
									if ((minecraftVersionA != null && minecraftVersion.compareTo(minecraftVersionA) < 0)
										|| (minecraftVersionB != null && minecraftVersion.compareTo(minecraftVersionB) > 0)) {
										acceptsLines[i] = false; // mc version is not contained in the version range
									}
								}
							}
						} else {
							try {
								MinecraftVersion version = keratin.getMinecraftVersion(line);

								for (int i = 0; i < minecraftVersions.length; i++) {
									MinecraftVersion minecraftVersion = minecraftVersions[i];
									
									if (minecraftVersion.compareTo(version) != 0) {
										acceptsLines[i] = false;
									}
								}
							} catch (Exception e) {
								// ignore : this line may just be a comment?
							}
						}
					}

					continue; // do not export comments
				}

				for (int i = 0; i < minecraftVersions.length; i++) {
					if (acceptsLines[i]) {
						writers[i].write(line);
						writers[i].newLine();
					}
				}
			}
		} catch (Exception e) {
			throw new IOException("error exporting unpick definitions from " + file, e);
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
