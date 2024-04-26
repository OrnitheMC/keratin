package net.ornithemc.keratin.api.task;

import java.io.File;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskAction;

public abstract class UnchheckedDownloadTask extends KeratinTask implements Downloader {

	public abstract Property<String> getUrl();

	public abstract Property<File> getOutput();

	@TaskAction
	public void run() throws Exception {
		download(getUrl().get(), getOutput().get());
	}
}
