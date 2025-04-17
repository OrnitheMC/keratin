package net.ornithemc.keratin.api.task;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public interface Extractor {

	default boolean extract(String pathInZip, File zip, File dst) {
		return extract(pathInZip, zip, dst, false);
	}

	default boolean extract(String pathInZip, File zip, File dst, boolean overwrite) {
		if (overwrite || !dst.exists()) {
			if (dst.exists()) {
				dst.delete();
			}

			try (FileSystem fs = FileSystems.newFileSystem(zip.toPath())) {
				Path src = fs.getPath(pathInZip);

				if (Files.exists(src)) {
					Files.copy(src, dst.toPath());
				} else {
					throw new IOException("file " + pathInZip + " could not be found!");
				}
			} catch (IOException e) {
				throw new RuntimeException("error while extracting file " + pathInZip + " from zip " + zip.getName());
			}

			return true;
		}

		return false;
	}
}
