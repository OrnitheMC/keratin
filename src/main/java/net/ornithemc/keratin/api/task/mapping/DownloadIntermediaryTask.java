package net.ornithemc.keratin.api.task.mapping;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.Constants;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.DownloaderAndExtracter;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class DownloadIntermediaryTask extends MinecraftTask implements DownloaderAndExtracter {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		int intermediaryGen = keratin.getIntermediaryGen().get();

		downloadAndExtract(
			Constants.calamusGen2Url(minecraftVersion, intermediaryGen),
			"mappings/mappings.tiny",
			files.getMergedIntermediaryMappings(minecraftVersion)
		);
	}
}
