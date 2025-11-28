package net.ornithemc.keratin.files;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import net.ornithemc.keratin.Configurations;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.files.GlobalCacheAccess;
import net.ornithemc.keratin.api.settings.BuildNumbers;
import net.ornithemc.keratin.api.settings.ProcessorSettings;

public class GlobalCache extends FileContainer implements FileCache, GlobalCacheAccess {

	private final Set<FileCache> caches;

	private final MetadataCache metadataCache;
	private final GameJarsCache  gameJarsCache;
	private final MappedJarsCache mappedJarsCache;
	private final ProcessedJarsCache processedJarsCache;
	private final MappingsCache mappingsCache;
	private final ExceptionsCache exceptionsCache;
	private final SignaturesCache signaturesCache;
	private final NestsCache nestsCache;
	private final LibrariesCache librariesCache;

	public GlobalCache(KeratinGradleExtension keratin, KeratinFiles files) {
		super(keratin, files);

		this.caches = new LinkedHashSet<>();

		this.metadataCache = addCache(new MetadataCache(keratin, files));
		this.gameJarsCache = addCache(new GameJarsCache(keratin, files));
		this.mappedJarsCache = addCache(new MappedJarsCache(keratin, files));
		this.processedJarsCache = addCache(new ProcessedJarsCache(keratin, files));
		this.mappingsCache = addCache(new MappingsCache(keratin, files));
		this.exceptionsCache = addCache(new ExceptionsCache(keratin, files));
		this.signaturesCache = addCache(new SignaturesCache(keratin, files));
		this.nestsCache = addCache(new NestsCache(keratin, files));
		this.librariesCache = new LibrariesCache(keratin, files);
	}

	private <C extends FileCache> C addCache(C cache) {
		caches.add(cache);
		return cache;
	}

	@Override
	public void mkdirs() throws IOException {
		for (FileCache cache : caches) {
			mkdirs(cache.getDirectory());
		}
	}

	@Override
	public File getDirectory() {
		return new File(project.getGradle().getGradleUserHomeDir(), "caches/%s/gen%d/".formatted(keratin.getGlobalCacheDirectory().get(), keratin.getIntermediaryGen().get()));
	}

	@Override
	public File getVersionsManifestJson() {
		return file("versions-manifest.json");
	}

	@Override
	public MetadataCache getMetadataCache() {
		return metadataCache;
	}

	@Override
	public GameJarsCache getGameJarsCache() {
		return gameJarsCache;
	}

	@Override
	public MappedJarsCache getMappedJarsCache() {
		return mappedJarsCache;
	}

	@Override
	public ProcessedJarsCache getProcessedJarsCache() {
		return processedJarsCache;
	}

	@Override
	public MappingsCache getMappingsCache() {
		return mappingsCache;
	}

	@Override
	public ExceptionsCache getExceptionsCache() {
		return exceptionsCache;
	}

	@Override
	public SignaturesCache getSignaturesCache() {
		return signaturesCache;
	}

	@Override
	public NestsCache getNestsCache() {
		return nestsCache;
	}

	@Override
	public LibrariesCache getLibrariesCache() {
		return librariesCache;
	}

	public static class MetadataCache extends FileContainer implements FileCache, GlobalCacheAccess.MetadataCacheAccess {

		public MetadataCache(KeratinGradleExtension keratin, KeratinFiles files) {
			super(keratin, files);
		}

		@Override
		public File getDirectory() {
			return files.getGlobalCache().file("metadata");
		}

		@Override
		public File getVersionInfoJson(String minecraftVersion) {
			return file("%s-info.json".formatted(minecraftVersion));
		}

		@Override
		public File getVersionDetailJson(String minecraftVersion) {
			return file("%s-details.json".formatted(minecraftVersion));
		}
	}

	public static class GameJarsCache extends FileContainer implements FileCache, GlobalCacheAccess.GameJarsCacheAccess {

		public GameJarsCache(KeratinGradleExtension keratin, KeratinFiles files) {
			super(keratin, files);
		}

		@Override
		public File getDirectory() {
			return files.getGlobalCache().file("game-jars");
		}

		@Override
		public File getClientJar(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasClient()) {
				throw new IllegalArgumentException("client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else {
				return file("%s-client.jar".formatted(minecraftVersion.client().id()));
			}
		}

		@Override
		public File getServerJar(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else {
				return file("%s-server.jar".formatted(minecraftVersion.server().id()));
			}
		}

