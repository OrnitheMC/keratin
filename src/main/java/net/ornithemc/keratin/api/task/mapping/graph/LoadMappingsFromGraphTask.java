package net.ornithemc.keratin.api.task.mapping.graph;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.enigma.EnigmaSession;
import net.ornithemc.keratin.files.KeratinFiles;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles;

import net.ornithemc.mappingutils.io.Format;

public abstract class LoadMappingsFromGraphTask extends MinecraftTask implements MappingsGraph {

	@Override
	public void run() throws Exception {
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		MappingsDevelopmentFiles mappings = files.getMappingsDevelopmentFiles();

		for (File f : mappings.getRunDirectory().listFiles()) {
			File enigmaSessionLock = new File(f, EnigmaSession.LOCK_FILE);

			if (!enigmaSessionLock.exists()) {
				FileUtils.forceDelete(f);
			}
		}

		super.run();
	}

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws IOException {
		getProject().getLogger().lifecycle(":loading mappings from the graph for Minecraft " + minecraftVersion.id());

		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		MappingsDevelopmentFiles mappings = files.getMappingsDevelopmentFiles();

		File graphDir = mappings.getMappingsDirectory();
		File output = mappings.getWorkingDirectory(minecraftVersion);

		loadMappings(minecraftVersion.id(), graphDir, output, Format.ENIGMA_DIR, Validators.removeDummyMappings(false));
	}
}
