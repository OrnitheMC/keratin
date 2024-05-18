package net.ornithemc.keratin.api.task.merging;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;

public abstract class MergeSparrowTask extends MergeTask {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) {
		String namespace = getNamespace().get();

		validateNamespace(namespace);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (!details.client() && !details.server()) {
			throw new IllegalStateException("cannot merge Sparrow for Minecraft " + minecraftVersion + ": both client and server files must be available");
		}

		if (!details.sharedMappings()) {
			if (!details.sharedMappings()) {
				workQueue.submit(MergeSparrow.class, parameters -> {
					parameters.getClientBuild().set(keratin.getSparrowBuild(minecraftVersion, GameSide.CLIENT));
					parameters.getServerBuild().set(keratin.getSparrowBuild(minecraftVersion, GameSide.SERVER));
					parameters.getClient().set(files.getIntermediaryClientNests(minecraftVersion));
					parameters.getServer().set(files.getIntermediaryServerNests(minecraftVersion));
					parameters.getMerged().set(files.getIntermediaryMergedNests(minecraftVersion));
				});
			}
		}
	}

	private static void validateNamespace(String namespace) {
		if (!"intermediary".equals(namespace)) {
			throw new IllegalStateException("cannot merge Sparrow in the " + namespace + " namespace");
		}
	}
}
