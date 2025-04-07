package net.ornithemc.keratin.api.task.unpick;

import java.io.File;
import java.util.Set;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles.BuildFiles;
import net.ornithemc.keratin.files.OrnitheFiles;

public abstract class LoadUnpickDefinitionsTask extends MinecraftTask implements UnpickDefinitions {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFiles files = keratin.getFiles();

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
