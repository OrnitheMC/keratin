package net.ornithemc.keratin.api.task.merging;

import java.io.IOException;

import com.google.common.io.Files;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;

public abstract class MergeSparrowTask extends MergeTask {

	@Override
	public void run(String minecraftVersion) throws IOException {
		String namespace = getNamespace().get();

		validateNamespace(namespace);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (!details.client() && !details.server()) {
			throw new IllegalStateException("cannot merge Sparrow for Minecraft " + minecraftVersion + ": both client and server files must be available");
		}

		if (!details.sharedMappings()) {
			int clientBuild = keratin.getSparrowBuild(minecraftVersion, GameSide.CLIENT);
			int serverBuild = keratin.getSparrowBuild(minecraftVersion, GameSide.SERVER);

			if (clientBuild > 0 && serverBuild > 0) {
				getProject().getLogger().lifecycle(":merging " + namespace + " Sparrow for Minecraft " + minecraftVersion);

				mergeSparrow(
					files.getIntermediaryClientSparrowFile(minecraftVersion),
					files.getIntermediaryServerSparrowFile(minecraftVersion),
					files.getIntermediaryMergedSparrowFile(minecraftVersion)
				);
			} else {
				if (clientBuild > 0) {
					Files.copy(
						files.getIntermediaryClientSparrowFile(minecraftVersion),
						files.getIntermediaryMergedSparrowFile(minecraftVersion)
					);
				}
				if (serverBuild > 0) {
					Files.copy(
						files.getIntermediaryServerSparrowFile(minecraftVersion),
						files.getIntermediaryMergedSparrowFile(minecraftVersion)
					);
				}
			}
		}
	}

	private static void validateNamespace(String namespace) {
		if (!"intermediary".equals(namespace)) {
			throw new IllegalStateException("cannot merge Sparrow in the " + namespace + " namespace");
		}
	}
}
