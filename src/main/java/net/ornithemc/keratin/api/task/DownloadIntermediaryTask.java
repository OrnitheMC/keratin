package net.ornithemc.keratin.api.task;

import java.io.File;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskAction;

import net.fabricmc.stitch.commands.CommandSplitTiny;

import net.ornithemc.keratin.Constants;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.manifest.VersionDetails;

public abstract class DownloadIntermediaryTask extends KeratinTask implements DownloaderAndExtracter {

	public abstract Property<String> getMinecraftVersion();

	public abstract Property<Integer> getIntermediaryGen();

	@TaskAction
	public void run() throws Exception {
		String minecraftVersion = getMinecraftVersion().get();
		int intermediaryGen = getIntermediaryGen().get();

		if (intermediaryGen == 1) {
			throw new IllegalStateException("gen1 intermediary is not supported at this time");
		}

		getProject().getLogger().lifecycle(":downloading intermediary gen" + intermediaryGen + " for Minecraft version " + minecraftVersion);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		downloadAndExtract(
			Constants.calamusGen2Url(minecraftVersion, intermediaryGen),
			"mappings/mappings.tiny",
			files.getMergedIntermediaryMappings(minecraftVersion)
		);

		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (!details.sharedMappings()) {
			File merged = keratin.getFiles().getMergedIntermediaryMappings(minecraftVersion);
			File client = keratin.getFiles().getClientIntermediaryMappings(minecraftVersion);
			File server = keratin.getFiles().getServerIntermediaryMappings(minecraftVersion);

			boolean genClient = (details.client() && (!client.exists() || isRefreshDependencies()));
			boolean genServer = (details.server() && (!server.exists() || isRefreshDependencies()));

			if (genClient || genServer) {
				new CommandSplitTiny().run(new String[] {
					merged.getAbsolutePath(),
					details.client() ? client.getAbsolutePath() : "-",
					details.server() ? server.getAbsolutePath() : "-"
				});
			}
		}
	}
}
