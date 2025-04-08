package net.ornithemc.keratin.api.task.setup;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.settings.BuildNumbers;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.processing.Nester;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles.SetupJars;
import net.ornithemc.keratin.files.GlobalCache.GameJarsCache;
import net.ornithemc.keratin.files.GlobalCache.NestsCache;
import net.ornithemc.keratin.files.OrnitheFiles;

public abstract class MakeSetupJarsTask extends MinecraftTask implements Nester {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFiles files = keratin.getFiles();

		GameJarsCache gameJars = files.getGlobalCache().getGameJarsCache();
		NestsCache nests = files.getGlobalCache().getNestsCache();
		SetupJars setupJars = files.getExceptionsAndSignaturesDevelopmentFiles().getSetupJars();

		BuildNumbers nestsBuilds = keratin.getNestsBuilds(minecraftVersion);

		if (minecraftVersion.hasSharedObfuscation()) {
			workQueue.submit(NestJar.class, parameters -> {
				parameters.getInputJar().set(gameJars.getMergedJar(minecraftVersion));
				parameters.getOutputJar().set(setupJars.getMergedJar(minecraftVersion));
				parameters.getNestsFile().set(nests.getMergedNestsFile(minecraftVersion, nestsBuilds));
			});
		} else {
			if (minecraftVersion.hasClient()) {
				workQueue.submit(NestJar.class, parameters -> {
					parameters.getInputJar().set(gameJars.getClientJar(minecraftVersion));
					parameters.getOutputJar().set(setupJars.getClientJar(minecraftVersion));
					parameters.getNestsFile().set(nests.getClientNestsFile(minecraftVersion, nestsBuilds));
				});
			}
			if (minecraftVersion.hasServer()) {
				workQueue.submit(NestJar.class, parameters -> {
					parameters.getInputJar().set(gameJars.getServerJar(minecraftVersion));
					parameters.getOutputJar().set(setupJars.getServerJar(minecraftVersion));
					parameters.getNestsFile().set(nests.getServerNestsFile(minecraftVersion, nestsBuilds));
				});
			}
		}
	}
}
