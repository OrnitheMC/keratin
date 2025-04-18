package net.ornithemc.keratin.files;

import java.io.File;
import java.io.IOException;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.files.MappingsDevelopmentFilesAccess;
import net.ornithemc.keratin.api.task.enigma.EnigmaSession;
import net.ornithemc.keratin.api.task.unpick.UnpickDefinitions;

public class MappingsDevelopmentFiles extends FileContainer implements MappingsDevelopmentFilesAccess {

	private final BuildFiles buildFiles;

	public MappingsDevelopmentFiles(KeratinGradleExtension keratin, OrnitheFiles files) {
		super(keratin, files);

		this.buildFiles = new BuildFiles(keratin, files);
	}

	@Override
	public void mkdirs() throws IOException {
		mkdirs(getMappingsDirectory());
		mkdirs(getUnpickDirectory());
		mkdirs(getRunDirectory());
	}

	@Override
	public File getMappingsDirectory() {
		return project.file("mappings");
	}

	@Override
	public File getUnpickDirectory() {
		return project.file("unpick-definitions");
	}

	@Override
	public File getUnpickJson() {
		return new File(getUnpickDirectory(), "unpick.json");
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
		return buildFiles;
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
		public File getNamedNestsFile(MinecraftVersion minecraftVersion) {
			return file("%s-named.nest".formatted(minecraftVersion.id()));
		}

		@Override
		public File getProcessedUnpickDefinitionsFile(MinecraftVersion minecraftVersion) {
			return file("%s-processed%s".formatted(minecraftVersion.id(), UnpickDefinitions.FILE_EXTENSION));
		}

		@Override
		public File getProcessedIntermediaryUnpickDefinitionsFile(MinecraftVersion minecraftVersion) {
			return file("%s-processed-intermediary-gen%d%s".formatted(minecraftVersion.id(), getIntermediaryGen(), UnpickDefinitions.FILE_EXTENSION));
		}

		@Override
		public File getUnpickedProcessedIntermediaryJar(MinecraftVersion minecraftVersion) {
			return file("%s-unpicked-processed-intermediary-gen%d.jar".formatted(minecraftVersion.id(), getIntermediaryGen()));
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
		public File getUnpickDefinitionsFile(String minecraftVersion) {
			return file("%s%s".formatted(minecraftVersion, UnpickDefinitions.FILE_EXTENSION));
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
