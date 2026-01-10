package net.ornithemc.keratin.api.task.generation;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.processing.Exceptor;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles.BuildFiles;
import net.ornithemc.keratin.files.GlobalCache.GameJarsCache;
import net.ornithemc.keratin.files.KeratinFiles;

public abstract class MakeBaseExceptionsTask extends MinecraftTask implements Exceptor {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		GameJarsCache gameJars = files.getGlobalCache().getGameJarsCache();
		BuildFiles buildFiles = files.getExceptionsAndSignaturesDevelopmentFiles().getBuildFiles();

		if (minecraftVersion.canBeMergedAsObfuscated()) {
			extractExceptions(
				gameJars.getMergedJar(minecraftVersion),
				buildFiles.getBaseMergedExceptionsFile(minecraftVersion)
			);
		} else {
			if (minecraftVersion.hasClient()) {
				extractExceptions(
					gameJars.getClientJar(minecraftVersion),
					buildFiles.getBaseClientExceptionsFile(minecraftVersion)
				);
			}
			if (minecraftVersion.hasServer()) {
				extractExceptions(
					gameJars.getServerJar(minecraftVersion),
					buildFiles.getBaseServerExceptionsFile(minecraftVersion)
				);
			}
		}
	}
}
