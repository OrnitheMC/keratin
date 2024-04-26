package net.ornithemc.keratin.api.task.minecraft;

import java.io.File;

import org.gradle.api.tasks.TaskAction;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.OrnitheFiles;
import net.ornithemc.keratin.api.manifest.VersionInfo;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class DownloadMinecraftLibrariesTask extends MinecraftTask implements MinecraftLibrariesDownloader {

	@TaskAction
	public void run() throws Exception {
		String minecraftVersion = getMinecraftVersion().get();

		getProject().getLogger().lifecycle(":downloading libraries for Minecraft " + minecraftVersion);

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
