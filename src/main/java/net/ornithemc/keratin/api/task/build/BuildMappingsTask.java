package net.ornithemc.keratin.api.task.build;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingWriter;
import net.fabricmc.mappingio.adapter.MappingDstNsReorder;
import net.fabricmc.mappingio.adapter.MappingNsCompleter;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.mapping.Mapper;

public abstract class BuildMappingsTask extends MinecraftTask {

	@Internal
	public abstract Property<String> getClassNamePattern();

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		String classNamePattern = getClassNamePattern().getOrElse("*");

		if (minecraftVersion.hasSharedVersioning()) {
			workQueue.submit(BuildMappings.class, parameters -> {
				parameters.getMultipleOfficialNamespaces().set(!minecraftVersion.hasSharedObfuscation());
				parameters.getClassNamePattern().set(classNamePattern);
				parameters.getIntermediaryMappings().set(files.getMergedIntermediaryMappings(minecraftVersion));
				parameters.getCompletedMappings().set(files.getCompletedNamedMappings(minecraftVersion));
				parameters.getNamedV1Mappings().set(files.getTinyV1NamedMappings(minecraftVersion.id()));
				parameters.getNamedV2Mappings().set(files.getTinyV2NamedMappings(minecraftVersion.id()));
				parameters.getMergedNamedV1Mappings().set(files.getMergedTinyV1NamedMappings(minecraftVersion.id()));
				parameters.getMergedNamedV2Mappings().set(files.getMergedTinyV2NamedMappings(minecraftVersion.id()));
			});
		} else {
			if (minecraftVersion.hasClient()) {
				workQueue.submit(BuildMappings.class, parameters -> {
					parameters.getMultipleOfficialNamespaces().set(false);
					parameters.getClassNamePattern().set(classNamePattern);
					parameters.getIntermediaryMappings().set(files.getClientIntermediaryMappings(minecraftVersion));
					parameters.getCompletedMappings().set(files.getCompletedNamedMappings(minecraftVersion));
					parameters.getNamedV1Mappings().set(files.getTinyV1NamedMappings(minecraftVersion.client().id()));
					parameters.getNamedV2Mappings().set(files.getTinyV2NamedMappings(minecraftVersion.client().id()));
					parameters.getMergedNamedV1Mappings().set(files.getMergedTinyV1NamedMappings(minecraftVersion.client().id()));
					parameters.getMergedNamedV2Mappings().set(files.getMergedTinyV2NamedMappings(minecraftVersion.client().id()));
				});
			}
			if (minecraftVersion.hasServer()) {
				workQueue.submit(BuildMappings.class, parameters -> {
					parameters.getMultipleOfficialNamespaces().set(false);
					parameters.getClassNamePattern().set(classNamePattern);
					parameters.getIntermediaryMappings().set(files.getServerIntermediaryMappings(minecraftVersion));
					parameters.getCompletedMappings().set(files.getCompletedNamedMappings(minecraftVersion));
					parameters.getNamedV1Mappings().set(files.getTinyV1NamedMappings(minecraftVersion.server().id()));
					parameters.getNamedV2Mappings().set(files.getTinyV2NamedMappings(minecraftVersion.server().id()));
					parameters.getMergedNamedV1Mappings().set(files.getMergedTinyV1NamedMappings(minecraftVersion.server().id()));
					parameters.getMergedNamedV2Mappings().set(files.getMergedTinyV2NamedMappings(minecraftVersion.server().id()));
				});
			}
		}
	}

	public interface BuildParameters extends WorkParameters {

		Property<Boolean> getMultipleOfficialNamespaces();

		Property<String> getClassNamePattern();

		Property<File> getIntermediaryMappings();

		Property<File> getCompletedMappings();

		Property<File> getNamedV1Mappings();

		Property<File> getNamedV2Mappings();

		Property<File> getMergedNamedV1Mappings();

		Property<File> getMergedNamedV2Mappings();

	}

	public static abstract class BuildMappings implements WorkAction<BuildParameters> {

		@Override
		public void execute() {
			boolean multipleOfficialNamespaces = getParameters().getMultipleOfficialNamespaces().get();
			String classNamePattern = getParameters().getClassNamePattern().get();
			File intermediaryFile = getParameters().getIntermediaryMappings().get();
			File completedMappings = getParameters().getCompletedMappings().get();
			File namedV1File = getParameters().getNamedV1Mappings().get();
			File namedV2File = getParameters().getNamedV2Mappings().get();
			File mergedNamedV1File = getParameters().getMergedNamedV1Mappings().get();
			File mergedNamedV2File = getParameters().getMergedNamedV2Mappings().get();

			try {
				MemoryMappingTree mappings = new MemoryMappingTree();
				MappingReader.read(completedMappings.toPath(), new MappingClassNameFilter(mappings, classNamePattern));

				try (MappingWriter writer = MappingWriter.create(namedV1File.toPath(), MappingFormat.TINY_FILE)) {
					mappings.accept(writer);
				}
				try (MappingWriter writer = MappingWriter.create(namedV2File.toPath(), MappingFormat.TINY_2_FILE)) {
					mappings.accept(writer);
				}

				if (multipleOfficialNamespaces) {
					MappingReader.read(intermediaryFile.toPath(), mappings);

					try (MappingWriter writer = MappingWriter.create(mergedNamedV1File.toPath(), MappingFormat.TINY_FILE)) {
						mappings.accept(new MappingDstNsReorder(new MappingNsCompleter(writer, Map.of(Mapper.NAMED, Mapper.INTERMEDIARY)), Mapper.CLIENT_OFFICIAL, Mapper.SERVER_OFFICIAL, Mapper.NAMED));
					}
					try (MappingWriter writer = MappingWriter.create(mergedNamedV2File.toPath(), MappingFormat.TINY_2_FILE)) {
						mappings.accept(new MappingDstNsReorder(new MappingNsCompleter(writer, Map.of(Mapper.NAMED, Mapper.INTERMEDIARY)), Mapper.CLIENT_OFFICIAL, Mapper.SERVER_OFFICIAL, Mapper.NAMED));
					}
				} else {
					MappingReader.read(intermediaryFile.toPath(), new MappingSourceNsSwitch(mappings, Mapper.INTERMEDIARY));

					try (MappingWriter writer = MappingWriter.create(mergedNamedV1File.toPath(), MappingFormat.TINY_FILE)) {
						mappings.accept(new MappingSourceNsSwitch(new MappingDstNsReorder(new MappingNsCompleter(writer, Map.of(Mapper.NAMED, Mapper.INTERMEDIARY)), Mapper.INTERMEDIARY, Mapper.NAMED), Mapper.OFFICIAL, true));
					}
					try (MappingWriter writer = MappingWriter.create(mergedNamedV2File.toPath(), MappingFormat.TINY_2_FILE)) {
						mappings.accept(new MappingSourceNsSwitch(new MappingDstNsReorder(new MappingNsCompleter(writer, Map.of(Mapper.NAMED, Mapper.INTERMEDIARY)), Mapper.INTERMEDIARY, Mapper.NAMED), Mapper.OFFICIAL, true));
					}
				}
			} catch (IOException e) {
				throw new UncheckedIOException("error while building mappings", e);
			}
		}
	}
}
