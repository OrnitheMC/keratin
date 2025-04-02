package net.ornithemc.keratin.api.task.merging;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.mapping.Mapper;

public abstract class MergeExceptionsTask extends MergeTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		String namespace = getNamespace().get();

		validateNamespace(namespace);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		if (!minecraftVersion.canBeMergedAsObfuscated()) {
			int clientBuild = keratin.getExceptionsBuild(minecraftVersion, GameSide.CLIENT);
			int serverBuild = keratin.getExceptionsBuild(minecraftVersion, GameSide.SERVER);

			if (clientBuild > 0 && serverBuild > 0) {
				workQueue.submit(MergeExceptions.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getClient().set(files.getIntermediaryClientExceptionsFile(minecraftVersion));
					parameters.getServer().set(files.getIntermediaryServerExceptionsFile(minecraftVersion));
					parameters.getMerged().set(files.getIntermediaryMergedExceptionsFile(minecraftVersion));
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
