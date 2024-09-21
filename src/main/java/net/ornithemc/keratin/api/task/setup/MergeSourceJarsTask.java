package net.ornithemc.keratin.api.task.setup;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.merging.Merger;

public abstract class MergeSourceJarsTask extends MinecraftTask implements Merger {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		if (!minecraftVersion.hasSharedObfuscation() && minecraftVersion.canBeMerged()) {
			workQueue.submit(MergeJars.class, parameters -> {
				parameters.getClient().set(files.getNamedSourceClientJar(minecraftVersion));
				parameters.getServer().set(files.getNamedSourceServerJar(minecraftVersion));
				parameters.getMerged().set(files.getNamedSourceMergedJar(minecraftVersion));
			});
		}
	}
}
