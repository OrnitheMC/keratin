package net.ornithemc.keratin.api.task.processing;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.settings.BuildNumbers;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.GlobalCache.ExceptionsCache;
import net.ornithemc.keratin.files.KeratinFiles;
import net.ornithemc.keratin.util.FileUtils;

public abstract class SplitExceptionsTask extends MinecraftTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		ExceptionsCache exceptions = files.getGlobalCache().getExceptionsCache();

		if (minecraftVersion.hasSharedObfuscation() && !minecraftVersion.canBeMergedAsObfuscated()) {
			BuildNumbers builds = keratin.getExceptionsBuilds(minecraftVersion);

			if (builds.merged() > 0) {
				if (minecraftVersion.hasClient()) {
					FileUtils.copy(
						exceptions.getMergedExceptionsFile(minecraftVersion, builds),
						exceptions.getClientExceptionsFile(minecraftVersion, builds),
						keratin.isCacheInvalid()
					);
				}
				if (minecraftVersion.hasServer()) {
					FileUtils.copy(
						exceptions.getMergedExceptionsFile(minecraftVersion, builds),
						exceptions.getServerExceptionsFile(minecraftVersion, builds),
						keratin.isCacheInvalid()
					);
				}
			}
		}
	}
}
