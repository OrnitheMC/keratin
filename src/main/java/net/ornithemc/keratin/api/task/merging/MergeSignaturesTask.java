package net.ornithemc.keratin.api.task.merging;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.mapping.Mapper;

public abstract class MergeSignaturesTask extends MergeTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		String namespace = getNamespace().get();

		validateNamespace(namespace);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		if (!minecraftVersion.canBeMergedAsObfuscated()) {
			int clientBuild = keratin.getSignaturesBuild(minecraftVersion, GameSide.CLIENT);
			int serverBuild = keratin.getSignaturesBuild(minecraftVersion, GameSide.SERVER);

			if (clientBuild > 0 && serverBuild > 0) {
				workQueue.submit(MergeSignatures.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getClient().set(files.getIntermediaryClientSignaturesFile(minecraftVersion));
					parameters.getServer().set(files.getIntermediaryServerSignaturesFile(minecraftVersion));
					parameters.getMerged().set(files.getIntermediaryMergedSignaturesFile(minecraftVersion));
				});
			}
		}
	}

	private static void validateNamespace(String namespace) {
		if (!Mapper.INTERMEDIARY.equals(namespace)) {
			throw new IllegalStateException("cannot merge signatures in the " + namespace + " namespace");
		}
	}
}
