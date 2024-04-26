package net.ornithemc.keratin.api.task;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;

public interface VersionJsonDownloader extends TaskAware {

	default void downloadVersionJson(String url, File output) throws Exception {
		if (!output.exists() || isRefreshDependencies()) {
			FileUtils.copyURLToFile(new URL(url), output);
		}
	}
}
