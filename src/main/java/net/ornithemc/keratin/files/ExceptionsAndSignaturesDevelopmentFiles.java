package net.ornithemc.keratin.files;

import java.io.File;
import java.io.IOException;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.files.ExceptionsAndSignaturesDevelopmentFilesAccess;

public class ExceptionsAndSignaturesDevelopmentFiles extends FileContainer implements ExceptionsAndSignaturesDevelopmentFilesAccess {

	private final SetupJars setupJars;
	private final SetupFiles setupFiles;
	private final SourceJars sourceJars;
	private final SourceMappings sourceMappings;
	private final BuildFiles buildFiles;

	public ExceptionsAndSignaturesDevelopmentFiles(KeratinGradleExtension keratin, KeratinFiles files) {
		super(keratin, files);

		this.setupJars = new SetupJars(keratin, files);
		this.setupFiles = new SetupFiles(keratin, files);
		this.sourceJars = new SourceJars(keratin, files);
		this.sourceMappings = new SourceMappings(keratin, files);
		this.buildFiles = new BuildFiles(keratin, files);
	}

	@Override
	public void mkdirs() throws IOException {
		mkdirs(getExceptionsDirectory());
		mkdirs(getSignaturesDirectory());
	}

	@Override
	public File getExceptionsDirectory() {
		return project.file("exceptions");
	}

	@Override
	public File getSignaturesDirectory() {
		return project.file("signatures");
	}

