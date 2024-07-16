package net.ornithemc.keratin.api.task.setup;

import java.io.File;
import java.io.IOException;

import org.gradle.workers.WorkQueue;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingWriter;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class CombineSetupMappingsTask extends MinecraftTask {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (details.sharedMappings()) {
			mergeMappings(
				files.getSetupMergedIntermediaryMappings(minecraftVersion),
				files.getSetupMergedNamedMappings(minecraftVersion),
				files.getCombinedSetupMergedMappings(minecraftVersion)
			);
		} else {
			if (details.client()) {
				mergeMappings(
					files.getSetupClientIntermediaryMappings(minecraftVersion),
					files.getSetupClientNamedMappings(minecraftVersion),
					files.getCombinedSetupClientMappings(minecraftVersion)
				);
			}
			if (details.server()) {
				mergeMappings(
					files.getSetupServerIntermediaryMappings(minecraftVersion),
					files.getSetupServerNamedMappings(minecraftVersion),
					files.getCombinedSetupServerMappings(minecraftVersion)
				);
			}
		}
	}

	private void mergeMappings(File intermediary, File named, File merged) throws IOException {
		MemoryMappingTree mappings = new MemoryMappingTree();

		MappingReader.read(intermediary.toPath(), new MappingSourceNsSwitch(mappings, "intermediary"));
		MappingReader.read(named.toPath(), mappings);

		MappingWriter writer = MappingWriter.create(merged.toPath(), MappingFormat.TINY_2_FILE);
		MappingSourceNsSwitch srcNsSwitch = new MappingSourceNsSwitch(writer, "official", true);

		mappings.accept(srcNsSwitch);
	}
}