		@Override
		public File getServerZip(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasServerZip()) {
				throw new IllegalArgumentException("server zip for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else {
				return file("%s-server.zip".formatted(minecraftVersion.server().id()));
			}
		}

		@Override
		public File getMergedJar(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("game jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
			} else if (!minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("game jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: the client and server jars do not have shared mappings!");
			} else {
				return file("%s-merged.jar".formatted(minecraftVersion.id()));
			}
		}
	}

	public static class MappedJarsCache extends FileContainer implements FileCache, GlobalCacheAccess.MappedJarsCacheAccess {

		public MappedJarsCache(KeratinGradleExtension keratin, KeratinFiles files) {
			super(keratin, files);
		}

		@Override
		public File getDirectory() {
			return files.getGlobalCache().file("mapped-jars");
		}

		@Override
		public File getIntermediaryClientJar(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasClient()) {
				throw new IllegalArgumentException("client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("intermediray client jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
			} else {
				return file("%s-intermediary-client.jar".formatted(minecraftVersion.client().id()));
			}
		}

		@Override
		public File getIntermediaryServerJar(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("intermediray server jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
			} else {
				return file("%s-intermediary-server.jar".formatted(minecraftVersion.server().id()));
			}
		}

		@Override
		public File getIntermediaryMergedJar(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("intermediary jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
			} else {
				return file("%s-intermediary-merged.jar".formatted(minecraftVersion.id()));
			}
		}

		@Override
		public File getMainIntermediaryJar(MinecraftVersion minecraftVersion) {
			return pickFileForPresentSides(minecraftVersion, this::getIntermediaryClientJar, this::getIntermediaryServerJar, this::getIntermediaryMergedJar);
		}
	}

	public static class ProcessedJarsCache extends FileContainer implements FileCache, GlobalCacheAccess.ProcessedJarsCacheAccess {

		public ProcessedJarsCache(KeratinGradleExtension keratin, KeratinFiles files) {
			super(keratin, files);
		}

		@Override
		public File getDirectory() {
			return files.getGlobalCache().file("processed-jars");
		}

		@Override
		public File getProcessedIntermediaryJar(MinecraftVersion minecraftVersion, ProcessorSettings processorSettings) {
			return file("%s-processed-intermediary-%s.jar".formatted(minecraftVersion.id(), Integer.toHexString(processorSettings.hashCode())));
		}
	}

	public static class MappingsCache extends FileContainer implements FileCache, GlobalCacheAccess.MappingsCacheAccess {

		public MappingsCache(KeratinGradleExtension keratin, KeratinFiles files) {
			super(keratin, files);
		}

		@Override
		public File getDirectory() {
			return files.getGlobalCache().file("mappings");
		}

		@Override
		public File getClientIntermediaryMappingsJar(MinecraftVersion minecraftVersion) {
			return file("%s-intermediary-client.jar".formatted(minecraftVersion.client().id()));
		}

		@Override
		public File getServerIntermediaryMappingsJar(MinecraftVersion minecraftVersion) {
			return file("%s-intermediary-server.jar".formatted(minecraftVersion.server().id()));
		}

		@Override
		public File getMergedIntermediaryMappingsJar(MinecraftVersion minecraftVersion) {
			return file("%s-intermediary-merged.jar".formatted(minecraftVersion.id()));
		}

		@Override
		public File getClientIntermediaryMappingsFile(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasClient()) {
				throw new IllegalArgumentException("client intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("client intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
			} else {
				return file("%s-intermediary-client.tiny".formatted(minecraftVersion.client().id()));
			}
		}

		@Override
		public File getServerIntermediaryMappingsFile(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("server intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("server intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
			} else {
				return file("%s-intermediary-server.tiny".formatted(minecraftVersion.server().id()));
			}
		}

		@Override
		public File getMergedIntermediaryMappingsFile(MinecraftVersion minecraftVersion) {
			return file("%s-intermediary-merged.tiny".formatted(minecraftVersion.id()));
		}

		@Override
		public File getMainIntermediaryMappingsFile(MinecraftVersion minecraftVersion) {
			return pickFileForPresentSides(minecraftVersion, this::getClientIntermediaryMappingsFile, this::getServerIntermediaryMappingsFile, this::getMergedIntermediaryMappingsFile);
		}

		@Override
		public File getFilledClientIntermediaryMappingsFile(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasClient()) {
				throw new IllegalArgumentException("client intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("client intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
			} else {
				return file("%s-filled-intermediary-client.tiny".formatted(minecraftVersion.client().id()));
			}
		}

