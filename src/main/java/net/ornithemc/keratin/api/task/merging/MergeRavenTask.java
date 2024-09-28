package net.ornithemc.keratin.api.task.merging;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.mapping.Mapper;

public abstract class MergeRavenTask extends MergeTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		String namespace = getNamespace().get();

		validateNamespace(namespace);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		if (!minecraftVersion.canBeMergedAsObfuscated()) {
			int clientBuild = keratin.getRavenBuild(minecraftVersion, GameSide.CLIENT);
			int serverBuild = keratin.getRavenBuild(minecraftVersion, GameSide.SERVER);

			if (clientBuild > 0 && serverBuild > 0) {
				workQueue.submit(MergeRaven.class, parameters -> {
					parameters.getClient().set(files.getIntermediaryClientRavenFile(minecraftVersion));
					parameters.getServer().set(files.getIntermediaryServerRavenFile(minecraftVersion));
					parameters.getMerged().set(files.getIntermediaryMergedRavenFile(minecraftVersion));
				});
			}
		}
	}

	private static void validateNamespace(String namespace) {
		if (!Mapper.INTERMEDIARY.equals(namespace)) {
			throw new IllegalStateException("cannot merge Raven in the " + namespace + " namespace");
		}
	}
}
