package net.ornithemc.keratin.api.task.merging;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;

public abstract class MergeNestsTask extends MergeTask {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) {
		String namespace = getNamespace().get();

		validateNamespace(namespace);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (!details.client() && !details.server()) {
			throw new IllegalStateException("cannot merge Nests for Minecraft " + minecraftVersion + ": both client and server Nests must be available");
		}

		if (!details.sharedMappings()) {
			workQueue.submit(MergeNests.class, parameters -> {
				parameters.getClientBuild().set(keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT));
				parameters.getServerBuild().set(keratin.getNestsBuild(minecraftVersion, GameSide.SERVER));
				parameters.getClient().set(files.getIntermediaryClientNests(minecraftVersion));
				parameters.getServer().set(files.getIntermediaryServerNests(minecraftVersion));
				parameters.getMerged().set(files.getIntermediaryMergedNests(minecraftVersion));
			});
		}
	}

	private static void validateNamespace(String namespace) {
		if (!"intermediary".equals(namespace)) {
			throw new IllegalStateException("cannot merge Nests in the " + namespace + " namespace");
		}
	}
}
