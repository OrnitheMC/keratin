package net.ornithemc.keratin.api.task.unpick;

import java.io.File;
import java.util.Set;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.KeratinFiles;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles.BuildFiles;

public abstract class LoadUnpickDefinitionsTask extends MinecraftTask implements UnpickDefinitions {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		MappingsDevelopmentFiles mappings = files.getMappingsDevelopmentFiles();
		BuildFiles buildFiles = mappings.getBuildFiles();

		File unpickDir = mappings.getUnpickDirectory();
		Set<File> unpickDefinitions = collectUnpickDefinitions(keratin, minecraftVersion, unpickDir);

		workQueue.submit(CombineUnpickDefinitionsAction.class, parameters -> {
			parameters.getInputs().set(unpickDefinitions);
			parameters.getOutput().set(buildFiles.getProcessedUnpickDefinitionsFile(minecraftVersion));
		});
	}
}
