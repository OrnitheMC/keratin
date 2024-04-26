package net.ornithemc.keratin.api.task.build;

import java.io.File;
import java.io.IOException;

import org.gradle.api.tasks.TaskAction;

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

public abstract class BuildMappingsTask extends BuildTask {

	@TaskAction
	public void run() throws IOException {
		String minecraftVersion = getMinecraftVersion().get();

		getProject().getLogger().lifecycle(":building mappings for Minecraft " + minecraftVersion);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		File intermediaryFile = files.getMainIntermediaryMappings(minecraftVersion);
		File completedMappings = files.getCompletedNamedMappings(minecraftVersion);
		File namedV1File = files.getTinyV1NamedMappings(minecraftVersion);
		File namedV2File = files.getTinyV2NamedMappings(minecraftVersion);
		File mergedNamedV1File = files.getMergedTinyV1NamedMappings(minecraftVersion);
		File mergedNamedV2File = files.getMergedTinyV2NamedMappings(minecraftVersion);

		MemoryMappingTree mappings = new MemoryMappingTree();
		MappingReader.read(completedMappings.toPath(), mappings);

		try (MappingWriter writer = MappingWriter.create(namedV1File.toPath(), MappingFormat.TINY_FILE)) {
			mappings.accept(writer);
		}
		try (MappingWriter writer = MappingWriter.create(namedV2File.toPath(), MappingFormat.TINY_2_FILE)) {
			mappings.accept(writer);
		}

		if (details.sharedMappings()) {
			MappingVisitor visitor = new MappingSourceNsSwitch(mappings, "intermediary");
			MappingReader.read(intermediaryFile.toPath(), visitor);

			try (MappingWriter writer = MappingWriter.create(mergedNamedV1File.toPath(), MappingFormat.TINY_FILE)) {
				mappings.accept(new MappingSourceNsSwitch(new MappingDstNsReorder(writer, "intermediary", "named"), "official"));
			}
			try (MappingWriter writer = MappingWriter.create(mergedNamedV2File.toPath(), MappingFormat.TINY_2_FILE)) {
				mappings.accept(new MappingSourceNsSwitch(new MappingDstNsReorder(writer, "intermediary", "named"), "official"));
			}
		} else {
			MappingReader.read(intermediaryFile.toPath(), mappings);

			try (MappingWriter writer = MappingWriter.create(mergedNamedV1File.toPath(), MappingFormat.TINY_FILE)) {
				mappings.accept(new MappingDstNsReorder(writer, "clientOfficial", "serverOfficial", "named"));
			}
			try (MappingWriter writer = MappingWriter.create(mergedNamedV2File.toPath(), MappingFormat.TINY_2_FILE)) {
				mappings.accept(new MappingDstNsReorder(writer, "clientOfficial", "serverOfficial", "named"));
			}
		}
	}
}
