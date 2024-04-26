package net.ornithemc.keratin.api.task;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskAction;

import net.ornithemc.keratin.Constants;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.manifest.VersionDetails;

public abstract class DownloadSparrowTask extends KeratinTask implements DownloaderAndExtracter {

	public abstract Property<String> getMinecraftVersion();

	@TaskAction
	public void run() throws Exception {
		String minecraftVersion = getMinecraftVersion().get();

		getProject().getLogger().lifecycle(":downloading Sparrow for Minecraft version " + minecraftVersion);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (details.sharedMappings()) {
			downloadAndExtract(
				Constants.sparrowUrl(
					minecraftVersion,
					GameSide.MERGED,
					keratin.getSparrowBuild(minecraftVersion, GameSide.MERGED)),
				"signatures/mappings.sigs",
				files.getMergedSparrowFile(minecraftVersion)
			);
		} else {
			if (details.client()) {
				downloadAndExtract(
					Constants.sparrowUrl(
						minecraftVersion,
						GameSide.CLIENT,
						keratin.getSparrowBuild(minecraftVersion, GameSide.CLIENT)),
					"signatures/mappings.sigs",
					files.getClientSparrowFile(minecraftVersion)
				);
			}
			if (details.server()) {
				downloadAndExtract(
					Constants.sparrowUrl(
						minecraftVersion,
						GameSide.SERVER,
						keratin.getSparrowBuild(minecraftVersion, GameSide.SERVER)),
					"signatures/mappings.sigs",
					files.getServerSparrowFile(minecraftVersion)
				);
			}
		}
	}
}