	@Override
	public File getClientExceptionsFile(MinecraftVersion minecraftVersion) {
		if (minecraftVersion.hasSharedObfuscation()) {
			throw new IllegalArgumentException("there are no shared mappings for Minecraft version " + minecraftVersion.id() + " - please use the merged exceptions!");
		} else if (!minecraftVersion.hasClient()) {
			throw new IllegalArgumentException("client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
		} else if (minecraftVersion.hasSharedVersioning()) {
			return new File(getExceptionsDirectory(), "%s-client.excs".formatted(minecraftVersion.client().id()));
		} else {
			return new File(getExceptionsDirectory(), "%s.excs".formatted(minecraftVersion.client().id()));
		}
	}

	@Override
	public File getServerExceptionsFile(MinecraftVersion minecraftVersion) {
		if (minecraftVersion.hasSharedObfuscation()) {
			throw new IllegalArgumentException("there are no shared mappings for Minecraft version " + minecraftVersion.id() + " - please use the merged exceptions!");
		} else if (!minecraftVersion.hasServer()) {
			throw new IllegalArgumentException("server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
		} else if (minecraftVersion.hasSharedVersioning()) {
			return new File(getExceptionsDirectory(), "%s-server.excs".formatted(minecraftVersion.server().id()));
		} else {
			return new File(getExceptionsDirectory(), "%s.excs".formatted(minecraftVersion.server().id()));
		}
	}

	@Override
	public File getMergedExceptionsFile(MinecraftVersion minecraftVersion) {
		if (!minecraftVersion.hasSharedObfuscation()) {
			throw new IllegalArgumentException("game jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: the client and server jars do not have shared mappings!");
		} else {
			return new File(getExceptionsDirectory(), "%s.excs".formatted(minecraftVersion.id()));
		}
	}

	@Override
	public File getClientSignaturesFile(MinecraftVersion minecraftVersion) {
		if (minecraftVersion.hasSharedObfuscation()) {
			throw new IllegalArgumentException("there are no shared mappings for Minecraft version " + minecraftVersion.id() + " - please use the merged signatures!");
		} else if (!minecraftVersion.hasClient()) {
			throw new IllegalArgumentException("client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
		} else if (minecraftVersion.hasSharedVersioning()) {
			return new File(getSignaturesDirectory(), "%s-client.sigs".formatted(minecraftVersion.client().id()));
		} else {
			return new File(getSignaturesDirectory(), "%s.sigs".formatted(minecraftVersion.client().id()));
		}
	}

	@Override
	public File getServerSignaturesFile(MinecraftVersion minecraftVersion) {
		if (minecraftVersion.hasSharedObfuscation()) {
			throw new IllegalArgumentException("there are no shared mappings for Minecraft version " + minecraftVersion.id() + " - please use the merged signatures!");
		} else if (!minecraftVersion.hasServer()) {
			throw new IllegalArgumentException("server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
		} else if (minecraftVersion.hasSharedVersioning()) {
			return new File(getSignaturesDirectory(), "%s-server.sigs".formatted(minecraftVersion.server().id()));
		} else {
			return new File(getSignaturesDirectory(), "%s.sigs".formatted(minecraftVersion.server().id()));
		}
	}

	@Override
	public File getMergedSignaturesFile(MinecraftVersion minecraftVersion) {
		if (!minecraftVersion.hasSharedObfuscation()) {
			throw new IllegalArgumentException("game jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: the client and server jars do not have shared mappings!");
		} else {
			return new File(getSignaturesDirectory(), "%s.sigs".formatted(minecraftVersion.id()));
		}
	}

	@Override
	public SetupJars getSetupJars() {
		return setupJars;
	}

	@Override
	public SetupFiles getSetupFiles() {
		return setupFiles;
	}

	@Override
	public SourceJars getSourceJars() {
		return sourceJars;
	}

	@Override
	public SourceMappings getSourceMappings() {
		return sourceMappings;
	}

	@Override
	public BuildFiles getBuildFiles() {
		return buildFiles;
	}

	public static class SetupJars extends FileContainer implements ExceptionsAndSignaturesDevelopmentFilesAccess.SetupJarsAccess {

		public SetupJars(KeratinGradleExtension keratin, KeratinFiles files) {
			super(keratin, files);
		}

		private File file(String name) {
			return files.getLocalCache().file(name);
		}

		@Override
		public File getClientJar(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasClient()) {
				throw new IllegalArgumentException("client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("setup client jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
			} else {
				return file("%s-setup-client.jar".formatted(minecraftVersion.client().id()));
			}
		}

		@Override
		public File getServerJar(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("setup server jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
			} else {
				return file("%s-setup-server.jar".formatted(minecraftVersion.server().id()));
			}
		}

		@Override
		public File getMergedJar(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("setup jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
			} else {
				return file("%s-setup-merged.jar".formatted(minecraftVersion.id()));
			}
		}

		@Override
		public File getIntermediaryClientJar(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasClient()) {
				throw new IllegalArgumentException("client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("setup client jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
			} else {
				return file("%s-intermediary-setup-client.jar".formatted(minecraftVersion.client().id()));
			}
		}

		@Override
		public File getIntermediaryServerJar(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("setup server jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
			} else {
				return file("%s-intermediary-setup-server.jar".formatted(minecraftVersion.server().id()));
			}
		}

		@Override
		public File getIntermediaryMergedJar(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("setup jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
			} else {
				return file("%s-intermediary-setup-merged.jar".formatted(minecraftVersion.id()));
			}
		}
	}

	public static class SetupFiles extends FileContainer implements ExceptionsAndSignaturesDevelopmentFilesAccess.SetupFilesAccess {

		public SetupFiles(KeratinGradleExtension keratin, KeratinFiles files) {
			super(keratin, files);
		}

		private File file(String name) {
			return files.getLocalCache().file(name);
		}

		@Override
		public File getClientIntermediaryMappingsFile(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasClient()) {
				throw new IllegalArgumentException("client intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("client intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
			} else {
				return file("%s-setup-intermediary-client.tiny".formatted(minecraftVersion.client().id()));
			}
		}

		@Override
		public File getServerIntermediaryMappingsFile(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("server intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("server intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
			} else {
				return file("%s-setup-intermediary-server.tiny".formatted(minecraftVersion.server().id()));
			}
		}

		@Override
		public File getMergedIntermediaryMappingsFile(MinecraftVersion minecraftVersion) {
			return file("%s-setup-intermediary-merged.tiny".formatted(minecraftVersion.id()));
		}

		@Override
		public File getClientNamedMappingsFile(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasClient()) {
				throw new IllegalArgumentException("client named mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("client named mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
			} else {
				return file("%s-setup-named-client.tiny".formatted(minecraftVersion.client().id()));
			}
		}

		@Override
		public File getServerNamedMappingsFile(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("server named mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("server named mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
			} else {
				return file("%s-setup-named-server.tiny".formatted(minecraftVersion.server().id()));
			}
		}

		@Override
		public File getMergedNamedMappingsFile(MinecraftVersion minecraftVersion) {
			return file("%s-setup-named-merged.tiny".formatted(minecraftVersion.id()));
		}

		@Override
		public File getClientExceptionsFile(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasClient()) {
				throw new IllegalArgumentException("client exceptions for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("client exceptions for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged exceptions!");
			} else {
				return file("%s-setup-client.excs".formatted(minecraftVersion.client().id()));
			}
		}

		@Override
		public File getServerExceptionsFile(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("server exceptions for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("server exceptions for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged exceptions!");
			} else {
				return file("%s-setup-server.excs".formatted(minecraftVersion.server().id()));
			}
		}

		@Override
		public File getMergedExceptionsFile(MinecraftVersion minecraftVersion) {
			return file("%s-setup-merged.excs".formatted(minecraftVersion.id()));
		}

		@Override
		public File getClientSignaturesFile(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasClient()) {
				throw new IllegalArgumentException("client signatures for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("client signatures for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged signatures!");
			} else {
				return file("%s-setup-client.sigs".formatted(minecraftVersion.client().id()));
			}
		}

		@Override
		public File getServerSignaturesFile(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("server signatures for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("server signatures for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged signatures!");
			} else {
				return file("%s-setup-server.sigs".formatted(minecraftVersion.server().id()));
			}
		}

		@Override
		public File getMergedSignaturesFile(MinecraftVersion minecraftVersion) {
			return file("%s-setup-merged.sigs".formatted(minecraftVersion.id()));
		}
	}

	public static class SourceJars extends FileContainer implements ExceptionsAndSignaturesDevelopmentFilesAccess.SourceJarsAccess {

		public SourceJars(KeratinGradleExtension keratin, KeratinFiles files) {
			super(keratin, files);
		}

		private File file(String name) {
			return files.getLocalCache().file(name);
		}

		@Override
		public File getClientJar(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasClient()) {
				throw new IllegalArgumentException("client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("source client jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
			} else {
				return file("%s-source-client.jar".formatted(minecraftVersion.client().id()));
			}
		}

		@Override
		public File getServerJar(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("source server jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
			} else {
				return file("%s-source-server.jar".formatted(minecraftVersion.server().id()));
			}
		}

		@Override
		public File getMergedJar(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("source jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
			} else {
				return file("%s-source-merged.jar".formatted(minecraftVersion.id()));
			}
		}

		@Override
		public File getNamedClientJar(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasClient()) {
				throw new IllegalArgumentException("client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("source client jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
			} else {
				return file("%s-named-source-client.jar".formatted(minecraftVersion.client().id()));
			}
		}

		@Override
		public File getNamedServerJar(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("source server jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
			} else {
				return file("%s-named-source-server.jar".formatted(minecraftVersion.server().id()));
			}
		}

		@Override
		public File getNamedMergedJar(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("source jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
			} else {
				return file("%s-named-source-merged.jar".formatted(minecraftVersion.id()));
			}
		}

		@Override
		public File getProcessedNamedJar(MinecraftVersion minecraftVersion) {
			return file("%s-processed-named-source.jar".formatted(minecraftVersion.id()));
		}
	}

	public static class SourceMappings extends FileContainer implements ExceptionsAndSignaturesDevelopmentFilesAccess.SourceMappingsAccess {

		public SourceMappings(KeratinGradleExtension keratin, KeratinFiles files) {
			super(keratin, files);
		}

		private File file(String name) {
			return files.getLocalCache().file(name);
		}

		@Override
		public File getClientMappingsFile(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasClient()) {
				throw new IllegalArgumentException("client mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("client mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
			} else {
				return file("%s-source-client.tiny".formatted(minecraftVersion.client().id()));
			}
		}

		@Override
		public File getServerMappingsFile(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("server mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("server mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
			} else {
				return file("%s-source-server.tiny".formatted(minecraftVersion.server().id()));
			}
		}

		@Override
		public File getMergedMappingsFile(MinecraftVersion minecraftVersion) {
			return file("%s-source-merged.tiny".formatted(minecraftVersion.id()));
		}
	}

	public static class BuildFiles extends FileContainer implements ExceptionsAndSignaturesDevelopmentFilesAccess.BuildFilesAccess {

		public BuildFiles(KeratinGradleExtension keratin, KeratinFiles files) {
			super(keratin, files);
		}

		private File file(String name) {
			return files.getLocalCache().file(name);
		}

		@Override
		public File getBaseClientExceptionsFile(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasClient()) {
				throw new IllegalArgumentException("client exceptions for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("client exceptions for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged exceptions!");
			} else {
				return file("%s-base-client.excs".formatted(minecraftVersion.client().id()));
			}
		}

		@Override
		public File getBaseServerExceptionsFile(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("server exceptions for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("server exceptions for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged exceptions!");
			} else {
				return file("%s-base-server.excs".formatted(minecraftVersion.server().id()));
			}
		}

		@Override
		public File getBaseMergedExceptionsFile(MinecraftVersion minecraftVersion) {
			return file("%s-base-merged.excs".formatted(minecraftVersion.id()));
		}

		@Override
		public File getBaseClientSignaturesFile(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasClient()) {
				throw new IllegalArgumentException("client signatures for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("client signatures for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged signatures!");
			} else {
				return file("%s-base-client.sigs".formatted(minecraftVersion.client().id()));
			}
		}

		@Override
		public File getBaseServerSignaturesFile(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("server signatures for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("server signatures for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged signatures!");
			} else {
				return file("%s-base-server.sigs".formatted(minecraftVersion.server().id()));
			}
		}

		@Override
		public File getBaseMergedSignaturesFile(MinecraftVersion minecraftVersion) {
			return file("%s-base-merged.sigs".formatted(minecraftVersion.id()));
		}

		@Override
		public File getGeneratedClientJar(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasClient()) {
				throw new IllegalArgumentException("client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("generated client jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
			} else {
				return file("%s-generated-client.jar".formatted(minecraftVersion.client().id()));
			}
		}

		@Override
		public File getGeneratedServerJar(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("generated server jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
			} else {
				return file("%s-generated-server.jar".formatted(minecraftVersion.server().id()));
			}
		}

		@Override
		public File getGeneratedMergedJar(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("generated jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
			} else {
				return file("%s-generated-merged.jar".formatted(minecraftVersion.id()));
			}
		}

		@Override
		public File getNamedGeneratedClientJar(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasClient()) {
				throw new IllegalArgumentException("client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("generated client jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
			} else {
				return file("%s-named-generated-client.jar".formatted(minecraftVersion.client().id()));
			}
		}

		@Override
		public File getNamedGeneratedServerJar(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("generated server jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
			} else {
				return file("%s-named-generated-server.jar".formatted(minecraftVersion.server().id()));
			}
		}

		@Override
		public File getNamedGeneratedMergedJar(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("generated jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
			} else {
				return file("%s-named-generated-merged.jar".formatted(minecraftVersion.id()));
			}
		}

		@Override
		public File getGeneratedClientExceptionsFile(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasClient()) {
				throw new IllegalArgumentException("client exceptions for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("client exceptions for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged exceptions!");
			} else {
				return file("%s-generated-client.excs".formatted(minecraftVersion.client().id()));
			}
		}

		@Override
		public File getGeneratedServerExceptionsFile(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("server exceptions for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("server exceptions for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged exceptions!");
			} else {
				return file("%s-generated-server.excs".formatted(minecraftVersion.server().id()));
			}
		}

		@Override
		public File getGeneratedMergedExceptionsFile(MinecraftVersion minecraftVersion) {
			return file("%s-generated-merged.excs".formatted(minecraftVersion.id()));
		}

		@Override
		public File getGeneratedClientSignaturesFile(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasClient()) {
				throw new IllegalArgumentException("client signatures for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("client signatures for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged signatures!");
			} else {
				return file("%s-generated-client.sigs".formatted(minecraftVersion.client().id()));
			}
		}

		@Override
		public File getGeneratedServerSignaturesFile(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("server signatures for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("server signatures for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged signatures!");
			} else {
				return file("%s-generated-server.sigs".formatted(minecraftVersion.server().id()));
			}
		}

		@Override
		public File getGeneratedMergedSignaturesFile(MinecraftVersion minecraftVersion) {
			return file("%s-generated-merged.sigs".formatted(minecraftVersion.id()));
		}
	}
}
