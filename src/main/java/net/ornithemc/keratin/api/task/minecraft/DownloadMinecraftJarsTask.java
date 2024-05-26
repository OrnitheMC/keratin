package net.ornithemc.keratin.api.task.minecraft;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.manifest.VersionDetails.Download;
import net.ornithemc.keratin.api.task.Downloader;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class DownloadMinecraftJarsTask extends MinecraftTask implements Downloader {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (details.client()) {
			Download download = details.downloads().get("client");

			download(
				download.url(),
				download.sha1(),
				files.getClientJar(minecraftVersion)
			);
		}
		if (details.server()) {
			Download download = details.downloads().get("server");

			download(
				download.url(),
				download.sha1(),
				files.getServerJar(minecraftVersion)
			);
		}
	}
}
