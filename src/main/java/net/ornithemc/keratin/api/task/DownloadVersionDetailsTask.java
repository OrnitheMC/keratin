package net.ornithemc.keratin.api.task;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskAction;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.manifest.VersionsManifest;

public abstract class DownloadVersionDetailsTask extends KeratinTask implements VersionJsonDownloader {

	public abstract Property<String> getMinecraftVersion();

	@TaskAction
	public void run() throws Exception {
		String minecraftVersion = getMinecraftVersion().get();

		getProject().getLogger().lifecycle(":downloading version details for Minecraft version " + minecraftVersion);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionsManifest manifest = keratin.getVersionsManifest();

		VersionsManifest.Entry entry = manifest.findOrThrow(minecraftVersion);

		downloadVersionJson(
			entry.details(),
			files.getVersionDetails(minecraftVersion)
		);
	}
}
