package net.ornithemc.keratin.api.task.unpick;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.mapping.Mapper;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles.BuildFiles;
import net.ornithemc.keratin.files.OrnitheFiles;

public abstract class MapUnpickDefinitionsToIntermediaryTask extends MinecraftTask implements UnpickDefinitions {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFiles files = keratin.getFiles();

		BuildFiles buildFiles = files.getMappingsDevelopmentFiles().getBuildFiles();

		workQueue.submit(MapUnpickDefinitionsAction.class, parameters -> {
			parameters.getInput().set(buildFiles.getProcessedUnpickDefinitionsFile(minecraftVersion));
			parameters.getMappings().set(buildFiles.getProcessedMappingsFile(minecraftVersion));
			parameters.getSourceNamespace().set(Mapper.NAMED);
			parameters.getTargetNamespace().set(Mapper.INTERMEDIARY);
			parameters.getOutput().set(buildFiles.getProcessedIntermediaryUnpickDefinitionsFile(minecraftVersion));
		});
	}
}
