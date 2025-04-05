package net.ornithemc.keratin.files;

import java.io.File;
import java.io.IOException;

import org.gradle.api.provider.Property;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.files.MappingsDevelopmentFilesAccess;
import net.ornithemc.keratin.api.task.enigma.EnigmaSession;

public class MappingsDevelopmentFiles extends FileContainer implements MappingsDevelopmentFilesAccess {

	private final Property<BuildFiles> buildFiles;

	public MappingsDevelopmentFiles(KeratinGradleExtension keratin, OrnitheFiles files) {
		super(keratin, files);

		this.buildFiles = property(BuildFiles.class, () -> new BuildFiles(keratin, files));
	}

	@Override
	public void mkdirs() throws IOException {
		mkdirs(getMappingsDirectory());
		mkdirs(getRunDirectory());

		getBuildFiles().mkdirs();
	}

	@Override
	public File getMappingsDirectory() {
		return project.file("mappings");
	}

	@Override
	public File getRunDirectory() {
		return project.file("run");
	}

	@Override
	public File getEnigmaProfileJson() {
		return project.file("enigma/enigma_profile.json");
	}

	@Override
	public File getWorkingDirectory(MinecraftVersion minecraftVersion) {
		return new File(getRunDirectory(), minecraftVersion.id());
	}

	@Override
	public File getEnigmaSessionLock(MinecraftVersion minecraftVersion) {
		return new File(getWorkingDirectory(minecraftVersion), EnigmaSession.LOCK_FILE);
	}

	@Override
	public BuildFiles getBuildFiles() {
		return buildFiles.get();
	}

	public static class BuildFiles extends FileContainer implements MappingsDevelopmentFilesAccess.BuildFilesAccess {

		public BuildFiles(KeratinGradleExtension keratin, OrnitheFiles files) {
			super(keratin, files);
		}

		private File file(String name) {
			return files.getLocalCache().file(name);
		}

		@Override
		public File getMappingsFile(MinecraftVersion minecraftVersion) {
			return file("%s-named.tiny".formatted(minecraftVersion.id()));
		}

		@Override
		public File getProcessedMappingsFile(MinecraftVersion minecraftVersion) {
			return file("%s-processed-named.tiny".formatted(minecraftVersion.id()));
		}

		@Override
		public File getCompletedMappingsFile(MinecraftVersion minecraftVersion) {
			return file("%s-completed.tiny".formatted(minecraftVersion.id()));
		}

		@Override
		public File getTinyV1MappingsFile(String minecraftVersion) {
			return file("%s-tiny-v1.tiny".formatted(minecraftVersion));
		}

		@Override
		public File getTinyV2MappingsFile(String minecraftVersion) {
			return file("%s-tiny-v2.tiny".formatted(minecraftVersion));
		}

		@Override
		public File getMergedTinyV1MappingsFile(String minecraftVersion) {
			return file("%s-merged-tiny-v1.tiny".formatted(minecraftVersion));
		}

		@Override
		public File getMergedTinyV2MappingsFile(String minecraftVersion) {
			return file("%s-merged-tiny-v2.tiny".formatted(minecraftVersion));
		}

		@Override
		public File getCompressedMergedTinyV1MappingsFile(String minecraftVersion) {
			return file("%s-merged-tiny-v1.gz".formatted(minecraftVersion));
		}

		@Override
		public File getNamedJar(String minecraftVersion) {
			return file("%s-named.jar".formatted(minecraftVersion));
		}

		@Override
		public File getProcessedNamedJar(String minecraftVersion) {
			return file("%s-processed-named.jar".formatted(minecraftVersion));
		}

		@Override
		public File getJavadocNamedJar(String minecraftVersion) {
			return file("%s-javadoc-named.jar".formatted(minecraftVersion));
		}

		@Override
		public File getFakeSourceDirectory(String minecraftVersion) {
			return file("%s-fakeSrc".formatted(minecraftVersion));
		}

		@Override
		public File getJavadocDirectory(String minecraftVersion) {
			return file("%s-javadoc".formatted(minecraftVersion));
		}
	}
}
