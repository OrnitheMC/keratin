package net.ornithemc.keratin.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.gradle.api.provider.Property;

import net.ornithemc.keratin.Configurations;
import net.ornithemc.keratin.Constants;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.files.GlobalCacheAccess;
import net.ornithemc.keratin.util.Versioned;

public class GlobalCache extends FileContainer implements FileCache, GlobalCacheAccess {

	private final Set<FileCache> caches;

	private final Property<File> dir;
	private final Property<File> versionsManifest;

	private final MetadataCache metadataCache;
	private final GameJarsCache gameJarsCache;
	private final MappedJarsCache mappedJarsCache;
	private final ProcessedJarsCache processedJarsCache;
	private final MappingsCache mappingsCache;
	private final ExceptionsCache exceptionsCache;
	private final SignaturesCache signaturesCache;
	private final NestsCache nestsCache;
	private final LibrariesCache librariesCache;

	public GlobalCache(KeratinGradleExtension keratin) {
		super(keratin);

		this.caches = new LinkedHashSet<>();

		this.dir = fileProperty(() -> new File(this.project.getGradle().getGradleUserHomeDir(), "caches/%s".formatted(keratin.getGlobalCacheDirectory().get())));
		this.versionsManifest = fileProperty(() -> {
			String url = keratin.getVersionsManifestUrl().get();

			if (Constants.VERSIONS_MANIFEST_URL.equals(url)) {
				return file("versions-manifest.json");
			} else {
				return file("versions-manifest-" + Integer.toHexString(url.hashCode()) + ".json");
			}
		});

		this.metadataCache = addCache(new MetadataCache(keratin, this));
		this.gameJarsCache = addCache(new GameJarsCache(keratin, this));
		this.mappedJarsCache = addCache(new MappedJarsCache(keratin, this));
		this.processedJarsCache = addCache(new ProcessedJarsCache(keratin, this));
		this.mappingsCache = addCache(new MappingsCache(keratin, this));
		this.exceptionsCache = addCache(new ExceptionsCache(keratin, this));
		this.signaturesCache = addCache(new SignaturesCache(keratin, this));
		this.nestsCache = addCache(new NestsCache(keratin, this));
		this.librariesCache = addCache(new LibrariesCache(keratin, this));
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
		return dir.get();
	}

	@Override
	public File getVersionsManifest() {
		return versionsManifest.get();
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

		private final Property<File> dir;

		private final Versioned<String, File> versionInfoJsons;
		private final Versioned<String, File> versionDetailJsons;

		public MetadataCache(KeratinGradleExtension keratin, GlobalCache globalCache) {
			super(keratin);

			this.dir = fileProperty(() -> globalCache.file("version-jsons"));

			this.versionInfoJsons = new Versioned<>(minecraftVersion -> file("%s-info.json".formatted(minecraftVersion)));
			this.versionDetailJsons = new Versioned<>(minecraftVersion -> file("%s-details.json".formatted(minecraftVersion)));
		}

		@Override
		public File getDirectory() {
			return dir.get();
		}

		@Override
		public File getVersionInfoJson(String minecraftVersion) {
			return versionInfoJsons.get(minecraftVersion);
		}

		@Override
		public File getVersionDetailJsons(String minecraftVersion) {
			return versionDetailJsons.get(minecraftVersion);
		}
	}

	public static class GameJarsCache extends FileContainer implements FileCache, GlobalCacheAccess.GameJarsCacheAccess {

		private final Property<File> dir;

		private final Versioned<MinecraftVersion, File> clientJar;
		private final Versioned<MinecraftVersion, File> serverJar;
		private final Versioned<MinecraftVersion, File> mergedJar;

