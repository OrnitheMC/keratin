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

public abstract class DownloadSparrowTask extends MinecraftTask implements DownloaderAndExtracter {

	@TaskAction
	public void run() throws Exception {
		String minecraftVersion = getMinecraftVersion().get();

		getProject().getLogger().lifecycle(":downloading Sparrow for Minecraft " + minecraftVersion);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (details.sharedMappings()) {
			File output = files.getMergedSparrowFile(minecraftVersion);

			if (output != null) {
				downloadAndExtract(
					Constants.sparrowUrl(
						minecraftVersion,
						GameSide.MERGED,
						keratin.getSparrowBuild(minecraftVersion, GameSide.MERGED)),
					"signatures/mappings.sigs",
					output
				);
			}
		} else {
			File client = files.getClientSparrowFile(minecraftVersion);
			File server = files.getServerSparrowFile(minecraftVersion);

			if (details.client() && client != null) {
				downloadAndExtract(
					Constants.sparrowUrl(
						minecraftVersion,
						GameSide.CLIENT,
						keratin.getSparrowBuild(minecraftVersion, GameSide.CLIENT)),
					"signatures/mappings.sigs",
					client
				);
			}
			if (details.server() && server != null) {
				downloadAndExtract(
					Constants.sparrowUrl(
						minecraftVersion,
						GameSide.SERVER,
						keratin.getSparrowBuild(minecraftVersion, GameSide.SERVER)),
					"signatures/mappings.sigs",
					server
				);
			}
		}
	}
}
