package net.ornithemc.keratin.api.task;

import java.io.File;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskAction;

public abstract class CheckedDownloadTask extends KeratinTask implements Downloader {

	public abstract Property<String> getUrl();

	public abstract Property<String> getSha1();

	public abstract Property<File> getOutput();

	@TaskAction
	public void run() throws Exception {
		download(getUrl().get(), getSha1().get(), getOutput().get());
	}
}
