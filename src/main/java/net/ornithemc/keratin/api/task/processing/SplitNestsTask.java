package net.ornithemc.keratin.api.task.processing;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.settings.BuildNumbers;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.GlobalCache.NestsCache;
import net.ornithemc.keratin.files.KeratinFiles;
import net.ornithemc.keratin.util.FileUtils;

public abstract class SplitNestsTask extends MinecraftTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		NestsCache nests = files.getGlobalCache().getNestsCache();

		if (minecraftVersion.hasSharedObfuscation() && !minecraftVersion.canBeMergedAsObfuscated()) {
			BuildNumbers builds = keratin.getNestsBuilds(minecraftVersion);

			if (builds.merged() > 0) {
				if (minecraftVersion.hasClient()) {
					FileUtils.copy(
						nests.getMergedNestsFile(minecraftVersion, builds),
						nests.getClientNestsFile(minecraftVersion, builds),
						keratin.isCacheInvalid()
					);
				}
				if (minecraftVersion.hasServer()) {
					FileUtils.copy(
						nests.getMergedNestsFile(minecraftVersion, builds),
						nests.getServerNestsFile(minecraftVersion, builds),
						keratin.isCacheInvalid()
					);
				}
			}
		}
	}
}
