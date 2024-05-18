package net.ornithemc.keratin.api.task.processing;

import java.io.File;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.Constants;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.DownloaderAndExtracter;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class DownloadNestsTask extends MinecraftTask implements DownloaderAndExtracter {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		String pathInJar = "nests/mappings.nest";

		if (details.sharedMappings()) {
			int build = keratin.getNestsBuild(minecraftVersion, GameSide.MERGED);

			if (build > 0) {
				File output = files.getMergedNests(minecraftVersion);

				downloadAndExtract(
					Constants.nestsUrl(
						minecraftVersion,
						GameSide.MERGED,
						build),
					pathInJar,
					output
				);
			}
		} else {
			int clientBuild = keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT);
			int serverBuild = keratin.getNestsBuild(minecraftVersion, GameSide.SERVER);

			if (details.client() && clientBuild > 0) {
				File output = files.getClientNests(minecraftVersion);

				downloadAndExtract(
					Constants.nestsUrl(
						minecraftVersion,
						GameSide.CLIENT,
						clientBuild),
					pathInJar,
					output
				);
			}
			if (details.server() && serverBuild > 0) {
				File output = files.getServerNests(minecraftVersion);

				downloadAndExtract(
					Constants.nestsUrl(
						minecraftVersion,
						GameSide.SERVER,
						serverBuild),
					pathInJar,
					output
				);
			}
		}
	}
}
