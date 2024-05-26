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

		if (details.client() && details.server() && !details.sharedMappings()) {
			int clientBuild = keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT);
			int serverBuild = keratin.getNestsBuild(minecraftVersion, GameSide.SERVER);

			if (clientBuild > 0 && serverBuild > 0) {
				workQueue.submit(MergeNests.class, parameters -> {
					parameters.getClient().set(files.getIntermediaryClientNests(minecraftVersion));
					parameters.getServer().set(files.getIntermediaryServerNests(minecraftVersion));
					parameters.getMerged().set(files.getIntermediaryMergedNests(minecraftVersion));
				});
			}
		}
	}

	private static void validateNamespace(String namespace) {
		if (!"intermediary".equals(namespace)) {
			throw new IllegalStateException("cannot merge Nests in the " + namespace + " namespace");
		}
	}
}
