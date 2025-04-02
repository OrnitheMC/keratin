package net.ornithemc.keratin.api.task.mapping;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class SplitIntermediaryTask extends MinecraftTask implements MappingSplitter {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		if (!minecraftVersion.hasSharedObfuscation() && minecraftVersion.hasSharedVersioning()) {
			workQueue.submit(SplitMappingsAction.class, parameters -> {
				parameters.getOverwrite().set(keratin.isCacheInvalid());
				parameters.getMerged().set(files.getMergedIntermediaryMappings(minecraftVersion));
				if (minecraftVersion.hasClient()) {
					parameters.getClient().set(files.getClientIntermediaryMappings(minecraftVersion));
				}
				if (minecraftVersion.hasServer()) {
					parameters.getServer().set(files.getServerIntermediaryMappings(minecraftVersion));
				}
			});
		}
	}
}
