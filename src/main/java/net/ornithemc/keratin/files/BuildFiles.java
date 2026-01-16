package net.ornithemc.keratin.files;

import java.io.File;
import java.io.IOException;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.files.BuildFilesAccess;

public class BuildFiles extends FileContainer implements FileCache, BuildFilesAccess {

	public BuildFiles(KeratinGradleExtension keratin, KeratinFiles files) {
		super(keratin, files);
	}

	@Override
	public void mkdirs() throws IOException {
		mkdirs(getDirectory());
	}

	@Override
	public File getDirectory() {
		return new File(project.getLayout().getBuildDirectory().getAsFile().get(), "%s/gen%d/".formatted(keratin.getLocalCacheDirectory().get(), keratin.getIntermediaryGen().get()));
	}
}
