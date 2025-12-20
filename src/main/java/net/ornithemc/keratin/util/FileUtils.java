package net.ornithemc.keratin.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileUtils {

	public static boolean copy(File src, File dst, boolean overwrite) throws IOException {
		return copy(src.toPath(), dst.toPath(), overwrite);
	}

	public static boolean copy(Path src, Path dst, boolean overwrite) throws IOException {
		if (overwrite) {
			Files.deleteIfExists(dst);
		} else if (!Files.exists(dst)) {
			overwrite = true;
		}

		if (overwrite) {
			Files.copy(src, dst);
		}

		return overwrite;
	}
}
