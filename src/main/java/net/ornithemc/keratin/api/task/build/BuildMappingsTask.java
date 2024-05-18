package net.ornithemc.keratin.api.task.build;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.MappingWriter;
import net.fabricmc.mappingio.adapter.MappingDstNsReorder;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class BuildMappingsTask extends MinecraftTask {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		workQueue.submit(BuildMappings.class, parameters -> {
			parameters.getLegacyMerged().set(!details.sharedMappings() && details.client() && details.server());
			parameters.getIntermediaryMappings().set(files.getMainIntermediaryMappings(minecraftVersion));
			parameters.getCompletedMappings().set(files.getCompletedNamedMappings(minecraftVersion));
			parameters.getNamedV1Mappings().set(files.getTinyV1NamedMappings(minecraftVersion));
			parameters.getNamedV2Mappings().set(files.getTinyV2NamedMappings(minecraftVersion));
			parameters.getMergedNamedV1Mappings().set(files.getMergedTinyV1NamedMappings(minecraftVersion));
			parameters.getMergedNamedV2Mappings().set(files.getMergedTinyV2NamedMappings(minecraftVersion));
		});
	}

	public interface BuildParameters extends WorkParameters {

		Property<Boolean> getLegacyMerged();

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
			boolean legacyMerged = getParameters().getLegacyMerged().get();
			File intermediaryFile = getParameters().getIntermediaryMappings().get();
			File completedMappings = getParameters().getCompletedMappings().get();
			File namedV1File = getParameters().getNamedV1Mappings().get();
			File namedV2File = getParameters().getNamedV2Mappings().get();
			File mergedNamedV1File = getParameters().getMergedNamedV1Mappings().get();
			File mergedNamedV2File = getParameters().getMergedNamedV2Mappings().get();

			try {
				MemoryMappingTree mappings = new MemoryMappingTree();
				MappingReader.read(completedMappings.toPath(), mappings);

				try (MappingWriter writer = MappingWriter.create(namedV1File.toPath(), MappingFormat.TINY_FILE)) {
					mappings.accept(writer);
				}
				try (MappingWriter writer = MappingWriter.create(namedV2File.toPath(), MappingFormat.TINY_2_FILE)) {
					mappings.accept(writer);
				}

				if (legacyMerged) {
					MappingReader.read(intermediaryFile.toPath(), mappings);

					try (MappingWriter writer = MappingWriter.create(mergedNamedV1File.toPath(), MappingFormat.TINY_FILE)) {
						mappings.accept(new MappingDstNsReorder(writer, "clientOfficial", "serverOfficial", "named"));
					}
					try (MappingWriter writer = MappingWriter.create(mergedNamedV2File.toPath(), MappingFormat.TINY_2_FILE)) {
						mappings.accept(new MappingDstNsReorder(writer, "clientOfficial", "serverOfficial", "named"));
					}
				} else {
					MappingVisitor visitor = new MappingSourceNsSwitch(mappings, "intermediary");
					MappingReader.read(intermediaryFile.toPath(), visitor);

					try (MappingWriter writer = MappingWriter.create(mergedNamedV1File.toPath(), MappingFormat.TINY_FILE)) {
						mappings.accept(new MappingSourceNsSwitch(new MappingDstNsReorder(writer, "intermediary", "named"), "official"));
					}
					try (MappingWriter writer = MappingWriter.create(mergedNamedV2File.toPath(), MappingFormat.TINY_2_FILE)) {
						mappings.accept(new MappingSourceNsSwitch(new MappingDstNsReorder(writer, "intermediary", "named"), "official"));
					}
				}
			} catch (IOException e) {
				throw new UncheckedIOException("error while building mappings", e);
			}
		}
	}
}
