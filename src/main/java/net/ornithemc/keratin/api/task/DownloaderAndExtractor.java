package net.ornithemc.keratin.api.task;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import net.ornithemc.keratin.api.maven.MavenArtifact;

public interface DownloaderAndExtractor extends Downloader, Extractor {

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
}
