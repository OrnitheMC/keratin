package net.ornithemc.keratin.api.task.setup;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.processing.Nester;

public abstract class MakeSetupJarsTask extends MinecraftTask implements Nester {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		if (minecraftVersion.hasSharedObfuscation()) {
			workQueue.submit(NestJar.class, parameters -> {
				parameters.getInputJar().set(files.getMergedJar(minecraftVersion));
				parameters.getOutputJar().set(files.getSetupMergedJar(minecraftVersion));
				parameters.getNestsFile().set(files.getMergedNests(minecraftVersion));
			});
		} else {
			if (minecraftVersion.hasClient()) {
				workQueue.submit(NestJar.class, parameters -> {
					parameters.getInputJar().set(files.getClientJar(minecraftVersion));
					parameters.getOutputJar().set(files.getSetupClientJar(minecraftVersion));
					parameters.getNestsFile().set(files.getClientNests(minecraftVersion));
				});
			}
			if (minecraftVersion.hasServer()) {
				workQueue.submit(NestJar.class, parameters -> {
					parameters.getInputJar().set(files.getServerJar(minecraftVersion));
					parameters.getOutputJar().set(files.getSetupServerJar(minecraftVersion));
					parameters.getNestsFile().set(files.getServerNests(minecraftVersion));
				});
			}
		}
	}
}
