package net.ornithemc.keratin.files;

import java.io.File;
import java.io.IOException;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.files.IntermediaryDevelopmentFilesAccess;

public class IntermediaryDevelopmentFiles extends FileContainer implements IntermediaryDevelopmentFilesAccess {

	public IntermediaryDevelopmentFiles(KeratinGradleExtension keratin, KeratinFiles files) {
		super(keratin, files);
	}

	@Override
	public void mkdirs() throws IOException {
		mkdirs(getMappingsDirectory());
	}

	@Override
	public File getMappingsDirectory() {
		return project.file("mappings");
	}

	@Override
	public File getTinyV1MappingsFile(String minecraftVersion) {
		return new File(getMappingsDirectory(), "%s.tiny".formatted(minecraftVersion));
	}

	@Override
	public File getTinyV2MappingsFile(String minecraftVersion) {
		return files.getLocalCache().file("%s.tiny".formatted(minecraftVersion));
	}
}
