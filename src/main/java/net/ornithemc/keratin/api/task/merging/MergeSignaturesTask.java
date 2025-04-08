package net.ornithemc.keratin.api.task.merging;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.settings.BuildNumbers;
import net.ornithemc.keratin.api.task.mapping.Mapper;
import net.ornithemc.keratin.files.GlobalCache.SignaturesCache;
import net.ornithemc.keratin.files.OrnitheFiles;

public abstract class MergeSignaturesTask extends MergeTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		String namespace = getNamespace().get();

		validateNamespace(namespace);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFiles files = keratin.getFiles();

		SignaturesCache signatures = files.getGlobalCache().getSignaturesCache();

		if (!minecraftVersion.canBeMergedAsObfuscated()) {
			BuildNumbers builds = keratin.getSignaturesBuilds(minecraftVersion);

			if (builds.client() > 0 && builds.server() > 0) {
				workQueue.submit(MergeSignatures.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getClient().set(signatures.getIntermediaryClientSignaturesFile(minecraftVersion, builds));
					parameters.getServer().set(signatures.getIntermediaryServerSignaturesFile(minecraftVersion, builds));
					parameters.getMerged().set(signatures.getIntermediaryMergedSignaturesFile(minecraftVersion, builds));
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
