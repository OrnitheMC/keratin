package net.ornithemc.keratin.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;

import org.gradle.api.provider.Property;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.files.ExceptionsAndSignaturesDevelopmentFilesAccess;
import net.ornithemc.keratin.util.Versioned;

public class ExceptionsAndSignaturesDevelopmentFiles extends FileContainer implements ExceptionsAndSignaturesDevelopmentFilesAccess {

	private final Property<File> exceptionsDir;
	private final Property<File> signaturesDir;

	private final Versioned<MinecraftVersion, File> clientExceptionsFile;
	private final Versioned<MinecraftVersion, File> serverExceptionsFile;
	private final Versioned<MinecraftVersion, File> mergedExceptionsFile;
	private final Versioned<MinecraftVersion, File> clientSignaturesFile;
	private final Versioned<MinecraftVersion, File> serverSignaturesFile;
	private final Versioned<MinecraftVersion, File> mergedSignaturesFile;

	private final SetupJars setupJars;
	private final SetupFiles setupFiles;
	private final SourceJars sourceJars;
	private final SourceMappings sourceMappings;
	private final BuildFiles buildFiles;

	public ExceptionsAndSignaturesDevelopmentFiles(KeratinGradleExtension keratin, LocalCache localCache) {
		super(keratin);

		this.exceptionsDir = fileProperty(() -> this.project.file("exceptions"));
		this.signaturesDir = fileProperty(() -> this.project.file("signatures"));

		this.clientExceptionsFile = new Versioned<>(minecraftVersion -> {
			if (minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("there are no shared mappings for Minecraft version " + minecraftVersion.id() + " - please use the merged exceptions!");
			} else if (!minecraftVersion.hasClient()) {
				throw new NoSuchFileException("client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasSharedVersioning()) {
				return new File(getExceptionsDirectory(), "%s-client.excs".formatted(minecraftVersion.client().id()));
			} else {
				return new File(getExceptionsDirectory(), "%s.excs".formatted(minecraftVersion.client().id()));
			}
		});
		this.serverExceptionsFile = new Versioned<>(minecraftVersion -> {
			if (minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("there are no shared mappings for Minecraft version " + minecraftVersion.id() + " - please use the merged exceptions!");
			} else if (!minecraftVersion.hasServer()) {
				throw new NoSuchFileException("server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasSharedVersioning()) {
				return new File(getExceptionsDirectory(), "%s-server.excs".formatted(minecraftVersion.server().id()));
			} else {
				return new File(getExceptionsDirectory(), "%s.excs".formatted(minecraftVersion.server().id()));
			}
		});
		this.mergedExceptionsFile = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("game jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: the client and server jars do not have shared mappings!");
			} else {
				return new File(getExceptionsDirectory(), "%s.excs".formatted(minecraftVersion.id()));
			}
		});
		this.clientSignaturesFile = new Versioned<>(minecraftVersion -> {
			if (minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("there are no shared mappings for Minecraft version " + minecraftVersion.id() + " - please use the merged signatures!");
			} else if (!minecraftVersion.hasClient()) {
				throw new NoSuchFileException("client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasSharedVersioning()) {
				return new File(getSignaturesDirectory(), "%s-client.sigs".formatted(minecraftVersion.client().id()));
			} else {
				return new File(getSignaturesDirectory(), "%s.sigs".formatted(minecraftVersion.client().id()));
			}
		});
		this.serverSignaturesFile = new Versioned<>(minecraftVersion -> {
			if (minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("there are no shared mappings for Minecraft version " + minecraftVersion.id() + " - please use the merged signatures!");
			} else if (!minecraftVersion.hasServer()) {
				throw new NoSuchFileException("server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasSharedVersioning()) {
				return new File(getSignaturesDirectory(), "%s-server.sigs".formatted(minecraftVersion.server().id()));
			} else {
				return new File(getSignaturesDirectory(), "%s.sigs".formatted(minecraftVersion.server().id()));
			}
		});
		this.mergedSignaturesFile = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("game jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: the client and server jars do not have shared mappings!");
			} else {
				return new File(getSignaturesDirectory(), "%s.sigs".formatted(minecraftVersion.id()));
			}
		});

		this.setupJars = new SetupJars(keratin, localCache);
		this.setupFiles = new SetupFiles(keratin, localCache);
		this.sourceJars = new SourceJars(keratin, localCache);
		this.sourceMappings = new SourceMappings(keratin, localCache);
		this.buildFiles = new BuildFiles(keratin, localCache);
	}

	@Override
	public void mkdirs() throws IOException {
		mkdirs(getExceptionsDirectory());
		mkdirs(getSignaturesDirectory());
	}

	@Override
	public File getExceptionsDirectory() {
		return exceptionsDir.get();
	}

	@Override
	public File getSignaturesDirectory() {
		return signaturesDir.get();
	}

	@Override
	public File getClientExceptionsFile(MinecraftVersion minecraftVersion) {
		return clientExceptionsFile.get(minecraftVersion);
	}

	@Override
	public File getServerExceptionsFile(MinecraftVersion minecraftVersion) {
		return serverExceptionsFile.get(minecraftVersion);
	}

	@Override
	public File getMergedExceptionsFile(MinecraftVersion minecraftVersion) {
		return mergedExceptionsFile.get(minecraftVersion);
	}

	@Override
	public File getClientSignaturesFile(MinecraftVersion minecraftVersion) {
		return clientSignaturesFile.get(minecraftVersion);
	}

	@Override
	public File getServerSignaturesFile(MinecraftVersion minecraftVersion) {
		return serverSignaturesFile.get(minecraftVersion);
	}

	@Override
	public File getMergedSignaturesFile(MinecraftVersion minecraftVersion) {
		return mergedSignaturesFile.get(minecraftVersion);
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

		private final Versioned<MinecraftVersion, File> clientJar;
		private final Versioned<MinecraftVersion, File> serverJar;
		private final Versioned<MinecraftVersion, File> mergedJar;
		private final Versioned<MinecraftVersion, File> intermediaryClientJar;
		private final Versioned<MinecraftVersion, File> intermediaryServerJar;
		private final Versioned<MinecraftVersion, File> intermediaryMergedJar;

		public SetupJars(KeratinGradleExtension keratin, LocalCache localCache) {
			super(keratin);

			this.clientJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient()) {
					throw new NoSuchFileException("client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
				} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("setup client jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
				} else {
					return localCache.file("%s-setup-client.jar".formatted(minecraftVersion.client().id()));
				}
			});
			this.serverJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer()) {
					throw new NoSuchFileException("server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
				} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("setup server jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
				} else {
					return localCache.file("%s-setup-server.jar".formatted(minecraftVersion.server().id()));
				}
			});
			this.mergedJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
					throw new NoSuchFileException("setup jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
				} else {
					return localCache.file("%s-setup-merged.jar".formatted(minecraftVersion.id()));
				}
			});
			this.intermediaryClientJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient()) {
					throw new NoSuchFileException("client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
				} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("setup client jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
				} else {
					return localCache.file("%s-intermediary-setup-client.jar".formatted(minecraftVersion.client().id()));
				}
			});
			this.intermediaryServerJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer()) {
					throw new NoSuchFileException("server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
				} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("setup server jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
				} else {
					return localCache.file("%s-intermediary-setup-server.jar".formatted(minecraftVersion.server().id()));
				}
			});
			this.intermediaryMergedJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
					throw new NoSuchFileException("setup jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
				} else {
					return localCache.file("%s-intermediary-setup-merged.jar".formatted(minecraftVersion.id()));
				}
			});
		}

		@Override
		public File getClientJar(MinecraftVersion minecraftVersion) {
			return clientJar.get(minecraftVersion);
		}

		@Override
		public File getServerJar(MinecraftVersion minecraftVersion) {
			return serverJar.get(minecraftVersion);
		}

		@Override
		public File getMergedJar(MinecraftVersion minecraftVersion) {
			return mergedJar.get(minecraftVersion);
		}

		@Override
		public File getIntermediaryClientJar(MinecraftVersion minecraftVersion) {
			return intermediaryClientJar.get(minecraftVersion);
		}

		@Override
		public File getIntermediaryServerJar(MinecraftVersion minecraftVersion) {
			return intermediaryServerJar.get(minecraftVersion);
		}

		@Override
		public File getIntermediaryMergedJar(MinecraftVersion minecraftVersion) {
			return intermediaryMergedJar.get(minecraftVersion);
		}
	}

	public static class SetupFiles extends FileContainer implements ExceptionsAndSignaturesDevelopmentFilesAccess.SetupFilesAccess {

		private final Versioned<MinecraftVersion, File> clientIntermediaryMappingsFile;
		private final Versioned<MinecraftVersion, File> serverIntermediaryMappingsFile;
		private final Versioned<MinecraftVersion, File> mergedIntermediaryMappingsFile;
		private final Versioned<MinecraftVersion, File> clientNamedMappingsFile;
		private final Versioned<MinecraftVersion, File> serverNamedMappingsFile;
		private final Versioned<MinecraftVersion, File> mergedNamedMappingsFile;

		private final Versioned<MinecraftVersion, File> setupClientExceptionsFile;
		private final Versioned<MinecraftVersion, File> setupServerExceptionsFile;
		private final Versioned<MinecraftVersion, File> setupMergedExceptionsFile;
		private final Versioned<MinecraftVersion, File> setupClientSignaturesFile;
		private final Versioned<MinecraftVersion, File> setupServerSignaturesFile;
		private final Versioned<MinecraftVersion, File> setupMergedSignaturesFile;

		public SetupFiles(KeratinGradleExtension keratin, LocalCache localCache) {
			super(keratin);

			this.clientIntermediaryMappingsFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient()) {
					throw new NoSuchFileException("client intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("client intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
				} else {
					return localCache.file("%s-setup-intermediary-gen%d-client.tiny".formatted(minecraftVersion.client().id(), getIntermediaryGen()));
				}
			});
			this.serverIntermediaryMappingsFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer()) {
					throw new NoSuchFileException("server intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("server intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
				} else {
					return localCache.file("%s-setup-intermediary-gen%d-server.tiny".formatted(minecraftVersion.server().id(), getIntermediaryGen()));
				}
			});
			this.mergedIntermediaryMappingsFile = new Versioned<>(minecraftVersion -> localCache.file("%s-setup-intermediary-gen%d-merged.tiny".formatted(minecraftVersion.id(), getIntermediaryGen())));
			this.clientNamedMappingsFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient()) {
					throw new NoSuchFileException("client named mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("client named mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
				} else {
					return localCache.file("%s-setup-named-client.tiny".formatted(minecraftVersion.client().id()));
				}
			});
			this.serverNamedMappingsFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer()) {
					throw new NoSuchFileException("server named mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("server named mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
				} else {
					return localCache.file("%s-setup-named-server.tiny".formatted(minecraftVersion.server().id()));
				}
			});
			this.mergedNamedMappingsFile = new Versioned<>(minecraftVersion -> localCache.file("%s-setup-named-merged.tiny".formatted(minecraftVersion.id())));

			this.setupClientExceptionsFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient()) {
					throw new NoSuchFileException("client exceptions for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("client exceptions for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged exceptions!");
				} else {
					return localCache.file("%s-setup-client.excs".formatted(minecraftVersion.client().id()));
				}
			});
			this.setupServerExceptionsFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer()) {
					throw new NoSuchFileException("server exceptions for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("server exceptions for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged exceptions!");
				} else {
					return localCache.file("%s-setup-server.excs".formatted(minecraftVersion.server().id()));
				}
			});
			this.setupMergedExceptionsFile = new Versioned<>(minecraftVersion -> localCache.file("%s-setup-merged.excs".formatted(minecraftVersion.id())));
			this.setupClientSignaturesFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient()) {
					throw new NoSuchFileException("client signatures for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("client signatures for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged signatures!");
				} else {
					return localCache.file("%s-setup-client.sigs".formatted(minecraftVersion.client().id()));
				}
			});
			this.setupServerSignaturesFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer()) {
					throw new NoSuchFileException("server signatures for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("server signatures for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged signatures!");
				} else {
					return localCache.file("%s-setup-server.sigs".formatted(minecraftVersion.server().id()));
				}
			});
			this.setupMergedSignaturesFile = new Versioned<>(minecraftVersion -> localCache.file("%s-setup-merged.sigs".formatted(minecraftVersion.id())));
		}

		@Override
		public File getClientIntermediaryMappingsFile(MinecraftVersion minecraftVersion) {
			return clientIntermediaryMappingsFile.get(minecraftVersion);
		}

		@Override
		public File getServerIntermediaryMappingsFile(MinecraftVersion minecraftVersion) {
			return serverIntermediaryMappingsFile.get(minecraftVersion);
		}

		@Override
		public File getMergedIntermediaryMappingsFile(MinecraftVersion minecraftVersion) {
			return mergedIntermediaryMappingsFile.get(minecraftVersion);
		}

		@Override
		public File getClientNamedMappingsFile(MinecraftVersion minecraftVersion) {
			return clientNamedMappingsFile.get(minecraftVersion);
		}

		@Override
		public File getServerNamedMappingsFile(MinecraftVersion minecraftVersion) {
			return serverNamedMappingsFile.get(minecraftVersion);
		}

		@Override
		public File getMergedNamedMappingsFile(MinecraftVersion minecraftVersion) {
			return mergedNamedMappingsFile.get(minecraftVersion);
		}

		@Override
		public File getClientExceptionsFile(MinecraftVersion minecraftVersion) {
			return setupClientExceptionsFile.get(minecraftVersion);
		}

		@Override
		public File getServerExceptionsFile(MinecraftVersion minecraftVersion) {
			return setupServerExceptionsFile.get(minecraftVersion);
		}

		@Override
		public File getMergedExceptionsFile(MinecraftVersion minecraftVersion) {
			return setupMergedExceptionsFile.get(minecraftVersion);
		}

		@Override
		public File getClientSignaturesFile(MinecraftVersion minecraftVersion) {
			return setupClientSignaturesFile.get(minecraftVersion);
		}

		@Override
		public File getServerSignaturesFile(MinecraftVersion minecraftVersion) {
			return setupServerSignaturesFile.get(minecraftVersion);
		}

		@Override
		public File getMergedSignaturesFile(MinecraftVersion minecraftVersion) {
			return setupMergedSignaturesFile.get(minecraftVersion);
		}
	}

	public static class SourceJars extends FileContainer implements ExceptionsAndSignaturesDevelopmentFilesAccess.SourceJarsAccess {

		private final Versioned<MinecraftVersion, File> clientJar;
		private final Versioned<MinecraftVersion, File> serverJar;
		private final Versioned<MinecraftVersion, File> mergedJar;
		private final Versioned<MinecraftVersion, File> namedClientJar;
		private final Versioned<MinecraftVersion, File> namedServerJar;
		private final Versioned<MinecraftVersion, File> namedMergedJar;
		private final Versioned<MinecraftVersion, File> processedNamedJar;

		public SourceJars(KeratinGradleExtension keratin, LocalCache localCache) {
			super(keratin);

			this.clientJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient()) {
					throw new NoSuchFileException("client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
				} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("source client jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
				} else {
					return localCache.file("%s-source-client.jar".formatted(minecraftVersion.client().id()));
				}
			});
			this.serverJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer()) {
					throw new NoSuchFileException("server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
				} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("source server jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
				} else {
					return localCache.file("%s-source-server.jar".formatted(minecraftVersion.server().id()));
				}
			});
			this.mergedJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
					throw new NoSuchFileException("source jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
				} else {
					return localCache.file("%s-source-merged.jar".formatted(minecraftVersion.id()));
				}
			});
			this.namedClientJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient()) {
					throw new NoSuchFileException("client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
				} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("source client jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
				} else {
					return localCache.file("%s-named-source-client.jar".formatted(minecraftVersion.client().id()));
				}
			});
			this.namedServerJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer()) {
					throw new NoSuchFileException("server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
				} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("source server jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
				} else {
					return localCache.file("%s-named-source-server.jar".formatted(minecraftVersion.server().id()));
				}
			});
			this.namedMergedJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
					throw new NoSuchFileException("source jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
				} else {
					return localCache.file("%s-named-source-merged.jar".formatted(minecraftVersion.id()));
				}
			});
			this.processedNamedJar = new Versioned<>(minecraftVersion -> localCache.file("%s-processed-named-source.jar".formatted(minecraftVersion.id())));
		}

		@Override
		public File getClientJar(MinecraftVersion minecraftVersion) {
			return clientJar.get(minecraftVersion);
		}

		@Override
		public File getServerJar(MinecraftVersion minecraftVersion) {
			return serverJar.get(minecraftVersion);
		}

		@Override
		public File getMergedJar(MinecraftVersion minecraftVersion) {
			return mergedJar.get(minecraftVersion);
		}

		@Override
		public File getNamedClientJar(MinecraftVersion minecraftVersion) {
			return namedClientJar.get(minecraftVersion);
		}

		@Override
		public File getNamedServerJar(MinecraftVersion minecraftVersion) {
			return namedServerJar.get(minecraftVersion);
		}

		@Override
		public File getNamedMergedJar(MinecraftVersion minecraftVersion) {
			return namedMergedJar.get(minecraftVersion);
		}

		@Override
		public File getProcessedNamedJar(MinecraftVersion minecraftVersion) {
			return processedNamedJar.get(minecraftVersion);
		}
	}

	public static class SourceMappings extends FileContainer implements ExceptionsAndSignaturesDevelopmentFilesAccess.SourceMappingsAccess {

		private final Versioned<MinecraftVersion, File> clientMappingsFile;
		private final Versioned<MinecraftVersion, File> serverMappingsFile;
		private final Versioned<MinecraftVersion, File> mergedMappingsFile;

		public SourceMappings(KeratinGradleExtension keratin, LocalCache localCache) {
			super(keratin);

			this.clientMappingsFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient()) {
					throw new NoSuchFileException("client mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("client mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
				} else {
					return localCache.file("%s-source-client.tiny".formatted(minecraftVersion.client().id()));
				}
			});
			this.serverMappingsFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer()) {
					throw new NoSuchFileException("server mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("server mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
				} else {
					return localCache.file("%s-source-server.tiny".formatted(minecraftVersion.server().id()));
				}
			});
			this.mergedMappingsFile = new Versioned<>(minecraftVersion -> localCache.file("%s-source-merged.tiny".formatted(minecraftVersion.id())));
		}

		@Override
		public File getClientMappingsFile(MinecraftVersion minecraftVersion) {
			return clientMappingsFile.get(minecraftVersion);
		}

		@Override
		public File getServerMappingsFile(MinecraftVersion minecraftVersion) {
			return serverMappingsFile.get(minecraftVersion);
		}

		@Override
		public File getMergedMappingsFile(MinecraftVersion minecraftVersion) {
			return mergedMappingsFile.get(minecraftVersion);
		}
	}

	public static class BuildFiles extends FileContainer implements ExceptionsAndSignaturesDevelopmentFilesAccess.BuildFilesAccess {

		private final Versioned<MinecraftVersion, File> baseClientExceptionsFile;
		private final Versioned<MinecraftVersion, File> baseServerExceptionsFile;
		private final Versioned<MinecraftVersion, File> baseMergedExceptionsFile;
		private final Versioned<MinecraftVersion, File> baseClientSignaturesFile;
		private final Versioned<MinecraftVersion, File> baseServerSignaturesFile;
		private final Versioned<MinecraftVersion, File> baseMergedSignaturesFile;

		private final Versioned<MinecraftVersion, File> generatedClientJar;
		private final Versioned<MinecraftVersion, File> generatedServerJar;
		private final Versioned<MinecraftVersion, File> generatedMergedJar;
		private final Versioned<MinecraftVersion, File> namedGeneratedClientJar;
		private final Versioned<MinecraftVersion, File> namedGeneratedServerJar;
		private final Versioned<MinecraftVersion, File> namedGeneratedMergedJar;

		private final Versioned<MinecraftVersion, File> generatedClientExceptionsFile;
		private final Versioned<MinecraftVersion, File> generatedServerExceptionsFile;
		private final Versioned<MinecraftVersion, File> generatedMergedExceptionsFile;
		private final Versioned<MinecraftVersion, File> generatedClientSignaturesFile;
		private final Versioned<MinecraftVersion, File> generatedServerSignaturesFile;
		private final Versioned<MinecraftVersion, File> generatedMergedSignaturesFile;

		public BuildFiles(KeratinGradleExtension keratin, LocalCache localCache) {
			super(keratin);

			this.baseClientExceptionsFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient()) {
					throw new NoSuchFileException("client exceptions for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("client exceptions for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged exceptions!");
				} else {
					return localCache.file("%s-base-client.excs".formatted(minecraftVersion.client().id()));
				}
			});
			this.baseServerExceptionsFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer()) {
					throw new NoSuchFileException("server exceptions for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("server exceptions for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged exceptions!");
				} else {
					return localCache.file("%s-base-server.excs".formatted(minecraftVersion.server().id()));
				}
			});
			this.baseMergedExceptionsFile = new Versioned<>(minecraftVersion -> localCache.file("%s-base-merged.excs".formatted(minecraftVersion.id())));
			this.baseClientSignaturesFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient()) {
					throw new NoSuchFileException("client signatures for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("client signatures for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged signatures!");
				} else {
					return localCache.file("%s-base-client.sigs".formatted(minecraftVersion.client().id()));
				}
			});
			this.baseServerSignaturesFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer()) {
					throw new NoSuchFileException("server signatures for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("server signatures for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged signatures!");
				} else {
					return localCache.file("%s-base-server.sigs".formatted(minecraftVersion.server().id()));
				}
			});
			this.baseMergedSignaturesFile = new Versioned<>(minecraftVersion -> localCache.file("%s-base-merged.sigs".formatted(minecraftVersion.id())));

			this.generatedClientJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient()) {
					throw new NoSuchFileException("client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
				} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("generated client jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
				} else {
					return localCache.file("%s-generated-client.jar".formatted(minecraftVersion.client().id()));
				}
			});
			this.generatedServerJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer()) {
					throw new NoSuchFileException("server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
				} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("generated server jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
				} else {
					return localCache.file("%s-generated-server.jar".formatted(minecraftVersion.server().id()));
				}
			});
			this.generatedMergedJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
					throw new NoSuchFileException("generated jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
				} else {
					return localCache.file("%s-generated-merged.jar".formatted(minecraftVersion.id()));
				}
			});
			this.namedGeneratedClientJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient()) {
					throw new NoSuchFileException("client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
				} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("generated client jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
				} else {
					return localCache.file("%s-named-generated-client.jar".formatted(minecraftVersion.client().id()));
				}
			});
			this.namedGeneratedServerJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer()) {
					throw new NoSuchFileException("server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
				} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("generated server jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
				} else {
					return localCache.file("%s-named-generated-server.jar".formatted(minecraftVersion.server().id()));
				}
			});
			this.namedGeneratedMergedJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
					throw new NoSuchFileException("generated jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
				} else {
					return localCache.file("%s-named-generated-merged.jar".formatted(minecraftVersion.id()));
				}
			});

			this.generatedClientExceptionsFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient()) {
					throw new NoSuchFileException("client exceptions for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("client exceptions for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged exceptions!");
				} else {
					return localCache.file("%s-generated-client.excs".formatted(minecraftVersion.client().id()));
				}
			});
			this.generatedServerExceptionsFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer()) {
					throw new NoSuchFileException("server exceptions for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("server exceptions for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged exceptions!");
				} else {
					return localCache.file("%s-generated-server.excs".formatted(minecraftVersion.server().id()));
				}
			});
			this.generatedMergedExceptionsFile = new Versioned<>(minecraftVersion -> localCache.file("%s-generated-merged.excs".formatted(minecraftVersion.id())));
			this.generatedClientSignaturesFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient()) {
					throw new NoSuchFileException("client signatures for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("client signatures for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged signatures!");
				} else {
					return localCache.file("%s-generated-client.sigs".formatted(minecraftVersion.client().id()));
				}
			});
			this.generatedServerSignaturesFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer()) {
					throw new NoSuchFileException("server signatures for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("server signatures for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged signatures!");
				} else {
					return localCache.file("%s-generated-server.sigs".formatted(minecraftVersion.server().id()));
				}
			});
			this.generatedMergedSignaturesFile = new Versioned<>(minecraftVersion -> localCache.file("%s-generated-merged.sigs".formatted(minecraftVersion.id())));
		}

		@Override
		public File getBaseClientExceptionsFile(MinecraftVersion minecraftVersion) {
			return baseClientExceptionsFile.get(minecraftVersion);
		}

		@Override
		public File getBaseServerExceptionsFile(MinecraftVersion minecraftVersion) {
			return baseServerExceptionsFile.get(minecraftVersion);
		}

		@Override
		public File getBaseMergedExceptionsFile(MinecraftVersion minecraftVersion) {
			return baseMergedExceptionsFile.get(minecraftVersion);
		}

		@Override
		public File getBaseClientSignaturesFile(MinecraftVersion minecraftVersion) {
			return baseClientSignaturesFile.get(minecraftVersion);
		}

		@Override
		public File getBaseServerSignaturesFile(MinecraftVersion minecraftVersion) {
			return baseServerSignaturesFile.get(minecraftVersion);
		}

		@Override
		public File getBaseMergedSignaturesFile(MinecraftVersion minecraftVersion) {
			return baseMergedSignaturesFile.get(minecraftVersion);
		}

		@Override
		public File getGeneratedClientJar(MinecraftVersion minecraftVersion) {
			return generatedClientJar.get(minecraftVersion);
		}

		@Override
		public File getGeneratedServerJar(MinecraftVersion minecraftVersion) {
			return generatedServerJar.get(minecraftVersion);
		}

		@Override
		public File getGeneratedMergedJar(MinecraftVersion minecraftVersion) {
			return generatedMergedJar.get(minecraftVersion);
		}

		@Override
		public File getNamedGeneratedClientJar(MinecraftVersion minecraftVersion) {
			return namedGeneratedClientJar.get(minecraftVersion);
		}

		@Override
		public File getNamedGeneratedServerJar(MinecraftVersion minecraftVersion) {
			return namedGeneratedServerJar.get(minecraftVersion);
		}

		@Override
		public File getNamedGeneratedMergedJar(MinecraftVersion minecraftVersion) {
			return namedGeneratedMergedJar.get(minecraftVersion);
		}

		@Override
		public File getGeneratedClientExceptionsFile(MinecraftVersion minecraftVersion) {
			return generatedClientExceptionsFile.get(minecraftVersion);
		}

		@Override
		public File getGeneratedServerExceptionsFile(MinecraftVersion minecraftVersion) {
			return generatedServerExceptionsFile.get(minecraftVersion);
		}

		@Override
		public File getGeneratedMergedExceptionsFile(MinecraftVersion minecraftVersion) {
			return generatedMergedExceptionsFile.get(minecraftVersion);
		}

		@Override
		public File getGeneratedClientSignaturesFile(MinecraftVersion minecraftVersion) {
			return generatedClientSignaturesFile.get(minecraftVersion);
		}

		@Override
		public File getGeneratedServerSignaturesFile(MinecraftVersion minecraftVersion) {
			return generatedServerSignaturesFile.get(minecraftVersion);
		}

		@Override
		public File getGeneratedMergedSignaturesFile(MinecraftVersion minecraftVersion) {
			return generatedMergedSignaturesFile.get(minecraftVersion);
		}
	}
}
