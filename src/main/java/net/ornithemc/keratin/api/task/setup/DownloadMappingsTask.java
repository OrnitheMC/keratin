package net.ornithemc.keratin.api.task.setup;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.Constants;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.DownloaderAndExtracter;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class DownloadMappingsTask extends MinecraftTask implements DownloaderAndExtracter {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		if (minecraftVersion.hasSharedVersioning()) {
			downloadMappings(minecraftVersion.id());
		} else {
			if (minecraftVersion.hasClient()) {
				downloadMappings(minecraftVersion.client().id());
			}
			if (minecraftVersion.hasServer()) {
				downloadMappings(minecraftVersion.server().id());
			}
		}
	}

	private void downloadMappings(String minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		int intermediaryGen = keratin.getIntermediaryGen().get();
		int featherBuild = keratin.getFeatherBuild(minecraftVersion);

		if (featherBuild < 1) {
			throw new RuntimeException("no Feather gen" + intermediaryGen + " builds exist yet for " + minecraftVersion);
		}

		downloadAndExtract(
			Constants.featherGen2Url(minecraftVersion, intermediaryGen, featherBuild),
			"mappings/mappings.tiny",
			files.getFeatherMappings(minecraftVersion)
		);
	}
}
