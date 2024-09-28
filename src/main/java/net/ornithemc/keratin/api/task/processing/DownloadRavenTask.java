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

public abstract class DownloadRavenTask extends MinecraftTask implements DownloaderAndExtracter {

	private static final String PATH_IN_JAR = "exceptions/mappings.excs";

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		if (minecraftVersion.hasSharedObfuscation()) {
			int build = keratin.getRavenBuild(minecraftVersion, GameSide.MERGED);

			if (build > 0) {
				File output = minecraftVersion.hasClient() && minecraftVersion.hasServer()
					? files.getMergedRavenFile(minecraftVersion)
					: minecraftVersion.hasClient()
						? files.getClientRavenFile(minecraftVersion)
						: files.getServerRavenFile(minecraftVersion);

				downloadAndExtract(
					Constants.ravenUrl(
						minecraftVersion.id(),
						GameSide.MERGED,
						build),
					PATH_IN_JAR,
					output
				);
			}
		} else {
			if (minecraftVersion.hasClient()) {
				int build = keratin.getRavenBuild(minecraftVersion, GameSide.CLIENT);

				if (build > 0) {
					File output = files.getClientRavenFile(minecraftVersion);

					downloadAndExtract(
						Constants.ravenUrl(
							minecraftVersion.client().id(),
							minecraftVersion.hasSharedVersioning() ? GameSide.CLIENT : GameSide.MERGED,
							build),
						PATH_IN_JAR,
						output
					);
				}
			}
			if (minecraftVersion.hasServer()) {
				int build = keratin.getRavenBuild(minecraftVersion, GameSide.SERVER);

				if (build > 0) {
					File output = files.getServerRavenFile(minecraftVersion);

					downloadAndExtract(
						Constants.ravenUrl(
							minecraftVersion.server().id(),
							minecraftVersion.hasSharedVersioning() ? GameSide.SERVER : GameSide.MERGED,
							build),
						PATH_IN_JAR,
						output
					);
				}
			}
		}
	}
}