		public GameJarsCache(KeratinGradleExtension keratin, GlobalCache globalCache) {
			super(keratin);

			this.dir = fileProperty(() -> globalCache.file("game-jars"));

			this.clientJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient()) {
					throw new NoSuchFileException("client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
				} else {
					return file("%s-client.jar".formatted(minecraftVersion.client().id()));
				}
			});
			this.serverJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer()) {
					throw new NoSuchFileException("server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
				} else {
					return file("%s-server.jar".formatted(minecraftVersion.server().id()));
				}
			});
			this.mergedJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
					throw new NoSuchFileException("game jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
				} else if (!minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("game jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: the client and server jars do not have shared mappings!");
				} else {
					return file("%s-merged.jar".formatted(minecraftVersion.id()));
				}
			});
		}

		@Override
		public File getDirectory() {
			return dir.get();
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
	}

	public static class MappedJarsCache extends FileContainer implements FileCache, GlobalCacheAccess.MappedJarsCacheAccess {

		private final Property<File> dir;

		private final Versioned<MinecraftVersion, File> intermediaryClientJar;
		private final Versioned<MinecraftVersion, File> intermediaryServerJar;
		private final Versioned<MinecraftVersion, File> intermediaryMergedJar;

		public MappedJarsCache(KeratinGradleExtension keratin, GlobalCache globalCache) {
			super(keratin);

			this.dir = fileProperty(() -> globalCache.file("mapped-jars"));

			this.intermediaryClientJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient()) {
					throw new NoSuchFileException("client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
				} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("intermediray client jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
				} else {
					return file("%s-intermediary-gen%d-client.jar".formatted(minecraftVersion.client().id(), getIntermediaryGen()));
				}
			});
			this.intermediaryServerJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer()) {
					throw new NoSuchFileException("server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
				} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("intermediray server jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
				} else {
					return file("%s-intermediary-gen%d-server.jar".formatted(minecraftVersion.server().id(), getIntermediaryGen()));
				}
			});
			this.intermediaryMergedJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
					throw new NoSuchFileException("intermediary jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
				} else {
					return file("%s-intermediary-gen%d-merged.jar".formatted(minecraftVersion.id(), getIntermediaryGen()));
				}
			});
		}

		@Override
		public File getDirectory() {
			return dir.get();
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

		@Override
		public File getMainIntermediaryJar(MinecraftVersion minecraftVersion) {
			return pickFileForPresentSides(minecraftVersion, intermediaryClientJar, intermediaryServerJar, intermediaryMergedJar);
		}
	}

	public static class ProcessedJarsCache extends FileContainer implements FileCache, GlobalCacheAccess.ProcessedJarsCacheAccess {

		private final Property<File> dir;

		private final Versioned<MinecraftVersion, File> lvtPatchedIntermediaryClientJar;
		private final Versioned<MinecraftVersion, File> lvtPatchedIntermediaryServerJar;
		private final Versioned<MinecraftVersion, File> lvtPatchedIntermediaryMergedJar;
		private final Versioned<MinecraftVersion, File> exceptionsPatchedIntermediaryClientJar;
		private final Versioned<MinecraftVersion, File> exceptionsPatchedIntermediaryServerJar;
		private final Versioned<MinecraftVersion, File> exceptionsPatchedIntermediaryMergedJar;
		private final Versioned<MinecraftVersion, File> signaturePatchedIntermediaryClientJar;
		private final Versioned<MinecraftVersion, File> signaturePatchedIntermediaryServerJar;
		private final Versioned<MinecraftVersion, File> signaturePatchedIntermediaryMergedJar;
		private final Versioned<MinecraftVersion, File> preenedIntermediaryClientJar;
		private final Versioned<MinecraftVersion, File> preenedIntermediaryServerJar;
		private final Versioned<MinecraftVersion, File> preenedIntermediaryMergedJar;
		private final Versioned<MinecraftVersion, File> nestedIntermediaryClientJar;
		private final Versioned<MinecraftVersion, File> nestedIntermediaryServerJar;
		private final Versioned<MinecraftVersion, File> nestedIntermediaryMergedJar;
		private final Versioned<MinecraftVersion, File> processedIntermediaryClientJar;
		private final Versioned<MinecraftVersion, File> processedIntermediaryServerJar;
		private final Versioned<MinecraftVersion, File> processedIntermediaryMergedJar;

		public ProcessedJarsCache(KeratinGradleExtension keratin, GlobalCache globalCache) {
			super(keratin);

			this.dir = fileProperty(() -> globalCache.file("processed-jars"));

			this.lvtPatchedIntermediaryClientJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient() || minecraftVersion.hasServer()) {
					throw new NoSuchFileException("intermediary client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
				} else {
					return file("%s-lvt-patched-intermediary-gen%d-client.jar".formatted(minecraftVersion.client().id(), getIntermediaryGen()));
				}
			});
			this.lvtPatchedIntermediaryServerJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer() || minecraftVersion.hasClient()) {
					throw new NoSuchFileException("intermediary server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
				} else {
					return file("%s-lvt-patched-intermediary-gen%d-server.jar".formatted(minecraftVersion.server().id(), getIntermediaryGen()));
				}
			});
			this.lvtPatchedIntermediaryMergedJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
					throw new NoSuchFileException("intermediary jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
				} else {
					return file("%s-lvt-patched-intermediary-gen%d-merged.jar".formatted(minecraftVersion.id(), getIntermediaryGen()));
				}
			});
			this.exceptionsPatchedIntermediaryClientJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient() || minecraftVersion.hasServer()) {
					throw new NoSuchFileException("intermediary client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
				} else {
					int build = keratin.getExceptionsBuild(minecraftVersion, GameSide.CLIENT);

					if (build < 1) {
						return null;
					} else {
						return file("%s-exceptions+build.%d-intermediary-gen%d-client.jar".formatted(minecraftVersion.client().id(), build, getIntermediaryGen()));
					}
				}
			});
			this.exceptionsPatchedIntermediaryServerJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer() || minecraftVersion.hasClient()) {
					throw new NoSuchFileException("intermediary server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
				} else {
					int build = keratin.getExceptionsBuild(minecraftVersion, GameSide.SERVER);

					if (build < 1) {
						return null;
					} else {
						return file("%s-exceptions+build.%d-intermediary-gen%d-server.jar".formatted(minecraftVersion.server().id(), build, getIntermediaryGen()));
					}
				}
			});
			this.exceptionsPatchedIntermediaryMergedJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
					throw new NoSuchFileException("intermediary jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
				} else {
					int build = keratin.getExceptionsBuild(minecraftVersion, GameSide.MERGED);
					int clientBuild = keratin.getExceptionsBuild(minecraftVersion, GameSide.CLIENT);
					int serverBuild = keratin.getExceptionsBuild(minecraftVersion, GameSide.SERVER);

					if (build < 1) {
						if (clientBuild < 1 && serverBuild < 1) {
							return null;
						} else {
							return file("%s-exceptions+build.(%d-%d)-intermediary-gen%d-merged.jar".formatted(minecraftVersion.id(), clientBuild, serverBuild, getIntermediaryGen()));
						}
					} else {
						return file("%s-exceptions+build.%d-intermediary-gen%d-merged.jar".formatted(minecraftVersion.id(), build, getIntermediaryGen()));
					}
				}
			});
			this.signaturePatchedIntermediaryClientJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient() || minecraftVersion.hasServer()) {
					throw new NoSuchFileException("intermediary client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
				} else {
					int build = keratin.getSignaturesBuild(minecraftVersion, GameSide.CLIENT);

					if (build < 1) {
						return null;
					} else {
						return file("%s-signatures+build.%d-intermediary-gen%d-client.jar".formatted(minecraftVersion.client().id(), build, getIntermediaryGen()));
					}
				}
			});
			this.signaturePatchedIntermediaryServerJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer() || minecraftVersion.hasClient()) {
					throw new NoSuchFileException("intermediary server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
				} else {
					int build = keratin.getSignaturesBuild(minecraftVersion, GameSide.SERVER);

					if (build < 1) {
						return null;
					} else {
						return file("%s-signatures+build.%d-intermediary-gen%d-server.jar".formatted(minecraftVersion.server().id(), build, getIntermediaryGen()));
					}
				}
			});
			this.signaturePatchedIntermediaryMergedJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
					throw new NoSuchFileException("intermediary jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
				} else {
					int build = keratin.getSignaturesBuild(minecraftVersion, GameSide.MERGED);
					int clientBuild = keratin.getSignaturesBuild(minecraftVersion, GameSide.CLIENT);
					int serverBuild = keratin.getSignaturesBuild(minecraftVersion, GameSide.SERVER);

					if (build < 1) {
						if (clientBuild < 1 && serverBuild < 1) {
							return null;
						} else {
							return file("%s-signatures+build.(%d-%d)-intermediary-gen%d-merged.jar".formatted(minecraftVersion.id(), clientBuild, serverBuild, getIntermediaryGen()));
						}
					} else {
						return file("%s-signatures+build.%d-intermediary-gen%d-merged.jar".formatted(minecraftVersion.id(), build, getIntermediaryGen()));
					}
				}
			});
			this.preenedIntermediaryClientJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient() || minecraftVersion.hasServer()) {
					throw new NoSuchFileException("intermediary client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
				} else {
					return file("%s-preened-intermediary-gen%d-client.jar".formatted(minecraftVersion.client().id(), getIntermediaryGen()));
				}
			});
			this.preenedIntermediaryServerJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer() || minecraftVersion.hasClient()) {
					throw new NoSuchFileException("intermediary server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
				} else {
					return file("%s-preened-intermediary-gen%d-server.jar".formatted(minecraftVersion.server().id(), getIntermediaryGen()));
				}
			});
			this.preenedIntermediaryMergedJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
					throw new NoSuchFileException("intermediary jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
				} else {
					return file("%s-preened-intermediary-gen%d-merged.jar".formatted(minecraftVersion.id(), getIntermediaryGen()));
				}
			});
			this.nestedIntermediaryClientJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient() || minecraftVersion.hasServer()) {
					throw new NoSuchFileException("intermediary client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
				} else {
					int build = keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT);

					if (build < 1) {
						return null;
					} else {
						return file("%s-nests+build.%d-intermediary-gen%d-client.jar".formatted(minecraftVersion.client().id(), build, getIntermediaryGen()));
					}
				}
			});
			this.nestedIntermediaryServerJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer() || minecraftVersion.hasClient()) {
					throw new NoSuchFileException("intermediary server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
				} else {
					int build = keratin.getNestsBuild(minecraftVersion, GameSide.SERVER);

					if (build < 1) {
						return null;
					} else {
						return file("%s-nests+build.%d-intermediary-gen%d-server.jar".formatted(minecraftVersion.server().id(), build, getIntermediaryGen()));
					}
				}
			});
			this.nestedIntermediaryMergedJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
					throw new NoSuchFileException("intermediary jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
				} else {
					int build = keratin.getNestsBuild(minecraftVersion, GameSide.MERGED);
					int clientBuild = keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT);
					int serverBuild = keratin.getNestsBuild(minecraftVersion, GameSide.SERVER);

					if (build < 1) {
						if (clientBuild < 1 && serverBuild < 1) {
							return null;
						} else {
							return file("%s-nests+build.(%d-%d)-intermediary-gen%d-merged.jar".formatted(minecraftVersion.id(), clientBuild, serverBuild, getIntermediaryGen()));
						}
					} else {
						return file("%s-nests+build.%d-intermediary-gen%d-merged.jar".formatted(minecraftVersion.id(), build, getIntermediaryGen()));
					}
				}
			});
			this.processedIntermediaryClientJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient() || minecraftVersion.hasServer()) {
					throw new NoSuchFileException("intermediary client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
				} else {
					return file("%s-processed-intermediary-gen%d-client.jar".formatted(minecraftVersion.client().id(), getIntermediaryGen()));
				}
			});
			this.processedIntermediaryServerJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer() || minecraftVersion.hasClient()) {
					throw new NoSuchFileException("intermediary server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
				} else {
					return file("%s-processed-intermediary-gen%d-server.jar".formatted(minecraftVersion.server().id(), getIntermediaryGen()));
				}
			});
			this.processedIntermediaryMergedJar = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
					throw new NoSuchFileException("intermediary jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
				} else {
					return file("%s-processed-intermediary-gen%d-merged.jar".formatted(minecraftVersion.id(), getIntermediaryGen()));
				}
			});
		}

		@Override
		public File getDirectory() {
			return dir.get();
		}

		@Override
		public File getLvtPatchedIntermediaryClientJar(MinecraftVersion minecraftVersion) {
			return lvtPatchedIntermediaryClientJar.get(minecraftVersion);
		}

		@Override
		public File getLvtPatchedIntermediaryServerJar(MinecraftVersion minecraftVersion) {
			return lvtPatchedIntermediaryServerJar.get(minecraftVersion);
		}

		@Override
		public File getLvtPatchedIntermediaryMergedJar(MinecraftVersion minecraftVersion) {
			return lvtPatchedIntermediaryMergedJar.get(minecraftVersion);
		}

		@Override
		public File getMainLvtPatchedIntermediaryJar(MinecraftVersion minecraftVersion) {
			return pickFileForPresentSides(minecraftVersion, lvtPatchedIntermediaryClientJar, lvtPatchedIntermediaryServerJar, lvtPatchedIntermediaryMergedJar);
		}

		@Override
		public File getExceptionsPatchedIntermediaryClientJar(MinecraftVersion minecraftVersion) {
			return exceptionsPatchedIntermediaryClientJar.get(minecraftVersion);
		}

		@Override
		public File getExceptionsPatchedIntermediaryServerJar(MinecraftVersion minecraftVersion) {
			return exceptionsPatchedIntermediaryServerJar.get(minecraftVersion);
		}

		@Override
		public File getExceptionsPatchedIntermediaryMergedJar(MinecraftVersion minecraftVersion) {
			return exceptionsPatchedIntermediaryMergedJar.get(minecraftVersion);
		}

		@Override
		public File getMainExceptionsPatchedIntermediaryJar(MinecraftVersion minecraftVersion) {
			return pickFileForPresentSides(minecraftVersion, exceptionsPatchedIntermediaryClientJar, exceptionsPatchedIntermediaryServerJar, exceptionsPatchedIntermediaryMergedJar);
		}

		@Override
		public File getSignaturePatchedIntermediaryClientJar(MinecraftVersion minecraftVersion) {
			return signaturePatchedIntermediaryClientJar.get(minecraftVersion);
		}

		@Override
		public File getSignaturePatchedIntermediaryServerJar(MinecraftVersion minecraftVersion) {
			return signaturePatchedIntermediaryServerJar.get(minecraftVersion);
		}

		@Override
		public File getSignaturePatchedIntermediaryMergedJar(MinecraftVersion minecraftVersion) {
			return signaturePatchedIntermediaryMergedJar.get(minecraftVersion);
		}

		@Override
		public File getMainSignaturePatchedIntermediaryJar(MinecraftVersion minecraftVersion) {
			return pickFileForPresentSides(minecraftVersion, signaturePatchedIntermediaryClientJar, signaturePatchedIntermediaryServerJar, signaturePatchedIntermediaryMergedJar);
		}

		@Override
		public File getPreenedIntermediaryClientJar(MinecraftVersion minecraftVersion) {
			return preenedIntermediaryClientJar.get(minecraftVersion);
		}

		@Override
		public File getPreenedIntermediaryServerJar(MinecraftVersion minecraftVersion) {
			return preenedIntermediaryServerJar.get(minecraftVersion);
		}

		@Override
		public File getPreenedIntermediaryMergedJar(MinecraftVersion minecraftVersion) {
			return preenedIntermediaryMergedJar.get(minecraftVersion);
		}

		@Override
		public File getMainPreenedIntermediaryJar(MinecraftVersion minecraftVersion) {
			return pickFileForPresentSides(minecraftVersion, preenedIntermediaryClientJar, preenedIntermediaryServerJar, preenedIntermediaryMergedJar);
		}

		@Override
		public File getNestedIntermediaryClientJar(MinecraftVersion minecraftVersion) {
			return nestedIntermediaryClientJar.get(minecraftVersion);
		}

		@Override
		public File getNestedIntermediaryServerJar(MinecraftVersion minecraftVersion) {
			return nestedIntermediaryServerJar.get(minecraftVersion);
		}

		@Override
		public File getNestedIntermediaryMergedJar(MinecraftVersion minecraftVersion) {
			return nestedIntermediaryMergedJar.get(minecraftVersion);
		}

		@Override
		public File getMainNestedIntermediaryJar(MinecraftVersion minecraftVersion) {
			return pickFileForPresentSides(minecraftVersion, nestedIntermediaryClientJar, nestedIntermediaryServerJar, nestedIntermediaryMergedJar);
		}

		@Override
		public File getProcessedIntermediaryClientJar(MinecraftVersion minecraftVersion) {
			return processedIntermediaryClientJar.get(minecraftVersion);
		}

		@Override
		public File getProcessedIntermediaryServerJar(MinecraftVersion minecraftVersion) {
			return processedIntermediaryServerJar.get(minecraftVersion);
		}

		@Override
		public File getProcessedIntermediaryMergedJar(MinecraftVersion minecraftVersion) {
			return processedIntermediaryMergedJar.get(minecraftVersion);
		}

		@Override
		public File getMainProcessedIntermediaryJar(MinecraftVersion minecraftVersion) {
			return pickFileForPresentSides(minecraftVersion, processedIntermediaryClientJar, processedIntermediaryServerJar, processedIntermediaryMergedJar);
		}
	}

	public static class MappingsCache extends FileContainer implements FileCache, GlobalCacheAccess.MappingsCacheAccess {

		private final Property<File> dir;

		private final Versioned<MinecraftVersion, File> clientIntermediaryMappingsJar;
		private final Versioned<MinecraftVersion, File> serverIntermediaryMappingsJar;
		private final Versioned<MinecraftVersion, File> mergedIntermediaryMappingsJar;
		private final Versioned<MinecraftVersion, File> clientIntermediaryMappingsFile;
		private final Versioned<MinecraftVersion, File> serverIntermediaryMappingsFile;
		private final Versioned<MinecraftVersion, File> mergedIntermediaryMappingsFile;
		private final Versioned<MinecraftVersion, File> filledClientIntermediaryMappingsFile;
		private final Versioned<MinecraftVersion, File> filledServerIntermediaryMappingsFile;
		private final Versioned<MinecraftVersion, File> filledMergedIntermediaryMappingsFile;

		private final Versioned<String, File> namedMappingsJar;
		private final Versioned<String, File> namedMappingsFile;

		public MappingsCache(KeratinGradleExtension keratin, GlobalCache globalCache) {
			super(keratin);

			this.dir = fileProperty(() -> globalCache.file("mappings"));

			this.clientIntermediaryMappingsJar = new Versioned<>(minecraftVersion -> file("%s-intermediary-gen%d-client.jar".formatted(minecraftVersion.client().id(), getIntermediaryGen())));
			this.serverIntermediaryMappingsJar = new Versioned<>(minecraftVersion -> file("%s-intermediary-gen%d-server.jar".formatted(minecraftVersion.server().id(), getIntermediaryGen())));
			this.mergedIntermediaryMappingsJar = new Versioned<>(minecraftVersion -> file("%s-intermediary-gen%d-merged.jar".formatted(minecraftVersion.id(), getIntermediaryGen())));
			this.clientIntermediaryMappingsFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient()) {
					throw new NoSuchFileException("client intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("client intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
				} else {
					return file("%s-intermediary-gen%d-client.tiny".formatted(minecraftVersion.client().id(), getIntermediaryGen()));
				}
			});
			this.serverIntermediaryMappingsFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer()) {
					throw new NoSuchFileException("server intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("server intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
				} else {
					return file("%s-intermediary-gen%d-server.tiny".formatted(minecraftVersion.server().id(), getIntermediaryGen()));
				}
			});
			this.mergedIntermediaryMappingsFile = new Versioned<>(minecraftVersion -> file("%s-intermediary-gen%d-merged.tiny".formatted(minecraftVersion.id(), getIntermediaryGen())));
			this.filledClientIntermediaryMappingsFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient()) {
					throw new NoSuchFileException("client intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("client intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
				} else {
					return file("%s-filled-intermediary-gen%d-client.tiny".formatted(minecraftVersion.client().id(), getIntermediaryGen()));
				}
			});
			this.filledServerIntermediaryMappingsFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer()) {
					throw new NoSuchFileException("server intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("server intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
				} else {
					return file("%s-filled-intermediary-gen%d-server.tiny".formatted(minecraftVersion.server().id(), getIntermediaryGen()));
				}
			});
			this.filledMergedIntermediaryMappingsFile = new Versioned<>(minecraftVersion -> file("%s-filled-intermediary-gen%d-merged.tiny".formatted(minecraftVersion.id(), getIntermediaryGen())));

			this.namedMappingsJar = new Versioned<>(minecraftVersion -> file("%s-named-gen%d+build.%d.jar".formatted(minecraftVersion, getIntermediaryGen(), keratin.getNamedMappingsBuild(minecraftVersion))));
			this.namedMappingsFile = new Versioned<>(minecraftVersion -> {
				int build = keratin.getNamedMappingsBuild(minecraftVersion);

				if (build < 1) {
					throw new NoSuchFileException("no named mappings builds for Minecraft version " + minecraftVersion + " exist yet!");
				}

				return file("%s-named-gen%d+build.%d.tiny".formatted(minecraftVersion, getIntermediaryGen(), build));
			});
		}

		@Override
		public File getDirectory() {
			return dir.get();
		}

		@Override
		public File getClientIntermediaryMappingsJar(MinecraftVersion minecraftVersion) {
			return clientIntermediaryMappingsJar.get(minecraftVersion);
		}

		@Override
		public File getServerIntermediaryMappingsJar(MinecraftVersion minecraftVersion) {
			return serverIntermediaryMappingsJar.get(minecraftVersion);
		}

		@Override
		public File getMergedIntermediaryMappingsJar(MinecraftVersion minecraftVersion) {
			return mergedIntermediaryMappingsJar.get(minecraftVersion);
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
		public File getMainIntermediaryMappingsFile(MinecraftVersion minecraftVersion) {
			return pickFileForPresentSides(minecraftVersion, clientIntermediaryMappingsFile, serverIntermediaryMappingsFile, mergedIntermediaryMappingsFile);
		}

		@Override
		public File getFilledClientIntermediaryMappingsFile(MinecraftVersion minecraftVersion) {
			return filledClientIntermediaryMappingsFile.get(minecraftVersion);
		}

		@Override
		public File getFilledServerIntermediaryMappingsFile(MinecraftVersion minecraftVersion) {
			return filledServerIntermediaryMappingsFile.get(minecraftVersion);
		}

		@Override
		public File getFilledMergedIntermediaryMappingsFile(MinecraftVersion minecraftVersion) {
			return filledMergedIntermediaryMappingsFile.get(minecraftVersion);
		}

		@Override
		public File getMainFilledIntermediaryMappingsFile(MinecraftVersion minecraftVersion) {
			return pickFileForPresentSides(minecraftVersion, filledClientIntermediaryMappingsFile, filledServerIntermediaryMappingsFile, filledMergedIntermediaryMappingsFile);
		}

		@Override
		public File getNamedMappingsJar(String minecraftVersion) {
			return namedMappingsJar.get(minecraftVersion);
		}

		@Override
		public File getNamedMappingsFile(String minecraftVersion) {
			return namedMappingsFile.get(minecraftVersion);
		}
	}

	public static class ExceptionsCache extends FileContainer implements FileCache, GlobalCacheAccess.ExceptionsCacheAccess {

		private final Property<File> dir;

		private final Versioned<MinecraftVersion, File> clientExceptionsJar;
		private final Versioned<MinecraftVersion, File> serverExceptionsJar;
		private final Versioned<MinecraftVersion, File> mergedExceptionsJar;
		private final Versioned<MinecraftVersion, File> clientExceptionsFile;
		private final Versioned<MinecraftVersion, File> serverExceptionsFile;
		private final Versioned<MinecraftVersion, File> mergedExceptionsFile;
		private final Versioned<MinecraftVersion, File> intermediaryClientExceptionsFile;
		private final Versioned<MinecraftVersion, File> intermediaryServerExceptionsFile;
		private final Versioned<MinecraftVersion, File> intermediaryMergedExceptionsFile;

		public ExceptionsCache(KeratinGradleExtension keratin, GlobalCache globalCache) {
			super(keratin);

			this.dir = fileProperty(() -> globalCache.file("exceptions"));

			this.clientExceptionsJar = new Versioned<>(minecraftVersion -> file("%s-exceptions+build.%d-client.jar".formatted(minecraftVersion.client().id(), keratin.getExceptionsBuild(minecraftVersion, GameSide.CLIENT))));
			this.serverExceptionsJar = new Versioned<>(minecraftVersion -> file("%s-exceptions+build.%d-server.jar".formatted(minecraftVersion.server().id(), keratin.getExceptionsBuild(minecraftVersion, GameSide.SERVER))));
			this.mergedExceptionsJar = new Versioned<>(minecraftVersion -> file("%s-exceptions+build.%d-merged.jar".formatted(minecraftVersion.id(), keratin.getExceptionsBuild(minecraftVersion, GameSide.MERGED))));
			this.clientExceptionsFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient()) {
					throw new NoSuchFileException("client exceptions for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("client exceptions for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged exceptions!");
				} else {
					int build = keratin.getExceptionsBuild(minecraftVersion, GameSide.CLIENT);

					if (build < 1) {
						return null;
					} else {
						return file("%s-exceptions+build.%d-client.excs".formatted(minecraftVersion.client().id(), build));
					}
				}
			});
			this.serverExceptionsFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer()) {
					throw new NoSuchFileException("server exceptions for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("server exceptions for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged exceptions!");
				} else {
					int build = keratin.getExceptionsBuild(minecraftVersion, GameSide.SERVER);

					if (build < 1) {
						return null;
					} else {
						return file("%s-exceptions+build.%d-server.excs".formatted(minecraftVersion.server().id(), build));
					}
				}
			});
			this.mergedExceptionsFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("exceptions for Minecraft version " + minecraftVersion.id() + " cannot be merged: the client and server exceptions do not have shared mappings!");
				} else {
					int build = keratin.getExceptionsBuild(minecraftVersion, GameSide.MERGED);

					if (build < 1) {
						return null;
					} else {
						return file("%s-exceptions+build.%d-merged.excs".formatted(minecraftVersion.id(), build));
					}
				}
			});
			this.intermediaryClientExceptionsFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient()) {
					throw new NoSuchFileException("client exceptions for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("intermediary client exceptions for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged exceptions!");
				} else {
					int build = keratin.getExceptionsBuild(minecraftVersion, GameSide.CLIENT);

					if (build < 1) {
						return null;
					} else {
						return file("%s-intermediary-gen%d-exceptions+build.%d-client.excs".formatted(minecraftVersion.client().id(), getIntermediaryGen(), build));
					}
				}
			});
			this.intermediaryServerExceptionsFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer()) {
					throw new NoSuchFileException("server exceptions for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("intermediary server exceptions for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged exceptions!");
				} else {
					int build = keratin.getExceptionsBuild(minecraftVersion, GameSide.SERVER);

					if (build < 1) {
						return null;
					} else {
						return file("%s-intermediary-gen%d-exceptions+build.%d-server.excs".formatted(minecraftVersion.server().id(), getIntermediaryGen(), build));
					}
				}
			});
			this.intermediaryMergedExceptionsFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
					throw new NoSuchFileException("intermediary exceptions for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server exceptions do not exist!");
				} else {
					int build = keratin.getExceptionsBuild(minecraftVersion, GameSide.MERGED);
					int clientBuild = keratin.getExceptionsBuild(minecraftVersion, GameSide.CLIENT);
					int serverBuild = keratin.getExceptionsBuild(minecraftVersion, GameSide.SERVER);

					if (build < 1) {
						if (clientBuild < 1 || serverBuild < 1) {
							return null;
						} else {
							return file("%s-intermediary-gen%d-exceptions+build.(%d-%d)-merged.excs".formatted(minecraftVersion.id(), getIntermediaryGen(), clientBuild, serverBuild));
						}
					} else {
						return file("%s-intermediary-gen%d-exceptions+build.%d-merged.excs".formatted(minecraftVersion.id(), getIntermediaryGen(), build));
					}
				}
			});
		}

		@Override
		public File getDirectory() {
			return dir.get();
		}

		@Override
		public File getClientExceptionsJar(MinecraftVersion minecraftVersion) {
			return clientExceptionsJar.get(minecraftVersion);
		}

		@Override
		public File getServerExceptionsJar(MinecraftVersion minecraftVersion) {
			return serverExceptionsJar.get(minecraftVersion);
		}

		@Override
		public File getMergedExceptionsJar(MinecraftVersion minecraftVersion) {
			return mergedExceptionsJar.get(minecraftVersion);
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
		public File getIntermediaryClientExceptionsFile(MinecraftVersion minecraftVersion) {
			return intermediaryClientExceptionsFile.get(minecraftVersion);
		}

		@Override
		public File getIntermediaryServerExceptionsFile(MinecraftVersion minecraftVersion) {
			return intermediaryServerExceptionsFile.get(minecraftVersion);
		}

		@Override
		public File getIntermediaryMergedExceptionsFile(MinecraftVersion minecraftVersion) {
			return intermediaryMergedExceptionsFile.get(minecraftVersion);
		}

		@Override
		public File getMainIntermediaryExceptionsFile(MinecraftVersion minecraftVersion) {
			return pickFileForPresentSides(minecraftVersion, intermediaryClientExceptionsFile, intermediaryServerExceptionsFile, intermediaryMergedExceptionsFile);
		}
	}

	public static class SignaturesCache extends FileContainer implements FileCache, GlobalCacheAccess.SignaturesCacheAccess {

		private final Property<File> dir;

		private final Versioned<MinecraftVersion, File> clientSignaturesJar;
		private final Versioned<MinecraftVersion, File> serverSignaturesJar;
		private final Versioned<MinecraftVersion, File> mergedSignaturesJar;
		private final Versioned<MinecraftVersion, File> clientSignaturesFile;
		private final Versioned<MinecraftVersion, File> serverSignaturesFile;
		private final Versioned<MinecraftVersion, File> mergedSignaturesFile;
		private final Versioned<MinecraftVersion, File> intermediaryClientSignaturesFile;
		private final Versioned<MinecraftVersion, File> intermediaryServerSignaturesFile;
		private final Versioned<MinecraftVersion, File> intermediaryMergedSignaturesFile;

		public SignaturesCache(KeratinGradleExtension keratin, GlobalCache globalCache) {
			super(keratin);

			this.dir = fileProperty(() -> globalCache.file("signatures"));

			this.clientSignaturesJar = new Versioned<>(minecraftVersion -> file("%s-signatures+build.%d-client.jar".formatted(minecraftVersion.client().id(), keratin.getSignaturesBuild(minecraftVersion, GameSide.CLIENT))));
			this.serverSignaturesJar = new Versioned<>(minecraftVersion -> file("%s-signatures+build.%d-server.jar".formatted(minecraftVersion.server().id(), keratin.getSignaturesBuild(minecraftVersion, GameSide.SERVER))));
			this.mergedSignaturesJar = new Versioned<>(minecraftVersion -> file("%s-signatures+build.%d-merged.jar".formatted(minecraftVersion.id(), keratin.getSignaturesBuild(minecraftVersion, GameSide.MERGED))));
			this.clientSignaturesFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient()) {
					throw new NoSuchFileException("client signatures for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("client signatures for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged signatures!");
				} else {
					int build = keratin.getSignaturesBuild(minecraftVersion, GameSide.CLIENT);

					if (build < 1) {
						return null;
					} else {
						return file("%s-signatures+build.%d-client.sigs".formatted(minecraftVersion.client().id(), build));
					}
				}
			});
			this.serverSignaturesFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer()) {
					throw new NoSuchFileException("server signatures for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("server signatures for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged signatures!");
				} else {
					int build = keratin.getSignaturesBuild(minecraftVersion, GameSide.SERVER);

					if (build < 1) {
						return null;
					} else {
						return file("%s-signatures+build.%d-server.sigs".formatted(minecraftVersion.server().id(), build));
					}
				}
			});
			this.mergedSignaturesFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("signatures for Minecraft version " + minecraftVersion.id() + " cannot be merged: the client and server signatures do not have shared mappings!");
				} else {
					int build = keratin.getSignaturesBuild(minecraftVersion, GameSide.MERGED);

					if (build < 1) {
						return null;
					} else {
						return file("%s-signatures+build.%d-merged.sigs".formatted(minecraftVersion.id(), build));
					}
				}
			});
			this.intermediaryClientSignaturesFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient()) {
					throw new NoSuchFileException("client signatures for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("intermediary client signatures for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged signatures!");
				} else {
					int build = keratin.getSignaturesBuild(minecraftVersion, GameSide.CLIENT);

					if (build < 1) {
						return null;
					} else {
						return file("%s-intermediary-gen%d-signatures+build.%d-client.sigs".formatted(minecraftVersion.client().id(), getIntermediaryGen(), build));
					}
				}
			});
			this.intermediaryServerSignaturesFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer()) {
					throw new NoSuchFileException("server signatures for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("intermediary server signatures for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged signatures!");
				} else {
					int build = keratin.getSignaturesBuild(minecraftVersion, GameSide.SERVER);

					if (build < 1) {
						return null;
					} else {
						return file("%s-intermediary-gen%d-signatures+build.%d-server.sigs".formatted(minecraftVersion.server().id(), getIntermediaryGen(), build));
					}
				}
			});
			this.intermediaryMergedSignaturesFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
					throw new NoSuchFileException("intermediary signatures for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server signatures do not exist!");
				} else {
					int build = keratin.getSignaturesBuild(minecraftVersion, GameSide.MERGED);
					int clientBuild = keratin.getSignaturesBuild(minecraftVersion, GameSide.CLIENT);
					int serverBuild = keratin.getSignaturesBuild(minecraftVersion, GameSide.SERVER);

					if (build < 1) {
						if (clientBuild < 1 || serverBuild < 1) {
							return null;
						} else {
							return file("%s-intermediary-gen%d-signatures+build.(%d-%d)-merged.sigs".formatted(minecraftVersion.id(), getIntermediaryGen(), clientBuild, serverBuild));
						}
					} else {
						return file("%s-intermediary-gen%d-signatures+build.%d-merged.sigs".formatted(minecraftVersion.id(), getIntermediaryGen(), build));
					}
				}
			});
		}

		@Override
		public File getDirectory() {
			return dir.get();
		}

		@Override
		public File getClientSignaturesJar(MinecraftVersion minecraftVersion) {
			return clientSignaturesJar.get(minecraftVersion);
		}

		@Override
		public File getServerSignaturesJar(MinecraftVersion minecraftVersion) {
			return serverSignaturesJar.get(minecraftVersion);
		}

		@Override
		public File getMergedSignaturesJar(MinecraftVersion minecraftVersion) {
			return mergedSignaturesJar.get(minecraftVersion);
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
		public File getIntermediaryClientSignaturesFile(MinecraftVersion minecraftVersion) {
			return intermediaryClientSignaturesFile.get(minecraftVersion);
		}

		@Override
		public File getIntermediaryServerSignaturesFile(MinecraftVersion minecraftVersion) {
			return intermediaryServerSignaturesFile.get(minecraftVersion);
		}

		@Override
		public File getIntermediaryMergedSignaturesFile(MinecraftVersion minecraftVersion) {
			return intermediaryMergedSignaturesFile.get(minecraftVersion);
		}

		@Override
		public File getMainIntermediarySignaturesFile(MinecraftVersion minecraftVersion) {
			return pickFileForPresentSides(minecraftVersion, intermediaryClientSignaturesFile, intermediaryServerSignaturesFile, intermediaryMergedSignaturesFile);
		}
	}

	public static class NestsCache extends FileContainer implements FileCache, GlobalCacheAccess.NestsCacheAccess {

		private final Property<File> dir;

		private final Versioned<MinecraftVersion, File> clientNestsJar;
		private final Versioned<MinecraftVersion, File> serverNestsJar;
		private final Versioned<MinecraftVersion, File> mergedNestsJar;
		private final Versioned<MinecraftVersion, File> clientNestsFile;
		private final Versioned<MinecraftVersion, File> serverNestsFile;
		private final Versioned<MinecraftVersion, File> mergedNestsFile;
		private final Versioned<MinecraftVersion, File> intermediaryClientNestsFile;
		private final Versioned<MinecraftVersion, File> intermediaryServerNestsFile;
		private final Versioned<MinecraftVersion, File> intermediaryMergedNestsFile;

		public NestsCache(KeratinGradleExtension keratin, GlobalCache globalCache) {
			super(keratin);

			this.dir = fileProperty(() -> globalCache.file("nests"));

			this.clientNestsJar = new Versioned<>(minecraftVersion -> file("%s-nests+build.%d-client.jar".formatted(minecraftVersion.client().id(), keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT))));
			this.serverNestsJar = new Versioned<>(minecraftVersion -> file("%s-nests+build.%d-server.jar".formatted(minecraftVersion.server().id(), keratin.getNestsBuild(minecraftVersion, GameSide.SERVER))));
			this.mergedNestsJar = new Versioned<>(minecraftVersion -> file("%s-nests+build.%d-merged.jar".formatted(minecraftVersion.id(), keratin.getNestsBuild(minecraftVersion, GameSide.MERGED))));
			this.clientNestsFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient()) {
					throw new NoSuchFileException("client nests for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("client nests for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged nests!");
				} else {
					int build = keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT);

					if (build < 1) {
						return null;
					} else {
						return file("%s-nests+build.%d-client.nests".formatted(minecraftVersion.client().id(), build));
					}
				}
			});
			this.serverNestsFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer()) {
					throw new NoSuchFileException("server nests for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("server nests for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged nests!");
				} else {
					int build = keratin.getNestsBuild(minecraftVersion, GameSide.SERVER);

					if (build < 1) {
						return null;
					} else {
						return file("%s-nests+build.%d-server.nests".formatted(minecraftVersion.server().id(), build));
					}
				}
			});
			this.mergedNestsFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("nests for Minecraft version " + minecraftVersion.id() + " cannot be merged: the client and server nests do not have shared mappings!");
				} else {
					int build = keratin.getNestsBuild(minecraftVersion, GameSide.MERGED);

					if (build < 1) {
						return null;
					} else {
						return file("%s-nests+build.%d-merged.nests".formatted(minecraftVersion.id(), build));
					}
				}
			});
			this.intermediaryClientNestsFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient()) {
					throw new NoSuchFileException("client nests for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("intermediary client nests for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged nests!");
				} else {
					int build = keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT);

					if (build < 1) {
						return null;
					} else {
						return file("%s-intermediary-gen%d-nests+build.%d-client.nests".formatted(minecraftVersion.client().id(), getIntermediaryGen(), build));
					}
				}
			});
			this.intermediaryServerNestsFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasServer()) {
					throw new NoSuchFileException("server nests for Minecraft version " + minecraftVersion.id() + " do not exist!");
				} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
					throw new NoSuchFileException("intermediary server nests for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged nests!");
				} else {
					int build = keratin.getNestsBuild(minecraftVersion, GameSide.SERVER);

					if (build < 1) {
						return null;
					} else {
						return file("%s-intermediary-gen%d-nests+build.%d-server.nests".formatted(minecraftVersion.server().id(), getIntermediaryGen(), build));
					}
				}
			});
			this.intermediaryMergedNestsFile = new Versioned<>(minecraftVersion -> {
				if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
					throw new NoSuchFileException("intermediary nests for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server nests do not exist!");
				} else {
					int build = keratin.getNestsBuild(minecraftVersion, GameSide.MERGED);
					int clientBuild = keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT);
					int serverBuild = keratin.getNestsBuild(minecraftVersion, GameSide.SERVER);

					if (build < 1) {
						if (clientBuild < 1 || serverBuild < 1) {
							return null;
						} else {
							return file("%s-intermediary-gen%d-nests+build.(%d-%d)-merged.nests".formatted(minecraftVersion.id(), getIntermediaryGen(), clientBuild, serverBuild));
						}
					} else {
						return file("%s-intermediary-gen%d-nests+build.%d-merged.nests".formatted(minecraftVersion.id(), getIntermediaryGen(), build));
					}
				}
			});
		}

		@Override
		public File getDirectory() {
			return dir.get();
		}

		@Override
		public File getClientNestsJar(MinecraftVersion minecraftVersion) {
			return clientNestsJar.get(minecraftVersion);
		}

		@Override
		public File getServerNestsJar(MinecraftVersion minecraftVersion) {
			return serverNestsJar.get(minecraftVersion);
		}

		@Override
		public File getMergedNestsJar(MinecraftVersion minecraftVersion) {
			return mergedNestsJar.get(minecraftVersion);
		}

		@Override
		public File getClientNestsFile(MinecraftVersion minecraftVersion) {
			return clientNestsFile.get(minecraftVersion);
		}

		@Override
		public File getServerNestsFile(MinecraftVersion minecraftVersion) {
			return serverNestsFile.get(minecraftVersion);
		}

		@Override
		public File getMergedNestsFile(MinecraftVersion minecraftVersion) {
			return mergedNestsFile.get(minecraftVersion);
		}

		@Override
		public File getIntermediaryClientNestsFile(MinecraftVersion minecraftVersion) {
			return intermediaryClientNestsFile.get(minecraftVersion);
		}

		@Override
		public File getIntermediaryServerNestsFile(MinecraftVersion minecraftVersion) {
			return intermediaryServerNestsFile.get(minecraftVersion);
		}

		@Override
		public File getIntermediaryMergedNestsFile(MinecraftVersion minecraftVersion) {
			return intermediaryMergedNestsFile.get(minecraftVersion);
		}

		@Override
		public File getMainIntermediaryNestsFile(MinecraftVersion minecraftVersion) {
			return pickFileForPresentSides(minecraftVersion, intermediaryClientNestsFile, intermediaryServerNestsFile, intermediaryMergedNestsFile);
		}
	}

	public static class LibrariesCache extends FileContainer implements FileCache, GlobalCacheAccess.LibrariesCacheAccess {

		private final Property<File> dir;

		public LibrariesCache(KeratinGradleExtension keratin, GlobalCache globalCache) {
			super(keratin);

			this.dir = fileProperty(() -> globalCache.file("libraries"));
		}

		@Override
		public File getDirectory() {
			return dir.get();
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
