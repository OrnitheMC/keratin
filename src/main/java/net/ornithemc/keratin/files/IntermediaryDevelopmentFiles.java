package net.ornithemc.keratin.files;

import java.io.File;
import java.io.IOException;

import org.gradle.api.provider.Property;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.files.IntermediaryDevelopmentFilesAccess;
import net.ornithemc.keratin.util.Versioned;

public class IntermediaryDevelopmentFiles extends FileContainer implements IntermediaryDevelopmentFilesAccess {

	private final Property<File> mappingsDir;

	private final Versioned<String, File> tinyV1MappingsFile;
	private final Versioned<String, File> tinyV2MappingsFile;

	public IntermediaryDevelopmentFiles(KeratinGradleExtension keratin, LocalCache localCache) {
		super(keratin);

		this.mappingsDir = fileProperty(() -> this.project.file("mappings"));

		this.tinyV1MappingsFile = new Versioned<>(minecraftVersion -> new File(getMappingsDirectory(), "%s.tiny".formatted(minecraftVersion)));
		this.tinyV2MappingsFile = new Versioned<>(minecraftVersion -> localCache.file("%s.tiny".formatted(minecraftVersion)));
	}

	@Override
	public void mkdirs() throws IOException {
		mkdirs(getMappingsDirectory());
	}

	@Override
	public File getMappingsDirectory() {
		return mappingsDir.get();
	}

	@Override
	public File getTinyV1MappingsFile(String minecraftVersion) {
		return tinyV1MappingsFile.get(minecraftVersion);
	}

	@Override
	public File getTinyV2MappingsFile(String minecraftVersion) {
		return tinyV2MappingsFile.get(minecraftVersion);
	}
}