		@Override
		public File getFilledServerIntermediaryMappingsFile(MinecraftVersion minecraftVersion) {
			if (!minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("server intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("server intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
			} else {
				return file("%s-filled-intermediary-server.tiny".formatted(minecraftVersion.server().id()));
			}
		}

		@Override
		public File getFilledMergedIntermediaryMappingsFile(MinecraftVersion minecraftVersion) {
			return file("%s-filled-intermediary-merged.tiny".formatted(minecraftVersion.id()));
		}

		@Override
		public File getMainFilledIntermediaryMappingsFile(MinecraftVersion minecraftVersion) {
			return pickFileForPresentSides(minecraftVersion, this::getFilledClientIntermediaryMappingsFile, this::getFilledServerIntermediaryMappingsFile, this::getFilledMergedIntermediaryMappingsFile);
		}

		@Override
		public File getNamedMappingsJar(String minecraftVersion, int build) {
			return file("%s-named+build.%d.jar".formatted(minecraftVersion, build));
		}

		@Override
		public File getNamedMappingsFile(String minecraftVersion, int build) {
			return file("%s-named+build.%d.tiny".formatted(minecraftVersion, build));
		}
	}

	public static class ExceptionsCache extends FileContainer implements FileCache, GlobalCacheAccess.ExceptionsCacheAccess {

		public ExceptionsCache(KeratinGradleExtension keratin, KeratinFiles files) {
			super(keratin, files);
		}

		@Override
		public File getDirectory() {
			return files.getGlobalCache().file("exceptions");
		}

		@Override
		public File getClientExceptionsJar(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			return file("%s-exceptions+build.%d-client.jar".formatted(minecraftVersion.client().id(), builds.client()));
		}

		@Override
		public File getServerExceptionsJar(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			return file("%s-exceptions+build.%d-server.jar".formatted(minecraftVersion.server().id(), builds.server()));
		}

		@Override
		public File getMergedExceptionsJar(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			return file("%s-exceptions+build.%d-merged.jar".formatted(minecraftVersion.id(), builds.merged()));
		}

