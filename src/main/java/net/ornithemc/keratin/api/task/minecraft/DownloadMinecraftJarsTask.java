package net.ornithemc.keratin.api.task.minecraft;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.manifest.VersionDetails.Downloads.Download;
import net.ornithemc.keratin.api.task.DownloaderAndExtractor;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.GlobalCache.GameJarsCache;
import net.ornithemc.keratin.files.KeratinFiles;

public abstract class DownloadMinecraftJarsTask extends MinecraftTask implements DownloaderAndExtractor {

	private static final String SERVER_PATH_IN_ZIP = "minecraft-server.jar";

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		GameJarsCache gameJars = files.getGlobalCache().getGameJarsCache();

		boolean minecraftJarsChanged = false;

		if (minecraftVersion.hasClient()) {
			Download download = minecraftVersion.client().downloads().client();

			minecraftJarsChanged |= download(
				download.url(),
				download.sha1(),
				gameJars.getClientJar(minecraftVersion),
				false
			);
		}
		if (minecraftVersion.hasServerJar()) {
			Download download = minecraftVersion.server().downloads().server();

			minecraftJarsChanged |= download(
				download.url(),
				download.sha1(),
				gameJars.getServerJarWithLibraries(minecraftVersion),
				false
			);
		}
		if (minecraftVersion.hasServerZip()) {
			Download download = minecraftVersion.server().downloads().server_zip();

			minecraftJarsChanged |= downloadAndExtract(
				download.url(),
				download.sha1(),
				SERVER_PATH_IN_ZIP,
				gameJars.getServerZipWithLibraries(minecraftVersion),
				gameJars.getServerJarWithLibraries(minecraftVersion),
				false
			);
		}

		if (minecraftJarsChanged) {
			keratin.invalidateCache();
		}
	}
}
