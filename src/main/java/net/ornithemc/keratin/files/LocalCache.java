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
	public File getNamedMappingsBuildsJsonBackup() {
		return file("named-mappings-builds.json.backup");
	}

	@Override
	public File getExceptionsBuildsJsonBackup() {
		return file("exceptions-builds.json.backup");
	}

	@Override
	public File getSignaturesBuildsJsonBackup() {
		return file("signatures-builds.json.backup");
	}

	@Override
	public File getNestsBuildsJsonBackup() {
		return file("nests-builds.json.backup");
	}
}
