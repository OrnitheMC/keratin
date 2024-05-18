package net.ornithemc.keratin.api.task.minecraft;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class DownloadMinecraftJarsTask extends MinecraftTask implements MinecraftJarsDownloader {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) throws Exception {
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
