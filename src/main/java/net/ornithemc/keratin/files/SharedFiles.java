package net.ornithemc.keratin.files;

import java.io.File;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.files.SharedFilesAccess;

public class SharedFiles extends FileContainer implements SharedFilesAccess {

	public SharedFiles(KeratinGradleExtension keratin, OrnitheFiles files) {
		super(keratin, files);
	}

	private File file(String name) {
		return project.file(name);
	}

	@Override
	public File getMatchesDirectory() {
		return file("matches/matches");
	}

	@Override
	public File getNamedMappingsBuildsJson() {
		return file("named-mappings-builds.json");
	}

	@Override
	public File getExceptionsBuildsJson() {
		return file("exceptions-builds.json");
	}

	@Override
	public File getSignaturesBuildsJson() {
		return file("signatures-builds.json");
	}

	@Override
	public File getNestsBuildsJson() {
		return file("nests-builds.json");
	}

	@Override
	public File getDecompiledSourceDirectory(MinecraftVersion minecraftVersion) {
		return file("%s-decompiledSrc".formatted(minecraftVersion.id()));
	}
}
