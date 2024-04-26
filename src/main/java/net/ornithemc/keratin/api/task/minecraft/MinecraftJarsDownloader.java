package net.ornithemc.keratin.api.task.minecraft;

import java.io.File;

import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.manifest.VersionDetails.Download;
import net.ornithemc.keratin.api.task.Downloader;

public interface MinecraftJarsDownloader extends Downloader {

	default void downloadMinecraftJars(VersionDetails details, File client, File server) throws Exception {
		String clientUrl = null;
		String clientSha1 = null;
		File clientOutput = null;
		String serverUrl = null;
		String serverSha1 = null;
		File serverOutput = null;

		if (details.client()) {
			Download download = details.downloads().get("client");

			clientUrl = download.url();
			clientSha1 = download.sha1();
			clientOutput = client;
		}
		if (details.server()) {
			Download download = details.downloads().get("server");

			serverUrl = download.url();
			serverSha1 = download.sha1();
			serverOutput = server;
		}

		downloadMinecraftJars(clientUrl, clientSha1, clientOutput, serverUrl, serverSha1, serverOutput);
	}

	default void downloadMinecraftJars(String clientUrl, String clientSha1, File clientOutput, String serverUrl, String serverSha1, File serverOutput) throws Exception {
		if (clientUrl != null) {
			download(clientUrl, clientSha1, clientOutput);
		}
		if (serverUrl != null) {
			download(serverUrl, serverSha1, serverOutput);
		}
	}
}
