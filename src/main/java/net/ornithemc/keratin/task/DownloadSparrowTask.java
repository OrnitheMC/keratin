package net.ornithemc.keratin.task;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskAction;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.DownloaderAndExtracter;
import net.ornithemc.keratin.api.task.KeratinTask;
import net.ornithemc.keratin.manifest.VersionDetails;

public abstract class DownloadSparrowTask extends KeratinTask implements DownloaderAndExtracter {

	public abstract Property<String> getClientUrl();

	public abstract Property<String> getServerUrl();

	public abstract Property<String> getMergedUrl();

	public abstract Property<String> getPathInJar();

	@TaskAction
	public void run() throws Exception {
		getProject().getLogger().lifecycle(":downloading Sparrow");

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails();

		if (details.sharedMappings()) {
			downloadAndExtract(
				getMergedUrl().get(),
				getPathInJar().get(),
				files.getMergedSparrowFile()
			);
		} else {
			if (details.client()) {
				downloadAndExtract(
					getClientUrl().get(),
					getPathInJar().get(),
					files.getClientSparrowFile()
				);
			}
			if (details.server()) {
				downloadAndExtract(
					getServerUrl().get(),
					getPathInJar().get(),
					files.getServerSparrowFile()
				);
			}
		}
	}
}
