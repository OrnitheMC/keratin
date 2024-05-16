package net.ornithemc.keratin.api.task.merging;

import java.io.IOException;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;

public abstract class MergeMinecraftJarsTask extends MergeTask {

	@Override
	public void run(String minecraftVersion) throws IOException {
		String namespace = getNamespace().get();

		validateNamespace(namespace);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (details.client() && details.server()) {
			boolean official = "official".equals(namespace);

			if (official == details.sharedMappings()) {
				getProject().getLogger().lifecycle(":merging " + namespace + " jars for Minecraft " + minecraftVersion);

				mergeJars(
					official ? files.getClientJar(minecraftVersion) : files.getIntermediaryClientJar(minecraftVersion),
					official ? files.getServerJar(minecraftVersion) : files.getIntermediaryServerJar(minecraftVersion),
					official ? files.getMergedJar(minecraftVersion) : files.getIntermediaryMergedJar(minecraftVersion)
				);
			}
		}
	}

	private static void validateNamespace(String namespace) {
		if (!"official".equals(namespace) && !"intermediary".equals(namespace)) {
			throw new IllegalStateException("cannot merge Minecraft jars in the " + namespace + " namespace");
		}
	}
}
