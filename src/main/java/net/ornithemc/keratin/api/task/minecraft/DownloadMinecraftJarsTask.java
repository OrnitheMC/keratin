package net.ornithemc.keratin.api.task.minecraft;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails.Downloads.Download;
import net.ornithemc.keratin.api.task.Downloader;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class DownloadMinecraftJarsTask extends MinecraftTask implements Downloader {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		if (minecraftVersion.hasClient()) {
			Download download = minecraftVersion.client().downloads().client();

			download(
				download.url(),
				download.sha1(),
				files.getClientJar(minecraftVersion)
			);
		}
		if (minecraftVersion.hasServer()) {
			Download download = minecraftVersion.server().downloads().server();

			download(
				download.url(),
				download.sha1(),
				files.getServerJar(minecraftVersion)
			);
		}
	}
}
