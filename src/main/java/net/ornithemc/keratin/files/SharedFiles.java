package net.ornithemc.keratin.files;

import java.io.File;

import org.gradle.api.provider.Property;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.files.SharedFilesAccess;
import net.ornithemc.keratin.util.Versioned;

public class SharedFiles extends FileContainer implements SharedFilesAccess {

	private final Property<File> matchesDir;

	private final Property<File> namedMappingsBuildsJson;
	private final Property<File> exceptionsBuildsJson;
	private final Property<File> signaturesBuildsJson;
	private final Property<File> nestsBuildsJson;

	private final Versioned<MinecraftVersion, File> decompiledSrcDir;

	public SharedFiles(KeratinGradleExtension keratin) {
		super(keratin);

		this.matchesDir = fileProperty(() -> this.project.file("matches/matches"));

		this.namedMappingsBuildsJson = fileProperty(() -> this.project.file("named-mappings-builds.json"));
		this.exceptionsBuildsJson = fileProperty(() -> this.project.file("exceptions-builds.json"));
		this.signaturesBuildsJson = fileProperty(() -> this.project.file("signatures-builds.json"));
		this.nestsBuildsJson = fileProperty(() -> this.project.file("nests-builds.json"));

		this.decompiledSrcDir = new Versioned<>(minecraftVersion -> this.project.file("%s-decompiledSrc".formatted(minecraftVersion.id())));
	}

	@Override
	public File getMatchesDirectory() {
		return matchesDir.get();
	}

	@Override
	public File getNamedMappingsBuildsJson() {
		return namedMappingsBuildsJson.get();
	}

	@Override
	public File getExceptionsBuildsJson() {
		return exceptionsBuildsJson.get();
	}

	@Override
	public File getSignaturesBuildsJson() {
		return signaturesBuildsJson.get();
	}

	@Override
	public File getNestsBuildsJson() {
		return nestsBuildsJson.get();
	}

	@Override
	public File getDecompiledSourceDirectory(MinecraftVersion minecraftVersion) {
		return decompiledSrcDir.get(minecraftVersion);
	}
}
