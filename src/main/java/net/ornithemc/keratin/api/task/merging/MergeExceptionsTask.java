package net.ornithemc.keratin.api.task.merging;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.settings.BuildNumbers;
import net.ornithemc.keratin.api.task.mapping.Mapper;
import net.ornithemc.keratin.files.GlobalCache.ExceptionsCache;
import net.ornithemc.keratin.files.OrnitheFiles;

public abstract class MergeExceptionsTask extends MergeTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		String namespace = getNamespace().get();

		validateNamespace(namespace);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFiles files = keratin.getFiles();

		ExceptionsCache exceptions = files.getGlobalCache().getExceptionsCache();

		if (!minecraftVersion.canBeMergedAsObfuscated()) {
			BuildNumbers builds = keratin.getExceptionsBuilds(minecraftVersion);

			if (builds.client() > 0 && builds.server() > 0) {
				workQueue.submit(MergeExceptions.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getClient().set(exceptions.getIntermediaryClientExceptionsFile(minecraftVersion, builds));
					parameters.getServer().set(exceptions.getIntermediaryServerExceptionsFile(minecraftVersion, builds));
					parameters.getMerged().set(exceptions.getIntermediaryMergedExceptionsFile(minecraftVersion, builds));
				});
			}
		}
	}

	private static void validateNamespace(String namespace) {
		if (!Mapper.INTERMEDIARY.equals(namespace)) {
			throw new IllegalStateException("cannot merge exceptions in the " + namespace + " namespace");
		}
	}
}
