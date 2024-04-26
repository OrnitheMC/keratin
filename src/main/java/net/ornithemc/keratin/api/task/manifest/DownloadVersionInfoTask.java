package net.ornithemc.keratin.api.task.manifest;

import org.gradle.api.tasks.TaskAction;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionsManifest;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class DownloadVersionInfoTask extends MinecraftTask implements VersionJsonDownloader {

	@TaskAction
	public void run() throws Exception {
		String minecraftVersion = getMinecraftVersion().get();

		getProject().getLogger().lifecycle(":downloading version info for Minecraft " + minecraftVersion);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionsManifest manifest = keratin.getVersionsManifest();

		VersionsManifest.Entry entry = manifest.findOrThrow(minecraftVersion);

		downloadVersionJson(
			entry.url(),
			files.getVersionInfo(minecraftVersion)
		);
	}
}
