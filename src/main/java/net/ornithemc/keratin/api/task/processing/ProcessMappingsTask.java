package net.ornithemc.keratin.api.task.processing;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class ProcessMappingsTask extends MinecraftTask implements Processor {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		workQueue.submit(ProcessMappings.class, parameters -> {
			if ((details.client() && details.server()) || details.sharedMappings()) {
				parameters.getMergedInputMappings().set(files.getMergedIntermediaryMappings(minecraftVersion));
				parameters.getMergedNestsFile().set(files.getIntermediaryMergedNests(minecraftVersion));
				parameters.getMergedNestedMappings().set(files.getNestedMergedIntermediaryMappings(minecraftVersion));
				parameters.getMergedOutputMappings().set(files.getProcessedMergedIntermediaryMappings(minecraftVersion));
			}
			if (details.client()) {
				parameters.getClientInputMappings().set(files.getClientIntermediaryMappings(minecraftVersion));
				parameters.getClientNestsFile().set(files.getIntermediaryClientNests(minecraftVersion));
				parameters.getClientNestedMappings().set(files.getNestedClientIntermediaryMappings(minecraftVersion));
				parameters.getClientOutputMappings().set(files.getProcessedClientIntermediaryMappings(minecraftVersion));
			}
			if (details.server()) {
				parameters.getServerInputMappings().set(files.getServerIntermediaryMappings(minecraftVersion));
				parameters.getServerNestsFile().set(files.getIntermediaryServerNests(minecraftVersion));
				parameters.getServerNestedMappings().set(files.getNestedServerIntermediaryMappings(minecraftVersion));
				parameters.getServerOutputMappings().set(files.getProcessedServerIntermediaryMappings(minecraftVersion));
			}
		});
	}
}
