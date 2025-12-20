package net.ornithemc.keratin.api.task.merging;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.GlobalCache.MappingsCache;
import net.ornithemc.keratin.files.KeratinFiles;

public abstract class MergeIntermediaryTask extends MinecraftTask implements Merger {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		MappingsCache mappings = files.getGlobalCache().getMappingsCache();

		if (!minecraftVersion.hasSharedVersioning() && minecraftVersion.canBeMergedAsMapped()) {
			workQueue.submit(MergeIntermediary.class, parameters -> {
				parameters.getOverwrite().set(keratin.isCacheInvalid());
				parameters.getClient().set(mappings.getClientIntermediaryMappingsFile(minecraftVersion));
				parameters.getServer().set(mappings.getServerIntermediaryMappingsFile(minecraftVersion));
				parameters.getMerged().set(mappings.getMergedIntermediaryMappingsFile(minecraftVersion));
			});
		}
	}
}
