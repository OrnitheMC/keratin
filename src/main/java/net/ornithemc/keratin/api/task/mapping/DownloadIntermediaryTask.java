package net.ornithemc.keratin.api.task.mapping;

import java.io.File;

import org.gradle.workers.WorkQueue;

import net.fabricmc.stitch.commands.tinyv2.CommandSplitTinyV2;

import net.ornithemc.keratin.Constants;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.DownloaderAndExtracter;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class DownloadIntermediaryTask extends MinecraftTask implements DownloaderAndExtracter {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		int intermediaryGen = keratin.getIntermediaryGen().get();

		if (intermediaryGen == 1) {
			throw new IllegalStateException("gen1 intermediary is not supported at this time");
		}

		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		File output = files.getMergedIntermediaryMappings(minecraftVersion);
		Runnable after = null;

		if (!details.sharedMappings()) {
			File client = details.client() ? files.getClientIntermediaryMappings(minecraftVersion) : null;
			File server = details.server() ? files.getServerIntermediaryMappings(minecraftVersion) : null;

			boolean genClient = (client != null && (!client.exists() || isRefreshDependencies()));
			boolean genServer = (server != null && (!server.exists() || isRefreshDependencies()));

			if (genClient || genServer) {
				after = () -> {
					try {
						new CommandSplitTinyV2().run(new String[] {
							output.getAbsolutePath(),
							details.client() ? client.getAbsolutePath() : "-",
							details.server() ? server.getAbsolutePath() : "-"
						});
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				};
			}
		}

		downloadAndExtract(
			Constants.calamusGen2Url(minecraftVersion, intermediaryGen),
			"mappings/mappings.tiny",
			output,
			after
		);
	}
}
