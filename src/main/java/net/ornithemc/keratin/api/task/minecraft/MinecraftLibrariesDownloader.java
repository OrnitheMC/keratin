package net.ornithemc.keratin.api.task.minecraft;

import java.io.File;
import java.util.function.Consumer;

import net.ornithemc.keratin.api.manifest.VersionInfo;
import net.ornithemc.keratin.api.manifest.VersionInfo.Library;
import net.ornithemc.keratin.api.manifest.VersionInfo.Library.Downloads.Artifact;
import net.ornithemc.keratin.api.task.Downloader;

public interface MinecraftLibrariesDownloader extends Downloader {

	default void downloadMinecraftLibraries(File dir, VersionInfo info) throws Exception {
		downloadMinecraftLibraries(dir, info, library -> { });
	}

	default void downloadMinecraftLibraries(File dir, VersionInfo info, Consumer<File> post) throws Exception {
		for (Library library : info.libraries()) {
			Artifact artifact = library.downloads().artifact();

			if (artifact == null) {
				continue;
			}

			String url = artifact.url();
			String sha1 = artifact.sha1();
			File file = new File(dir, url.substring(url.lastIndexOf('/')));

			download(url, sha1, file);
			post.accept(file);
		}
	}
}
