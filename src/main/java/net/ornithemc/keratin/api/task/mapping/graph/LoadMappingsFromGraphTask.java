package net.ornithemc.keratin.api.task.mapping.graph;

import java.io.File;
import java.io.IOException;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.mappingutils.io.Format;

public abstract class LoadMappingsFromGraphTask extends MinecraftTask implements MappingsGraph {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) throws IOException {
		getProject().getLogger().lifecycle(":loading mappings from the graph for Minecraft " + minecraftVersion);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		File graphDir = files.getMappingsDirectory();
		File output = files.getRunDirectory(minecraftVersion);

		loadMappings(minecraftVersion, graphDir, output, Format.ENIGMA_DIR, Validators.REMOVE_DUMMY_MAPPINGS);
	}
}