		@Override
		public File getClientExceptionsFile(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			if (!minecraftVersion.hasClient()) {
				throw new IllegalArgumentException("client exceptions for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("client exceptions for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged exceptions!");
			} else {
				if (builds.client() < 1) {
					return null;
				} else {
					return file("%s-exceptions+build.%d-client.excs".formatted(minecraftVersion.client().id(), builds.client()));
				}
			}
		}

		@Override
		public File getServerExceptionsFile(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			if (!minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("server exceptions for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("server exceptions for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged exceptions!");
			} else {
				if (builds.server() < 1) {
					return null;
				} else {
					return file("%s-exceptions+build.%d-server.excs".formatted(minecraftVersion.server().id(), builds.server()));
				}
			}
		}

		@Override
		public File getMergedExceptionsFile(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			if (!minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("exceptions for Minecraft version " + minecraftVersion.id() + " cannot be merged: the client and server exceptions do not have shared mappings!");
			} else {
				if (builds.merged() < 1) {
					return null;
				} else {
					return file("%s-exceptions+build.%d-merged.excs".formatted(minecraftVersion.id(), builds.merged()));
				}
			}
		}

		@Override
		public File getIntermediaryClientExceptionsFile(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			if (!minecraftVersion.hasClient()) {
				throw new IllegalArgumentException("client exceptions for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("intermediary client exceptions for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged exceptions!");
			} else {
				if (builds.client() < 1) {
					return null;
				} else {
					return file("%s-intermediary-exceptions+build.%d-client.excs".formatted(minecraftVersion.client().id(), builds.client()));
				}
			}
		}

		@Override
		public File getIntermediaryServerExceptionsFile(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			if (!minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("server exceptions for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("intermediary server exceptions for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged exceptions!");
			} else {
				if (builds.server() < 1) {
					return null;
				} else {
					return file("%s-intermediary-exceptions+build.%d-server.excs".formatted(minecraftVersion.server().id(), builds.server()));
				}
			}
		}

		@Override
		public File getIntermediaryMergedExceptionsFile(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("intermediary exceptions for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server exceptions do not exist!");
			} else {
				if (builds.merged() < 1) {
					if (builds.client() < 1 || builds.server() < 1) {
						return null;
					} else {
						return file("%s-intermediary-exceptions+build.(%d-%d)-merged.excs".formatted(minecraftVersion.id(), builds.client(), builds.server()));
					}
				} else {
					return file("%s-intermediary-exceptions+build.%d-merged.excs".formatted(minecraftVersion.id(), builds.merged()));
				}
			}
		}

		@Override
		public File getMainIntermediaryExceptionsFile(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			return pickFileForPresentSides(minecraftVersion, v -> getIntermediaryClientExceptionsFile(v, builds), v -> getIntermediaryServerExceptionsFile(v, builds), v -> getIntermediaryMergedExceptionsFile(v, builds));
		}
	}

	public static class SignaturesCache extends FileContainer implements FileCache, GlobalCacheAccess.SignaturesCacheAccess {

		public SignaturesCache(KeratinGradleExtension keratin, KeratinFiles files) {
			super(keratin, files);
		}

		@Override
		public File getDirectory() {
			return files.getGlobalCache().file("signatures");
		}

		@Override
		public File getClientSignaturesJar(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			return file("%s-signatures+build.%d-client.jar".formatted(minecraftVersion.client().id(), builds.client()));
		}

		@Override
		public File getServerSignaturesJar(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			return file("%s-signatures+build.%d-server.jar".formatted(minecraftVersion.server().id(), builds.server()));
		}

		@Override
		public File getMergedSignaturesJar(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			return file("%s-signatures+build.%d-merged.jar".formatted(minecraftVersion.id(), builds.merged()));
		}

		@Override
		public File getClientSignaturesFile(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			if (!minecraftVersion.hasClient()) {
				throw new IllegalArgumentException("client signatures for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("client signatures for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged signatures!");
			} else {
				if (builds.client() < 1) {
					return null;
				} else {
					return file("%s-signatures+build.%d-client.sigs".formatted(minecraftVersion.client().id(), builds.client()));
				}
			}
		}

		@Override
		public File getServerSignaturesFile(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			if (!minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("server signatures for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("server signatures for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged signatures!");
			} else {
				if (builds.server() < 1) {
					return null;
				} else {
					return file("%s-signatures+build.%d-server.sigs".formatted(minecraftVersion.server().id(), builds.server()));
				}
			}
		}

		@Override
		public File getMergedSignaturesFile(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			if (!minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("signatures for Minecraft version " + minecraftVersion.id() + " cannot be merged: the client and server signatures do not have shared mappings!");
			} else {
				if (builds.merged() < 1) {
					return null;
				} else {
					return file("%s-signatures+build.%d-merged.sigs".formatted(minecraftVersion.id(), builds.merged()));
				}
			}
		}

		@Override
		public File getIntermediaryClientSignaturesFile(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			if (!minecraftVersion.hasClient()) {
				throw new IllegalArgumentException("client signatures for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("intermediary client signatures for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged signatures!");
			} else {
				if (builds.client() < 1) {
					return null;
				} else {
					return file("%s-intermediary-signatures+build.%d-client.sigs".formatted(minecraftVersion.client().id(), builds.client()));
				}
			}
		}

		@Override
		public File getIntermediaryServerSignaturesFile(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			if (!minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("server signatures for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("intermediary server signatures for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged signatures!");
			} else {
				if (builds.server() < 1) {
					return null;
				} else {
					return file("%s-intermediary-signatures+build.%d-server.sigs".formatted(minecraftVersion.server().id(), builds.server()));
				}
			}
		}

		@Override
		public File getIntermediaryMergedSignaturesFile(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("intermediary signatures for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server signatures do not exist!");
			} else {
				if (builds.merged() < 1) {
					if (builds.client() < 1 || builds.server() < 1) {
						return null;
					} else {
						return file("%s-intermediary-signatures+build.(%d-%d)-merged.sigs".formatted(minecraftVersion.id(), builds.client(), builds.server()));
					}
				} else {
					return file("%s-intermediary-signatures+build.%d-merged.sigs".formatted(minecraftVersion.id(), builds.merged()));
				}
			}
		}

		@Override
		public File getMainIntermediarySignaturesFile(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			return pickFileForPresentSides(minecraftVersion, v -> getIntermediaryClientSignaturesFile(v, builds), v -> getIntermediaryServerSignaturesFile(v, builds), v -> getIntermediaryMergedSignaturesFile(v, builds));
		}
	}

	public static class NestsCache extends FileContainer implements FileCache, GlobalCacheAccess.NestsCacheAccess {

		public NestsCache(KeratinGradleExtension keratin, KeratinFiles files) {
			super(keratin, files);
		}

		@Override
		public File getDirectory() {
			return files.getGlobalCache().file("nests");
		}

		@Override
		public File getClientNestsJar(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			return file("%s-nests+build.%d-client.jar".formatted(minecraftVersion.client().id(), builds.client()));
		}

		@Override
		public File getServerNestsJar(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			return file("%s-nests+build.%d-server.jar".formatted(minecraftVersion.server().id(), builds.server()));
		}

		@Override
		public File getMergedNestsJar(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			return file("%s-nests+build.%d-merged.jar".formatted(minecraftVersion.id(), builds.merged()));
		}

		@Override
		public File getClientNestsFile(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			if (!minecraftVersion.hasClient()) {
				throw new IllegalArgumentException("client nests for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("client nests for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged nests!");
			} else {
				if (builds.client() < 1) {
					return null;
				} else {
					return file("%s-nests+build.%d-client.nests".formatted(minecraftVersion.client().id(), builds.client()));
				}
			}
		}

		@Override
		public File getServerNestsFile(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			if (!minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("server nests for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("server nests for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged nests!");
			} else {
				if (builds.server() < 1) {
					return null;
				} else {
					return file("%s-nests+build.%d-server.nests".formatted(minecraftVersion.server().id(), builds.server()));
				}
			}
		}

		@Override
		public File getMergedNestsFile(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			if (!minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("nests for Minecraft version " + minecraftVersion.id() + " cannot be merged: the client and server nests do not have shared mappings!");
			} else {
				if (builds.merged() < 1) {
					return null;
				} else {
					return file("%s-nests+build.%d-merged.nests".formatted(minecraftVersion.id(), builds.merged()));
				}
			}
		}

		@Override
		public File getIntermediaryClientNestsFile(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			if (!minecraftVersion.hasClient()) {
				throw new IllegalArgumentException("client nests for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("intermediary client nests for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged nests!");
			} else {
				if (builds.client() < 1) {
					return null;
				} else {
					return file("%s-intermediary-nests+build.%d-client.nests".formatted(minecraftVersion.client().id(), builds.client()));
				}
			}
		}

		@Override
		public File getIntermediaryServerNestsFile(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			if (!minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("server nests for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new IllegalArgumentException("intermediary server nests for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged nests!");
			} else {
				if (builds.server() < 1) {
					return null;
				} else {
					return file("%s-intermediary-nests+build.%d-server.nests".formatted(minecraftVersion.server().id(), builds.server()));
				}
			}
		}

		@Override
		public File getIntermediaryMergedNestsFile(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
				throw new IllegalArgumentException("intermediary nests for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server nests do not exist!");
			} else {
				if (builds.merged() < 1) {
					if (builds.client() < 1 || builds.server() < 1) {
						return null;
					} else {
						return file("%s-intermediary-nests+build.(%d-%d)-merged.nests".formatted(minecraftVersion.id(), builds.client(), builds.server()));
					}
				} else {
					return file("%s-intermediary-nests+build.%d-merged.nests".formatted(minecraftVersion.id(), builds.merged()));
				}
			}
		}

		@Override
		public File getMainIntermediaryNestsFile(MinecraftVersion minecraftVersion, BuildNumbers builds) {
			return pickFileForPresentSides(minecraftVersion, v -> getIntermediaryClientNestsFile(v, builds), v -> getIntermediaryServerNestsFile(v, builds), v -> getIntermediaryMergedNestsFile(v, builds));
		}
	}

	public static class LibrariesCache extends FileContainer implements GlobalCacheAccess.LibrariesCacheAccess {

		public LibrariesCache(KeratinGradleExtension keratin, KeratinFiles files) {
			super(keratin, files);
		}

		@Override
		public Collection<File> getLibraries(String minecraftVersion) {
			return project.getConfigurations().getByName(Configurations.minecraftLibraries(minecraftVersion)).getFiles();
		}

		@Override
		public Collection<File> getLibraries(MinecraftVersion minecraftVersion) {
			Set<File> libraries = new LinkedHashSet<>();

			if (minecraftVersion.hasClient()) {
				libraries.addAll(getLibraries(minecraftVersion.client().id()));
			}
			if (minecraftVersion.hasServer()) {
				libraries.addAll(getLibraries(minecraftVersion.server().id()));
			}

			return libraries;
		}
	}
}
