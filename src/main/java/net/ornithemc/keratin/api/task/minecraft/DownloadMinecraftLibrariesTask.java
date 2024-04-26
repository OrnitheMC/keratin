package net.ornithemc.keratin.api.task.minecraft;

import java.io.File;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskAction;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.OrnitheFiles;
import net.ornithemc.keratin.api.manifest.VersionInfo;
import net.ornithemc.keratin.api.task.KeratinTask;

public abstract class DownloadMinecraftLibrariesTask extends KeratinTask implements MinecraftLibrariesDownloader {

	public abstract Property<String> getMinecraftVersion();

	@TaskAction
	public void run() throws Exception {
		String minecraftVersion = getMinecraftVersion().get();

		getProject().getLogger().lifecycle(":downloading libraries for Minecraft version " + minecraftVersion);

		KeratinGradleExtension keratin = getExtension();
		VersionInfo info = keratin.getVersionInfo(minecraftVersion);
		OrnitheFiles files = keratin.getFiles();
		File dir = keratin.getFiles().getLibrariesCache();

		downloadMinecraftLibraries(
			dir,
			info,
			library -> files.addLibrary(minecraftVersion, library)
		);
	}
}
