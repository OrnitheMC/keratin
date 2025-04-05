package net.ornithemc.keratin.api.task.mapping;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.GlobalCache.MappingsCache;
import net.ornithemc.keratin.files.OrnitheFiles;

public abstract class SplitIntermediaryTask extends MinecraftTask implements MappingSplitter {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFiles files = keratin.getFiles();

		MappingsCache mappings = files.getGlobalCache().getMappingsCache();

		if (!minecraftVersion.hasSharedObfuscation() && minecraftVersion.hasSharedVersioning()) {
			workQueue.submit(SplitMappingsAction.class, parameters -> {
				parameters.getOverwrite().set(keratin.isCacheInvalid());
				parameters.getMerged().set(mappings.getMergedIntermediaryMappingsFile(minecraftVersion));
				if (minecraftVersion.hasClient()) {
					parameters.getClient().set(mappings.getClientIntermediaryMappingsFile(minecraftVersion));
				}
				if (minecraftVersion.hasServer()) {
					parameters.getServer().set(mappings.getServerIntermediaryMappingsFile(minecraftVersion));
				}
			});
		}
	}
}
