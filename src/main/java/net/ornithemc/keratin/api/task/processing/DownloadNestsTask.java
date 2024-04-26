package net.ornithemc.keratin.api.task.processing;

import java.io.File;

import org.gradle.api.tasks.TaskAction;

import net.ornithemc.keratin.Constants;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.DownloaderAndExtracter;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class DownloadNestsTask extends MinecraftTask implements DownloaderAndExtracter {

	@TaskAction
	public void run() throws Exception {
		String minecraftVersion = getMinecraftVersion().get();

		getProject().getLogger().lifecycle(":downloading Nests for Minecraft " + minecraftVersion);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (details.sharedMappings()) {
			File output = files.getMergedNests(minecraftVersion);

			if (output != null) {
				downloadAndExtract(
					Constants.nestsUrl(
						minecraftVersion,
						GameSide.MERGED,
						keratin.getNestsBuild(minecraftVersion, GameSide.MERGED)),
					"nests/mappings.nest",
					output
				);
			}
		} else {
			File client = files.getClientNests(minecraftVersion);
			File server = files.getServerNests(minecraftVersion);

			if (details.client() && client != null) {
				downloadAndExtract(
					Constants.nestsUrl(
						minecraftVersion,
						GameSide.CLIENT,
						keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT)),
					"nests/mappings.nest",
					client
				);
			}
			if (details.server() && server != null) {
				downloadAndExtract(
					Constants.nestsUrl(
						minecraftVersion,
						GameSide.SERVER,
						keratin.getNestsBuild(minecraftVersion, GameSide.SERVER)),
					"nests/mappings.nest",
					server
				);
			}
		}
	}
}
