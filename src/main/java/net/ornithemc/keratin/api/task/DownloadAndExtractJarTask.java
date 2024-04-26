package net.ornithemc.keratin.api.task;

import java.io.File;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public abstract class DownloadAndExtractJarTask extends KeratinTask implements DownloaderAndExtracter {

	@Internal
	public abstract Property<String> getUrl();

	@Internal
	public abstract Property<String> getPathInJar();

	@OutputFile
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
