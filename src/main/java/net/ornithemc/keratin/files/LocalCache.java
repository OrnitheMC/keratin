package net.ornithemc.keratin.files;

import java.io.File;
import java.io.IOException;

import org.gradle.api.provider.Property;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.files.LocalCacheAccess;

public class LocalCache extends FileContainer implements FileCache, LocalCacheAccess {

	private final Property<File> dir;

	public LocalCache(KeratinGradleExtension keratin) {
		super(keratin);

		this.dir = fileProperty(() -> this.project.getLayout().getBuildDirectory().dir(keratin.getLocalCacheDirectory().get()).get().getAsFile());
	}

	@Override
	public void mkdirs() throws IOException {
		mkdirs(getDirectory());
	}

	@Override
	public File getDirectory() {
		return dir.get();
	}
}
