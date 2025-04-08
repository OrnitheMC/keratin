package net.ornithemc.keratin.api.task.minecraft;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.manifest.VersionDetails.Downloads.Download;
import net.ornithemc.keratin.api.task.Downloader;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.GlobalCache.GameJarsCache;
import net.ornithemc.keratin.files.OrnitheFiles;

public abstract class DownloadMinecraftJarsTask extends MinecraftTask implements Downloader {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFiles files = keratin.getFiles();

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
		if (minecraftVersion.hasServer()) {
			Download download = minecraftVersion.server().downloads().server();

			minecraftJarsChanged |= download(
				download.url(),
				download.sha1(),
				gameJars.getServerJar(minecraftVersion),
				false
			);
		}

		if (minecraftJarsChanged) {
			keratin.invalidateCache();
		}
	}
}
