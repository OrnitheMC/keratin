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
		return new File(project.getRootDir(), ".gradle/%s/gen%d/".formatted(keratin.getLocalCacheDirectory().get(), keratin.getIntermediaryGen().get()));
	}

	@Override
	public File getOldNamedMappingsBuildsJson() {
		return file("old-named-mappings-builds.json");
	}

	@Override
	public File getOldExceptionsBuildsJson() {
		return file("old-exceptions-builds.json");
	}

	@Override
	public File getOldSignaturesBuildsJson() {
		return file("old-signatures-builds.json");
	}

	@Override
	public File getOldNestsBuildsJson() {
		return file("old-nests-builds.json");
	}
}
