package net.ornithemc.keratin.api.task;

import java.io.File;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public abstract class CheckedDownloadTask extends KeratinTask implements Downloader {

	@Internal
	public abstract Property<String> getUrl();

	@Internal
	public abstract Property<String> getSha1();

	@OutputFile
	public abstract Property<File> getOutput();

	@TaskAction
	public void run() throws Exception {
		download(getUrl().get(), getSha1().get(), getOutput().get());
	}
}
