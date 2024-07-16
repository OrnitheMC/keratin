package net.ornithemc.keratin.api.task.setup;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.Constants;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.DownloaderAndExtracter;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class DownloadMappingsTask extends MinecraftTask implements DownloaderAndExtracter {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		int intermediaryGen = keratin.getIntermediaryGen().get();
		// we want the latest build, not the next
		int featherBuild = keratin.getNextFeatherBuild(minecraftVersion) - 1;

		if (intermediaryGen == 1) {
			throw new RuntimeException("gen1 intermediary is not supported at this time");
		}
		if (featherBuild < 1) {
			throw new RuntimeException("no Feather gen" + intermediaryGen + " builds exist yet for " + minecraftVersion);
		}

		downloadAndExtract(
			Constants.calamusGen2Url(minecraftVersion, intermediaryGen),
			"mappings/mappings.tiny",
			files.getMergedIntermediaryMappings(minecraftVersion)
		);
		downloadAndExtract(
			Constants.featherGen2Url(minecraftVersion, intermediaryGen, featherBuild),
			"mappings/mappings.tiny",
			files.getFeatherMappings(minecraftVersion)
		);
	}
}
