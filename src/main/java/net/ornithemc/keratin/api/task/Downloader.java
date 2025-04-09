package net.ornithemc.keratin.api.task;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

import org.gradle.api.Project;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import de.undercouch.gradle.tasks.download.DownloadAction;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.maven.MavenArtifact;

public interface Downloader extends TaskAware {

	default boolean download(String url, File output) throws Exception {
		return download(url, output, false);
	}

	default boolean download(String url, File output, boolean overwrite) throws Exception {
		return download(url, null, output, overwrite);
	}

	default boolean download(MavenArtifact artifact, File output) throws Exception {
		return download(artifact, output, false);
	}

	default boolean download(MavenArtifact artifact, File output, boolean overwrite) throws Exception {
		return download(artifact.url(), artifact.sha1(), output, overwrite);
	}

	default boolean download(String url, String sha1, File output) throws Exception {
		return download(url, sha1, output, false);
	}

	default boolean download(String url, String sha1, File output, boolean overwrite) throws Exception {
		return downloadFuture(url, sha1, output, overwrite) != null;
	}

	default CompletableFuture<Void> downloadFuture(String url, String sha1, File output, boolean overwrite) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		Project project = keratin.getProject();

		if (overwrite || !output.exists() || isRefreshDependencies() || !validateChecksum(output, sha1)) {
			DownloadAction downloader = new DownloadAction(project, (org.gradle.api.Task) this);

			downloader.src(new URI(url));
			downloader.dest(output);
			downloader.overwrite(true);

			return downloader.execute();
		}

		return null;
	}

	private static boolean validateChecksum(File file, String sha1) throws IOException {
		if (sha1 == null || sha1.isEmpty()) {
			return true;
		}

		@SuppressWarnings("deprecation")
		HashCode hash = Files.asByteSource(file).hash(Hashing.sha1());

		StringBuilder sb = new StringBuilder();

		for (byte b : hash.asBytes()) {
			sb.append(Integer.toString((b & 0xFF) + 0x100, 16).substring(1));
		}

		return sb.toString().equals(sha1);
	}
}
