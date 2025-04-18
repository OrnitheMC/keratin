package net.ornithemc.keratin.api.task.merging;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.settings.BuildNumbers;
import net.ornithemc.keratin.api.task.mapping.Mapper;
import net.ornithemc.keratin.files.GlobalCache.NestsCache;
import net.ornithemc.keratin.files.KeratinFiles;

public abstract class MergeNestsTask extends MergeTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		String namespace = getNamespace().get();

		validateNamespace(namespace);

		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		NestsCache nests = files.getGlobalCache().getNestsCache();

		if (!minecraftVersion.canBeMergedAsObfuscated()) {
			BuildNumbers builds = keratin.getNestsBuilds(minecraftVersion);

			if (builds.client() > 0 && builds.server() > 0) {
				workQueue.submit(MergeNests.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getClient().set(nests.getIntermediaryClientNestsFile(minecraftVersion, builds));
					parameters.getServer().set(nests.getIntermediaryServerNestsFile(minecraftVersion, builds));
					parameters.getMerged().set(nests.getIntermediaryMergedNestsFile(minecraftVersion, builds));
				});
			}
		}
	}

	private static void validateNamespace(String namespace) {
		if (!Mapper.INTERMEDIARY.equals(namespace)) {
			throw new IllegalStateException("cannot merge nests in the " + namespace + " namespace");
		}
	}
}
