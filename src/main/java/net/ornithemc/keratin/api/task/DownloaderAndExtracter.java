package net.ornithemc.keratin.api.task;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import org.gradle.api.Project;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.maven.MavenArtifact;

public interface DownloaderAndExtracter extends Downloader {

	default boolean downloadAndExtract(MavenArtifact artifact, String pathInJar, File dstJar, File dst) throws Exception {
		return downloadAndExtract(artifact, pathInJar, dstJar, dst, false);
	}

	default boolean downloadAndExtract(MavenArtifact artifact, String pathInJar, File dstJar, File dst, boolean overwrite) throws Exception {
		CompletableFuture<Void> download = downloadFuture(artifact.url(), artifact.sha1(), dstJar, overwrite);

		if (download == null) {
			return extract(pathInJar, dstJar, dst, overwrite);
		} else {
			download.thenRun(() -> extract(pathInJar, dstJar, dst, true));
			return true;
		}
	}

	default boolean extract(String pathInJar, File jar, File dst) {
		return extract(pathInJar, jar, dst, false);
	}

	default boolean extract(String pathInJar, File jar, File dst, boolean overwrite) {
		if (overwrite || !dst.exists()) {
			KeratinGradleExtension keratin = getExtension();
			Project project = keratin.getProject();

			String nameInJar = pathInJar.substring(pathInJar.lastIndexOf('/') + 1);

			if (dst.exists()) {
				dst.delete();
			}

			project.copy(spec -> {
				spec.from(project.zipTree(jar), from -> {
					from.from(pathInJar);
					from.rename(nameInJar, dst.getName());
				});
				spec.into(dst.getParentFile().getAbsolutePath());
			});

			return true;
		}

		return false;
	}
}
