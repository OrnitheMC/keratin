package net.ornithemc.keratin.api.task.processing;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskAction;

import net.ornithemc.keratin.Constants;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.DownloaderAndExtracter;
import net.ornithemc.keratin.api.task.KeratinTask;

public abstract class DownloadNestsTask extends KeratinTask implements DownloaderAndExtracter {

	public abstract Property<String> getMinecraftVersion();

	@TaskAction
	public void run() throws Exception {
		String minecraftVersion = getMinecraftVersion().get();

		getProject().getLogger().lifecycle(":downloading Nests for Minecraft version " + minecraftVersion);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (details.sharedMappings()) {
			downloadAndExtract(
				Constants.nestsUrl(
					minecraftVersion,
					GameSide.MERGED,
					keratin.getNestsBuild(minecraftVersion, GameSide.MERGED)),
				"nests/mappings.nest",
				files.getMergedNests(minecraftVersion)
			);
		} else {
			if (details.client()) {
				downloadAndExtract(
					Constants.nestsUrl(
						minecraftVersion,
						GameSide.CLIENT,
						keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT)),
					"nests/mappings.nest",
					files.getClientNests(minecraftVersion)
				);
			}
			if (details.server()) {
				downloadAndExtract(
					Constants.nestsUrl(
						minecraftVersion,
						GameSide.SERVER,
						keratin.getNestsBuild(minecraftVersion, GameSide.SERVER)),
					"nests/mappings.nest",
					files.getServerNests(minecraftVersion)
				);
			}
		}
	}
}
