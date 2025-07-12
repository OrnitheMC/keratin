package net.ornithemc.keratin.api.task.unpick;

import java.io.File;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.KeratinFiles;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles.BuildFiles;

public abstract class LoadUnpickDefinitionsTask extends MinecraftTask implements UnpickDefinitions {

	@Override
	public void run() throws Exception {
		super.run();

		MinecraftVersion[] minecraftVersions = getMinecraftVersions().get().toArray(MinecraftVersion[]::new);
		File[] unpickFiles = new File[minecraftVersions.length];

		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		MappingsDevelopmentFiles mappings = files.getMappingsDevelopmentFiles();
		BuildFiles buildFiles = mappings.getBuildFiles();

		File unpickDir = mappings.getUnpickDirectory();

		for (int i = 0; i < minecraftVersions.length; i++) {
			unpickFiles[i] = buildFiles.getProcessedUnpickDefinitionsFile(minecraftVersions[i]);
		}

		collectUnpickDefinitions(keratin, minecraftVersions, unpickDir, unpickFiles);
	}

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
	}
}
