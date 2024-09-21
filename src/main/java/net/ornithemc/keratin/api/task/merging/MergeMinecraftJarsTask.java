package net.ornithemc.keratin.api.task.merging;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;

public abstract class MergeMinecraftJarsTask extends MergeTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		String namespace = getNamespace().get();

		validateNamespace(namespace);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		if (minecraftVersion.canBeMerged()) {
			boolean official = "official".equals(namespace);

			if (official == minecraftVersion.hasSharedObfuscation()) {
				workQueue.submit(MergeJars.class, parameters -> {
					parameters.getClient().set(official ? files.getClientJar(minecraftVersion) : files.getIntermediaryClientJar(minecraftVersion));
					parameters.getServer().set(official ? files.getServerJar(minecraftVersion) : files.getIntermediaryServerJar(minecraftVersion));
					parameters.getMerged().set(official ? files.getMergedJar(minecraftVersion) : files.getIntermediaryMergedJar(minecraftVersion));
				});
			}
		}
	}

	private static void validateNamespace(String namespace) {
		if (!"official".equals(namespace) && !"intermediary".equals(namespace)) {
			throw new IllegalStateException("cannot merge Minecraft jars in the " + namespace + " namespace");
		}
	}
}
