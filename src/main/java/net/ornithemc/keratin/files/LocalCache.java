package net.ornithemc.keratin.files;

import java.io.File;
import java.io.IOException;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.files.LocalCacheAccess;

public class LocalCache extends FileContainer implements FileCache, LocalCacheAccess {

	public LocalCache(KeratinGradleExtension keratin, KeratinFiles files) {
		super(keratin, files);
	}

	@Override
	public void mkdirs() throws IOException {
		mkdirs(getDirectory());
	}

	@Override
	public File getDirectory() {
		return project.getLayout().getBuildDirectory().dir(keratin.getLocalCacheDirectory().get().formatted(keratin.getIntermediaryGen().get())).get().getAsFile();
	}
}
