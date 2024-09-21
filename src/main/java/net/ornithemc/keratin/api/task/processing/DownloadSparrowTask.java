package net.ornithemc.keratin.api.task.processing;

import java.io.File;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.Constants;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.DownloaderAndExtracter;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class DownloadSparrowTask extends MinecraftTask implements DownloaderAndExtracter {

	private static final String PATH_IN_JAR = "signatures/mappings.sigs";

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		if (minecraftVersion.hasSharedObfuscation()) {
			int build = keratin.getSparrowBuild(minecraftVersion, GameSide.MERGED);

			if (build > 0) {
				File output = minecraftVersion.hasClient() && minecraftVersion.hasServer()
					? files.getMergedSparrowFile(minecraftVersion)
					: minecraftVersion.hasClient()
						? files.getClientSparrowFile(minecraftVersion)
						: files.getServerSparrowFile(minecraftVersion);

				downloadAndExtract(
					Constants.sparrowUrl(
						minecraftVersion,
						GameSide.MERGED,
						build),
					PATH_IN_JAR,
					output
				);
			}
		} else {
			if (minecraftVersion.hasClient()) {
				int clientBuild = keratin.getSparrowBuild(minecraftVersion, GameSide.CLIENT);

				if (clientBuild > 0) {
					File output = files.getClientSparrowFile(minecraftVersion);

					downloadAndExtract(
						Constants.sparrowUrl(
							minecraftVersion,
							GameSide.CLIENT,
							clientBuild),
						PATH_IN_JAR,
						output
					);
				}
			}
			if (minecraftVersion.hasServer()) {
				int serverBuild = keratin.getSparrowBuild(minecraftVersion, GameSide.SERVER);

				if (serverBuild > 0) {
					File output = files.getServerSparrowFile(minecraftVersion);

					downloadAndExtract(
						Constants.sparrowUrl(
							minecraftVersion,
							GameSide.SERVER,
							serverBuild),
						PATH_IN_JAR,
						output
					);
				}
			}
		}
	}
}
