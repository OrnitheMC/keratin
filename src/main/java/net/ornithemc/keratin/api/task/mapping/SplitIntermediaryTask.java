package net.ornithemc.keratin.api.task.mapping;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class SplitIntermediaryTask extends MinecraftTask implements MappingSplitter {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (!details.sharedMappings()) {
			workQueue.submit(SplitMappingsAction.class, parameters -> {
				parameters.getMerged().set(files.getMergedIntermediaryMappings(minecraftVersion));
				if (details.client()) {
					parameters.getClient().set(files.getClientIntermediaryMappings(minecraftVersion));
				}
				if (details.server()) {
					parameters.getServer().set(files.getServerIntermediaryMappings(minecraftVersion));
				}
			});
		}
	}
}
