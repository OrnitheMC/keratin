package net.ornithemc.keratin.files;

import java.io.File;
import java.io.IOException;

import org.gradle.api.provider.Property;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.files.MappingsDevelopmentFilesAccess;
import net.ornithemc.keratin.api.task.enigma.EnigmaSession;
import net.ornithemc.keratin.util.Versioned;

public class MappingsDevelopmentFiles extends FileContainer implements MappingsDevelopmentFilesAccess {

	private final Property<File> mappingsDir;
	private final Property<File> runDir;
	private final Property<File> enigmaProfileJson;

	private final Versioned<MinecraftVersion, File> workingDir;
	private final Versioned<MinecraftVersion, File> enigmaSessionLock;

	private final BuildFiles buildFiles;

	public MappingsDevelopmentFiles(KeratinGradleExtension keratin, LocalCache localCache) {
		super(keratin);

		this.mappingsDir = fileProperty(() -> this.project.file("mappings"));
		this.runDir = fileProperty(() -> this.project.file("run"));
		this.enigmaProfileJson = fileProperty(() -> this.project.file("enigma/enigma_profile.json"));

		this.workingDir = new Versioned<>(minecraftVersion -> new File(getRunDirectory(), minecraftVersion.id()));
		this.enigmaSessionLock = new Versioned<>(minecraftVersion -> new File(getWorkingDirectory(minecraftVersion), EnigmaSession.LOCK_FILE));

		this.buildFiles = new BuildFiles(keratin, localCache);
	}

	@Override
	public void mkdirs() throws IOException {
		mkdirs(getMappingsDirectory());
		mkdirs(getRunDirectory());

		buildFiles.mkdirs();
	}

	@Override
	public File getMappingsDirectory() {
		return mappingsDir.get();
	}

	@Override
	public File getRunDirectory() {
		return runDir.get();
	}

	@Override
	public File getEnigmaProfileJson() {
		return enigmaProfileJson.get();
	}

	@Override
	public File getWorkingDirectory(MinecraftVersion minecraftVersion) {
		return workingDir.get(minecraftVersion);
	}

	@Override
	public File getEnigmaSessionLock(MinecraftVersion minecraftVersion) {
		return enigmaSessionLock.get(minecraftVersion);
	}

	@Override
	public BuildFiles getBuildFiles() {
		return buildFiles;
	}

	public static class BuildFiles extends FileContainer implements MappingsDevelopmentFilesAccess.BuildFilesAccess {

		private final Versioned<MinecraftVersion, File> mappingsFile;
		private final Versioned<MinecraftVersion, File> processedMappingsFile;
		private final Versioned<MinecraftVersion, File> completedMappingsFile;
		private final Versioned<String, File> tinyV1MappingsFile;
		private final Versioned<String, File> tinyV2MappingsFile;
		private final Versioned<String, File> mergedTinyV1MappingsFile;
		private final Versioned<String, File> mergedTinyV2MappingsFile;
		private final Versioned<String, File> compressedMergedTinyV1MappingsFile;

		private final Versioned<String, File> namedJar;
		private final Versioned<String, File> processedNamedJar;
		private final Versioned<String, File> javadocNamedJar;

		private final Versioned<String, File> fakeSrcDir;
		private final Versioned<String, File> javadocDir;

		public BuildFiles(KeratinGradleExtension keratin, LocalCache localCache) {
			super(keratin);

			this.mappingsFile = new Versioned<>(minecraftVersion -> localCache.file("%s-named.tiny".formatted(minecraftVersion.id())));
			this.processedMappingsFile = new Versioned<>(minecraftVersion -> localCache.file("%s-processed-named.tiny".formatted(minecraftVersion.id())));
			this.completedMappingsFile = new Versioned<>(minecraftVersion -> localCache.file("%s-completed.tiny".formatted(minecraftVersion.id())));
			this.tinyV1MappingsFile = new Versioned<>(minecraftVersion -> localCache.file("%s-tiny-v1.tiny".formatted(minecraftVersion)));
			this.tinyV2MappingsFile = new Versioned<>(minecraftVersion -> localCache.file("%s-tiny-v2.tiny".formatted(minecraftVersion)));
			this.mergedTinyV1MappingsFile = new Versioned<>(minecraftVersion -> localCache.file("%s-merged-tiny-v1.tiny".formatted(minecraftVersion)));
			this.mergedTinyV2MappingsFile = new Versioned<>(minecraftVersion -> localCache.file("%s-merged-tiny-v2.tiny".formatted(minecraftVersion)));
			this.compressedMergedTinyV1MappingsFile = new Versioned<>(minecraftVersion -> localCache.file("%s-merged-tiny-v1.gz".formatted(minecraftVersion)));

			this.namedJar = new Versioned<>(minecraftVersion -> localCache.file("%s-named.jar".formatted(minecraftVersion)));
			this.processedNamedJar = new Versioned<>(minecraftVersion -> localCache.file("%s-processed-named.jar".formatted(minecraftVersion)));
			this.javadocNamedJar = new Versioned<>(minecraftVersion -> localCache.file("%s-javadoc-named.jar".formatted(minecraftVersion)));

			this.fakeSrcDir = new Versioned<>(minecraftVersion -> localCache.file("%s-fakeSrc".formatted(minecraftVersion)));
			this.javadocDir = new Versioned<>(minecraftVersion -> localCache.file("%s-javadoc".formatted(minecraftVersion)));
		}

		@Override
		public File getMappingsFile(MinecraftVersion minecraftVersion) {
			return mappingsFile.get(minecraftVersion);
		}

		@Override
		public File getProcessedMappingsFile(MinecraftVersion minecraftVersion) {
			return processedMappingsFile.get(minecraftVersion);
		}

		@Override
		public File getCompletedMappingsFile(MinecraftVersion minecraftVersion) {
			return completedMappingsFile.get(minecraftVersion);
		}

		@Override
		public File getTinyV1MappingsFile(String minecraftVersion) {
			return tinyV1MappingsFile.get(minecraftVersion);
		}

		@Override
		public File getTinyV2MappingsFile(String minecraftVersion) {
			return tinyV2MappingsFile.get(minecraftVersion);
		}

		@Override
		public File getMergedTinyV1MappingsFile(String minecraftVersion) {
			return mergedTinyV1MappingsFile.get(minecraftVersion);
		}

		@Override
		public File getMergedTinyV2MappingsFile(String minecraftVersion) {
			return mergedTinyV2MappingsFile.get(minecraftVersion);
		}

		@Override
		public File getCompressedMergedTinyV1MappingsFile(String minecraftVersion) {
			return compressedMergedTinyV1MappingsFile.get(minecraftVersion);
		}

		@Override
		public File getNamedJar(String minecraftVersion) {
			return namedJar.get(minecraftVersion);
		}

		@Override
		public File getProcessedNamedJar(String minecraftVersion) {
			return processedNamedJar.get(minecraftVersion);
		}

		@Override
		public File getJavadocNamedJar(String minecraftVersion) {
			return javadocNamedJar.get(minecraftVersion);
		}

		@Override
		public File getFakeSourceDirectory(String minecraftVersion) {
			return fakeSrcDir.get(minecraftVersion);
		}

		@Override
		public File getJavadocDirectory(String minecraftVersion) {
			return javadocDir.get(minecraftVersion);
		}
	}
}
