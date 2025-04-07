package net.ornithemc.keratin.api.task;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public interface Extractor {

	default boolean extract(String pathInJar, File jar, File dst) {
		return extract(pathInJar, jar, dst, false);
	}

	default boolean extract(String pathInJar, File jar, File dst, boolean overwrite) {
		if (overwrite || !dst.exists()) {
			if (dst.exists()) {
				dst.delete();
			}

			try (FileSystem fs = FileSystems.newFileSystem(jar.toPath())) {
				Path src = fs.getPath(pathInJar);

				if (Files.exists(src)) {
					Files.copy(src, dst.toPath());
				} else {
					throw new IOException("file " + pathInJar + " could not be found!");
				}
			} catch (IOException e) {
				throw new RuntimeException("error while extracting file " + pathInJar + " from jar " + jar.getName());
			}

			return true;
		}

		return false;
	}
}
