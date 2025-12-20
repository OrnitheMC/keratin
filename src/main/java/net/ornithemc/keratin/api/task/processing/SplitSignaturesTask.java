package net.ornithemc.keratin.api.task.processing;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.settings.BuildNumbers;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.GlobalCache.SignaturesCache;
import net.ornithemc.keratin.files.KeratinFiles;
import net.ornithemc.keratin.util.FileUtils;

public abstract class SplitSignaturesTask extends MinecraftTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		SignaturesCache signatures = files.getGlobalCache().getSignaturesCache();

		if (minecraftVersion.hasSharedObfuscation() && !minecraftVersion.canBeMergedAsObfuscated()) {
			BuildNumbers builds = keratin.getSignaturesBuilds(minecraftVersion);

			if (builds.merged() > 0) {
				if (minecraftVersion.hasClient()) {
					FileUtils.copy(
						signatures.getMergedSignaturesFile(minecraftVersion, builds),
						signatures.getClientSignaturesFile(minecraftVersion, builds),
						keratin.isCacheInvalid()
					);
				}
				if (minecraftVersion.hasServer()) {
					FileUtils.copy(
						signatures.getMergedSignaturesFile(minecraftVersion, builds),
						signatures.getServerSignaturesFile(minecraftVersion, builds),
						keratin.isCacheInvalid()
					);
				}
			}
		}
	}
}
