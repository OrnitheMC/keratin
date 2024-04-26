package net.ornithemc.keratin.api.task;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.gradle.api.Project;
import org.gradle.api.Task;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import de.undercouch.gradle.tasks.download.DownloadAction;

import net.ornithemc.keratin.KeratinGradleExtension;

public interface Downloader extends TaskAware {

	private static boolean validateChecksum(File file, String sha1) throws IOException {
		if (sha1.isEmpty()) {
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

	default void download(String url, File dst) throws Exception {
		download(url, "", dst);
	}

	default void download(String url, String sha1, File dst) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		Project project = keratin.getProject();

		if (!dst.exists() || isRefreshDependencies() || !validateChecksum(dst, sha1)) {
			DownloadAction download = new DownloadAction(project, (Task) this);

			download.src(new URL(url));
			download.dest(dst);
			download.overwrite(true);

			download.execute();
		}
	}
}
