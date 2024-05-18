package net.ornithemc.keratin.api.task;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import org.apache.commons.io.FileUtils;

import org.gradle.api.Project;

import com.google.common.io.Files;

import net.ornithemc.keratin.KeratinGradleExtension;

public interface DownloaderAndExtracter extends Downloader {

	default void downloadAndExtract(String url, String pathInJar, File dst) throws Exception {
		downloadAndExtract(url, pathInJar, dst, null);
	}

	default void downloadAndExtract(String url, String pathInJar, File dst, Runnable after) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		Project project = keratin.getProject();
		String nameInJar = pathInJar.substring(pathInJar.lastIndexOf('/') + 1);

		if (!dst.exists() || isRefreshDependencies()) {
			File tmpDir = project.file(".downloads-" + Integer.toHexString(url.hashCode()));
			File tmpJar = new File(tmpDir, "tmp.jar");
			File tmpDst = new File(tmpDir, "tmp");

			if (tmpDir.exists()) {
				FileUtils.forceDelete(tmpDir);
			}
			tmpDir.mkdirs();

			download(url, tmpJar).thenRun(() -> {
				try {
					project.copy(spec -> {
						spec.from(project.zipTree(tmpJar), from -> {
							from.from(pathInJar);
							from.rename(nameInJar, "../%s".formatted(tmpDst.getName()));
						});
						spec.into(tmpDir);
					});
					Files.copy(tmpDst, dst);

					FileUtils.forceDelete(tmpDir);

					if (after != null) {
						after.run();
					}
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
		}
	}
}
