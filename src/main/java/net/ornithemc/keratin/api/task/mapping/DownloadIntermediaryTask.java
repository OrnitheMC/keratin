package net.ornithemc.keratin.api.task.mapping;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.Constants;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.DownloaderAndExtracter;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class DownloadIntermediaryTask extends MinecraftTask implements DownloaderAndExtracter {

	private static final String PATH_IN_JAR = "mappings/mappings.tiny";

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		int intermediaryGen = keratin.getIntermediaryGen().get();

		if (minecraftVersion.hasSharedVersioning()) {
			downloadAndExtract(
				Constants.calamusGen2Url(minecraftVersion.id(), intermediaryGen),
				PATH_IN_JAR,
				files.getMergedIntermediaryMappings(minecraftVersion)
			);
		} else {
			if (minecraftVersion.hasClient()) {
				downloadAndExtract(
					Constants.calamusGen2Url(minecraftVersion.client().id(), intermediaryGen),
					PATH_IN_JAR,
					files.getClientIntermediaryMappings(minecraftVersion)
				);
			}
			if (minecraftVersion.hasServer()) {
				downloadAndExtract(
					Constants.calamusGen2Url(minecraftVersion.server().id(), intermediaryGen),
					PATH_IN_JAR,
					files.getServerIntermediaryMappings(minecraftVersion)
				);
			}
		}
	}
}
