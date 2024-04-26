package net.ornithemc.keratin.api.task.minecraft;

import org.gradle.api.tasks.TaskAction;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class DownloadMinecraftJarsTask extends MinecraftTask implements MinecraftJarsDownloader {

	@TaskAction
	public void run() throws Exception {
		String minecraftVersion = getMinecraftVersion().get();

		getProject().getLogger().lifecycle(":downloading game jars for Minecraft " + minecraftVersion);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		downloadMinecraftJars(
			details,
			files.getClientJar(minecraftVersion),
			files.getServerJar(minecraftVersion)
		);
	}
}
