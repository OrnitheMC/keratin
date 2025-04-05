package net.ornithemc.keratin.files;

import java.io.File;

public interface FileCache {

	File getDirectory();

	default File file(String name) {
		return new File(getDirectory(), name);
	}
}
