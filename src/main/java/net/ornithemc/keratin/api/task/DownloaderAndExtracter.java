package net.ornithemc.keratin.api.task;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;

import com.google.common.io.Files;

public interface DownloaderAndExtracter extends Downloader {

	default void downloadAndExtract(String url, String pathInJar, File dst) throws Exception {
		Project project = getExtension().getProject();
		String nameInJar = pathInJar.substring(pathInJar.lastIndexOf('/') + 1);

		if (!dst.exists() || isRefreshDependencies()) {
			File tmpDir = project.file(".downloads");
			File tmpJar = new File(tmpDir, "tmp.jar");
			File tmpDst = new File(tmpDir, dst.getName());

			download(url, tmpDst);

			project.copy(spec -> {
				spec.from(project.zipTree(tmpJar), from -> {
					from.from(pathInJar);
					from.rename(nameInJar, dst.getName());
				});
				spec.into(tmpDir);
			});
			Files.copy(tmpDst, dst);

			FileUtils.forceDelete(tmpDir);
		}
	}
}
