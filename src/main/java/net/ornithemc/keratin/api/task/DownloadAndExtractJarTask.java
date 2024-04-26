package net.ornithemc.keratin.api.task;

import java.io.File;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskAction;

public abstract class DownloadAndExtractJarTask extends KeratinTask implements DownloaderAndExtracter {

	public abstract Property<String> getUrl();

	public abstract Property<String> getPathInJar();

	public abstract Property<File> getOutput();

	@TaskAction
	public void run() throws Exception {
		downloadAndExtract(
			getUrl().get(),
			getPathInJar().get(),
			getOutput().get()
		);
	}
}
