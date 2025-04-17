package net.ornithemc.keratin.api.task;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import net.ornithemc.keratin.api.maven.MavenArtifact;

public interface DownloaderAndExtractor extends Downloader, Extractor {

	default boolean downloadAndExtract(MavenArtifact artifact, String pathInZip, File dstZip, File dst) throws Exception {
		return downloadAndExtract(artifact, pathInZip, dstZip, dst, false);
	}

	default boolean downloadAndExtract(MavenArtifact artifact, String pathInZip, File dstZip, File dst, boolean overwrite) throws Exception {
		return downloadAndExtract(artifact.url(), artifact.sha1(), pathInZip, dstZip, dst);
	}

	default boolean downloadAndExtract(String url, String sha1, String pathInZip, File dstZip, File dst) throws Exception {
		return downloadAndExtract(url, sha1, pathInZip, dstZip, dst, false);
	}

	default boolean downloadAndExtract(String url, String sha1, String pathInZip, File dstZip, File dst, boolean overwrite) throws Exception {
		CompletableFuture<Void> download = downloadFuture(url, sha1, dstZip, overwrite);

		if (download == null) {
			return extract(pathInZip, dstZip, dst, overwrite);
		} else {
			download.thenRun(() -> extract(pathInZip, dstZip, dst, true));
			return true;
		}
	}
}
