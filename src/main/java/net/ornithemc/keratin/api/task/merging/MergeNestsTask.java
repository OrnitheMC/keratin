package net.ornithemc.keratin.api.task.merging;

import java.io.IOException;

import org.gradle.api.tasks.TaskAction;

import com.google.common.io.Files;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;

public abstract class MergeNestsTask extends MergeTask {

	@TaskAction
	public void run() throws IOException {
		String minecraftVersion = getMinecraftVersion().get();
		String namespace = getNamespace().get();

		validateNamespace(namespace);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (!details.client() && !details.server()) {
			throw new IllegalStateException("cannot merge Nests for Minecraft " + minecraftVersion + ": both client and server Nests must be available");
		}

		getProject().getLogger().lifecycle(":merging " + namespace + " Nests for Minecraft " + minecraftVersion);

		if (!details.sharedMappings()) {
			int clientBuild = keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT);
			int serverBuild = keratin.getNestsBuild(minecraftVersion, GameSide.SERVER);

			if (clientBuild > 0 && serverBuild > 0) {
				mergeNests(
					files.getIntermediaryClientNests(minecraftVersion),
					files.getIntermediaryServerNests(minecraftVersion),
					files.getIntermediaryMergedNests(minecraftVersion)
				);
			} else {
				if (clientBuild > 0) {
					Files.copy(
						files.getIntermediaryClientNests(minecraftVersion),
						files.getIntermediaryMergedNests(minecraftVersion)
					);
				}
				if (serverBuild > 0) {
					Files.copy(
						files.getIntermediaryServerNests(minecraftVersion),
						files.getIntermediaryMergedNests(minecraftVersion)
					);
				}
			}
		}
	}

	private static void validateNamespace(String namespace) {
		if (!"intermediary".equals(namespace)) {
			throw new IllegalStateException("cannot merge Nests in the " + namespace + " namespace");
		}
	}
}
