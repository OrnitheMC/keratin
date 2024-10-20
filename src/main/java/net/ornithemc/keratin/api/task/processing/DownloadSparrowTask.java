package net.ornithemc.keratin.api.task.processing;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import org.gradle.workers.WorkQueue;

import com.google.common.io.Files;

import net.ornithemc.keratin.Constants;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.DownloaderAndExtracter;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class DownloadSparrowTask extends MinecraftTask implements DownloaderAndExtracter {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) throws Exception {
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
					output,
					() -> {
						try {
							if (!details.server()) {
								Files.copy(output, files.getClientSparrowFile(minecraftVersion));
							}
							if (!details.client()) {
								Files.copy(output, files.getServerSparrowFile(minecraftVersion));
							}
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					}
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
						details.isPreBeta() ? GameSide.MERGED : GameSide.CLIENT,
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
						details.isPreBeta() ? GameSide.MERGED : GameSide.SERVER,
						serverBuild),
					pathInJar,
					output
				);
			}
		}
	}
}
