package net.ornithemc.keratin.api.task;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskAction;

public abstract class DownloadVersionsManifestTask extends KeratinTask {

	public abstract Property<String> getUrl();

	public abstract Property<File> getOutput();

	@TaskAction
	public void run() throws IOException {
		getProject().getLogger().lifecycle(":downloading versions manifest");

		URL url = new URL(getUrl().get());
		File file = getOutput().get();

		if (!file.exists() || isRefreshDependencies()) {
			FileUtils.copyURLToFile(url, file);
		}
	}
}
