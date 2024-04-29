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

		String pathInJar = "signatures/mappings.sigs";

		if (details.sharedMappings()) {
			int build = keratin.getSparrowBuild(minecraftVersion, GameSide.MERGED);

			if (build > 0) {
				File output = files.getMergedSparrowFile(minecraftVersion);

				downloadAndExtract(
					Constants.sparrowUrl(
						minecraftVersion,
						GameSide.MERGED,
						build),
					pathInJar,
					output
				);
			}
		} else {
			int clientBuild = keratin.getSparrowBuild(minecraftVersion, GameSide.CLIENT);
			int serverBuild = keratin.getSparrowBuild(minecraftVersion, GameSide.SERVER);

			if (details.client() && clientBuild > 0) {
				File output = files.getClientSparrowFile(minecraftVersion);

				downloadAndExtract(
					Constants.sparrowUrl(
						minecraftVersion,
						GameSide.CLIENT,
						clientBuild),
					pathInJar,
					output
				);
			}
			if (details.server() && serverBuild > 0) {
				File output = files.getServerSparrowFile(minecraftVersion);

				downloadAndExtract(
					Constants.sparrowUrl(
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
