package net.ornithemc.keratin.task;

import java.io.File;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskAction;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.task.Downloader;
import net.ornithemc.keratin.api.task.KeratinTask;
import net.ornithemc.keratin.manifest.VersionDetails;
import net.ornithemc.keratin.manifest.VersionDetails.Download;

public abstract class DownloadMinecraftJarsTask extends KeratinTask implements Downloader {

	public abstract Property<String> getMinecraftVersion();

	@TaskAction
	public void run() throws Exception {
		getProject().getLogger().lifecycle(":downloading Minecraft jars");

		KeratinGradleExtension keratin = getExtension();
		VersionDetails details = keratin.getVersionDetails();

		File client = keratin.getFiles().getClientJar();
		File server = keratin.getFiles().getServerJar();

		if (details.client()) {
			Download download = details.downloads().get("client");
			download(download.url(), download.sha1(), client);
		}
		if (details.server()) {
			Download download = details.downloads().get("server");
			download(download.url(), download.sha1(), server);
		}
	}
}
