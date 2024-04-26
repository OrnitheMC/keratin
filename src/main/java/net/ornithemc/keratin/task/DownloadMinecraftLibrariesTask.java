package net.ornithemc.keratin.task;

import java.io.File;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskAction;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.OrnitheFiles;
import net.ornithemc.keratin.api.task.Downloader;
import net.ornithemc.keratin.api.task.KeratinTask;
import net.ornithemc.keratin.manifest.VersionInfo;
import net.ornithemc.keratin.manifest.VersionInfo.Library;
import net.ornithemc.keratin.manifest.VersionInfo.Library.Downloads.Artifact;

public abstract class DownloadMinecraftLibrariesTask extends KeratinTask implements Downloader {

	public abstract Property<String> getMinecraftVersion();

	@TaskAction
	public void run() throws Exception {
		getProject().getLogger().lifecycle(":downloading Minecraft libraries");

		KeratinGradleExtension keratin = getExtension();
		OrnitheFiles files = keratin.getFiles();
		VersionInfo info = keratin.getVersionInfo();

		File dir = keratin.getFiles().getLibrariesDir();

		for (Library library : info.libraries()) {
			Artifact artifact = library.downloads().artifact();

			if (artifact == null) {
				continue;
			}

			String url = artifact.url();
			String sha1 = artifact.sha1();
			File file = new File(dir, url.substring(url.lastIndexOf('/')));

			download(url, sha1, file);
			files.addLibrary(file);
		}
	}
}
