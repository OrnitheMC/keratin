package net.ornithemc.keratin.api.task.processing;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.processing.Processor.ProcessMappings;

public abstract class ProcessMappingsTask extends MinecraftTask implements Nester {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) {
		getProject().getLogger().lifecycle(":processing mappings for Minecraft " + minecraftVersion);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		workQueue.submit(ProcessMappings.class, parameters -> {
			parameters.getInputMappings().set(files.getMainIntermediaryMappings(minecraftVersion));
			parameters.getNestsFile().set(files.getMainIntermediaryNests(minecraftVersion));
			parameters.getNestedMappings().set(files.getMainNestedIntermediaryMappings(minecraftVersion));
			parameters.getOutputMappings().set(files.getMainProcessedIntermediaryMappings(minecraftVersion));
		});
	}
}
