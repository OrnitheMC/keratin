package net.ornithemc.keratin;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;

import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.util.Versioned;

public class OrnitheFiles implements OrnitheFilesAPI {

	private final Project project;
	private final KeratinGradleExtension keratin;

	private final Set<Property<File>> cacheDirs;

	private final Property<File> globalBuildCache;
	private final Property<File> localBuildCache;

	private final Property<File> versionJsonsCache;
	private final Property<File> gameJarsCache;
	private final Property<File> mappedJarsCache;
	private final Property<File> processedJarsCache;
	private final Property<File> librariesCache;
	private final Property<File> mappingsCache;
	private final Property<File> processedMappingsCache;
	private final Property<File> nestsCache;
	private final Property<File> sparrowCache;

	private final Property<File> nestsBuildsCache;
	private final Property<File> sparrowBuildsCache;
	private final Property<File> mappingsDir;
	private final Property<File> matchesDir;
	private final Versioned<File> runDir;
	private final Versioned<File> decompiledSrcDir;
	private final Versioned<File> fakeSrcDir;
	private final Versioned<File> javadocDir;

	private final Property<File> versionsManifest;
	private final Versioned<File> versionInfos;
	private final Versioned<File> versionDetails;

	private final Versioned<File> clientJar;
	private final Versioned<File> serverJar;
	private final Versioned<File> mergedJar;
	private final Versioned<File> intermediaryClientJar;
	private final Versioned<File> intermediaryServerJar;
	private final Versioned<File> intermediaryMergedJar;
	private final Versioned<File> nestedIntermediaryClientJar;
	private final Versioned<File> nestedIntermediaryServerJar;
	private final Versioned<File> nestedIntermediaryMergedJar;
	private final Versioned<File> signaturePatchedIntermediaryClientJar;
	private final Versioned<File> signaturePatchedIntermediaryServerJar;
	private final Versioned<File> signaturePatchedIntermediaryMergedJar;
	private final Versioned<File> processedIntermediaryClientJar;
	private final Versioned<File> processedIntermediaryServerJar;
	private final Versioned<File> processedIntermediaryMergedJar;

	private final Versioned<File> namedJar;
	private final Versioned<File> processedNamedJar;

	private final Versioned<File> clientIntermediaryMappings;
	private final Versioned<File> serverIntermediaryMappings;
	private final Versioned<File> mergedIntermediaryMappings;
	private final Versioned<File> nestedClientIntermediaryMappings;
	private final Versioned<File> nestedServerIntermediaryMappings;
	private final Versioned<File> nestedMergedIntermediaryMappings;
	private final Versioned<File> processedClientIntermediaryMappings;
	private final Versioned<File> processedServerIntermediaryMappings;
	private final Versioned<File> processedMergedIntermediaryMappings;

	private final Versioned<File> namedMappings;
	private final Versioned<File> processedNamedMappings;
	private final Versioned<File> completedNamedMappings;
	private final Versioned<File> tinyV1NamedMappings;
	private final Versioned<File> tinyV2NamedMappings;
	private final Versioned<File> mergedTinyV1NamedMappings;
	private final Versioned<File> mergedTinyV2NamedMappings;

	private final Versioned<File> clientNests;
	private final Versioned<File> serverNests;
	private final Versioned<File> mergedNests;
	private final Versioned<File> intermediaryClientNests;
	private final Versioned<File> intermediaryServerNests;
	private final Versioned<File> intermediaryMergedNests;

	private final Versioned<File> namedNests;

	private final Versioned<File> clientSparrowFile;
	private final Versioned<File> serverSparrowFile;
	private final Versioned<File> mergedSparrowFile;
	private final Versioned<File> intermediaryClientSparrowFile;
	private final Versioned<File> intermediaryServerSparrowFile;
	private final Versioned<File> intermediaryMergedSparrowFile;

	private final Versioned<File> namedSparrowFile;

	public OrnitheFiles(KeratinGradleExtension keratin) {
		this.project = keratin.getProject();
		this.keratin = keratin;

		this.cacheDirs = new LinkedHashSet<>();

		this.globalBuildCache = cacheDirectoryProperty(() -> new File(this.project.getGradle().getGradleUserHomeDir(), "caches/%s".formatted(keratin.getGlobalCacheDirectory().get())));
		this.localBuildCache = cacheDirectoryProperty(() -> new File(this.project.file(".gradle"), keratin.getLocalCacheDirectory().get()));

		this.versionJsonsCache = cacheDirectoryProperty(() -> new File(getGlobalBuildCache(), "version-jsons"));
		this.gameJarsCache = cacheDirectoryProperty(() -> new File(getGlobalBuildCache(), "game-jars"));
		this.mappedJarsCache = cacheDirectoryProperty(() -> new File(getGlobalBuildCache(), "mapped-jars"));
		this.processedJarsCache = cacheDirectoryProperty(() -> new File(getGlobalBuildCache(), "processed-jars"));
		this.librariesCache = cacheDirectoryProperty(() -> new File(getGlobalBuildCache(), "libraries"));
		this.mappingsCache = cacheDirectoryProperty(() -> new File(getGlobalBuildCache(), "mappings"));
		this.processedMappingsCache = cacheDirectoryProperty(() -> new File(getGlobalBuildCache(), "processed-mappings"));
		this.nestsCache = cacheDirectoryProperty(() -> new File(getGlobalBuildCache(), "nests"));
		this.sparrowCache = cacheDirectoryProperty(() -> new File(getGlobalBuildCache(), "sparrow"));

		this.nestsBuildsCache = fileProperty(() -> this.project.file("nests-builds.json"));
		this.sparrowBuildsCache = fileProperty(() -> this.project.file("sparrow-builds.json"));
		this.mappingsDir = fileProperty(() -> this.project.file("mappings"));
		this.matchesDir = fileProperty(() -> this.project.file("matches/matches"));
		this.runDir = new Versioned<>(minecraftVersion -> this.project.file("run/%s".formatted(minecraftVersion)));
		this.decompiledSrcDir = new Versioned<>(minecraftVersion -> this.project.file("%s-decompiledSrc".formatted(minecraftVersion)));
		this.fakeSrcDir = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-fakeSrc".formatted(minecraftVersion)));
		this.javadocDir = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-javadoc".formatted(minecraftVersion)));

		this.versionsManifest = fileProperty(() -> new File(getGlobalBuildCache(), "versions-manifest.json"));
		this.versionInfos = new Versioned<>(minecraftVersion -> new File(getVersionJsonsCache(), "%s-info.json".formatted(minecraftVersion)));
		this.versionDetails = new Versioned<>(minecraftVersion -> new File(getVersionJsonsCache(), "%s-details.json".formatted(minecraftVersion)));

		this.clientJar = new Versioned<>(minecraftVersion -> new File(getGameJarsCache(), "%s-client.jar".formatted(minecraftVersion)));
		this.serverJar = new Versioned<>(minecraftVersion -> new File(getGameJarsCache(), "%s-server.jar".formatted(minecraftVersion)));
		this.mergedJar = new Versioned<>(minecraftVersion -> {
			if (!keratin.getVersionDetails(minecraftVersion).sharedMappings()) {
				throw new RuntimeException("game jars for Minecraft version " + minecraftVersion + " cannot be merged!");
			} else {
				return new File(getGameJarsCache(), "%s-merged.jar".formatted(minecraftVersion));
			}
		});
		this.intermediaryClientJar = new Versioned<>(minecraftVersion -> new File(getMappedJarsCache(), "%s-intermediary-gen%d-client.jar".formatted(minecraftVersion, getIntermediaryGen())));
		this.intermediaryServerJar = new Versioned<>(minecraftVersion -> new File(getMappedJarsCache(), "%s-intermediary-gen%d-server.jar".formatted(minecraftVersion, getIntermediaryGen())));
		this.intermediaryMergedJar = new Versioned<>(minecraftVersion -> new File(getMappedJarsCache(), "%s-intermediary-gen%d-merged.jar".formatted(minecraftVersion, getIntermediaryGen())));
		this.nestedIntermediaryClientJar = new Versioned<>(minecraftVersion -> {
			int build = keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT);

			if (build < 1) {
				return null;
			} else {
				return new File(getProcessedJarsCache(), "%s-nests+build.%d-intermediary-gen%d-client.jar".formatted(minecraftVersion, build, getIntermediaryGen()));
			}
		});
		this.nestedIntermediaryServerJar = new Versioned<>(minecraftVersion -> {
			int build = keratin.getNestsBuild(minecraftVersion, GameSide.SERVER);

			if (build < 1) {
				return null;
			} else {
				return new File(getProcessedJarsCache(), "%s-nests+build.%d-intermediary-gen%d-server.jar".formatted(minecraftVersion, build, getIntermediaryGen()));
			}
		});
		this.nestedIntermediaryMergedJar = new Versioned<>(minecraftVersion -> {
			int build = keratin.getNestsBuild(minecraftVersion, GameSide.MERGED);
			int clientBuild = keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT);
			int serverBuild = keratin.getNestsBuild(minecraftVersion, GameSide.SERVER);

			if (build < 1) {
				if (clientBuild < 1 && serverBuild < 1) {
					return null;
				} else {
					return new File(getProcessedJarsCache(), "%s-nests+build.(%d-%d)-intermediary-gen%d-merged.jar".formatted(minecraftVersion, clientBuild, serverBuild, getIntermediaryGen()));
				}
			} else {
				return new File(getProcessedJarsCache(), "%s-nests+build.%d-intermediary-gen%d-merged.jar".formatted(minecraftVersion, build, getIntermediaryGen()));
			}
		});
		this.signaturePatchedIntermediaryClientJar = new Versioned<>(minecraftVersion -> {
			int build = keratin.getSparrowBuild(minecraftVersion, GameSide.CLIENT);

			if (build < 1) {
				return null;
			} else {
				return new File(getProcessedJarsCache(), "%s-sparrow+build.%d-intermediary-gen%d-client.jar".formatted(minecraftVersion, build, getIntermediaryGen()));
			}
		});
		this.signaturePatchedIntermediaryServerJar = new Versioned<>(minecraftVersion -> {
			int build = keratin.getSparrowBuild(minecraftVersion, GameSide.SERVER);

			if (build < 1) {
				return null;
			} else {
				return new File(getProcessedJarsCache(), "%s-sparrow+build.%d-intermediary-gen%d-server.jar".formatted(minecraftVersion, build, getIntermediaryGen()));
			}
		});
		this.signaturePatchedIntermediaryMergedJar = new Versioned<>(minecraftVersion -> {
			int build = keratin.getSparrowBuild(minecraftVersion, GameSide.MERGED);
			int clientBuild = keratin.getSparrowBuild(minecraftVersion, GameSide.CLIENT);
			int serverBuild = keratin.getSparrowBuild(minecraftVersion, GameSide.SERVER);

			if (build < 1) {
				if (clientBuild < 1 && serverBuild < 1) {
					return null;
				} else {
					return new File(getProcessedJarsCache(), "%s-sparrow+build.(%d-%d)-intermediary-gen%d-merged.jar".formatted(minecraftVersion, clientBuild, serverBuild, getIntermediaryGen()));
				}
			} else {
				return new File(getProcessedJarsCache(), "%s-sparrow+build.%d-intermediary-gen%d-merged.jar".formatted(minecraftVersion, build, getIntermediaryGen()));
			}
		});
		this.processedIntermediaryClientJar = new Versioned<>(minecraftVersion -> new File(getProcessedJarsCache(), "%s-processed-intermediary-gen%d-client.jar".formatted(minecraftVersion, getIntermediaryGen())));
		this.processedIntermediaryServerJar = new Versioned<>(minecraftVersion -> new File(getProcessedJarsCache(), "%s-processed-intermediary-gen%d-server.jar".formatted(minecraftVersion, getIntermediaryGen())));
		this.processedIntermediaryMergedJar = new Versioned<>(minecraftVersion -> new File(getProcessedJarsCache(), "%s-processed-intermediary-gen%d-merged.jar".formatted(minecraftVersion, getIntermediaryGen())));

		this.namedJar = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-named.jar".formatted(minecraftVersion)));
		this.processedNamedJar = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-processed-named.jar".formatted(minecraftVersion)));

		this.clientIntermediaryMappings = new Versioned<>(minecraftVersion -> new File(getMappingsCache(), "%s-intermediary-gen%d-client.tiny".formatted(minecraftVersion, getIntermediaryGen())));
		this.serverIntermediaryMappings = new Versioned<>(minecraftVersion -> new File(getMappingsCache(), "%s-intermediary-gen%d-server.tiny".formatted(minecraftVersion, getIntermediaryGen())));
		this.mergedIntermediaryMappings = new Versioned<>(minecraftVersion -> new File(getMappingsCache(), "%s-intermediary-gen%d-merged.tiny".formatted(minecraftVersion, getIntermediaryGen())));
		this.nestedClientIntermediaryMappings = new Versioned<>(minecraftVersion -> {
			int build = keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT);

			if (build < 1) {
				return null;
			} else {
				return new File(getProcessedMappingsCache(), "%s-nests+build.%d-intermediary-gen%d-client.tiny".formatted(minecraftVersion, build, getIntermediaryGen()));
			}
		});
		this.nestedServerIntermediaryMappings = new Versioned<>(minecraftVersion -> {
			int build = keratin.getNestsBuild(minecraftVersion, GameSide.SERVER);

			if (build < 1) {
				return null;
			} else {
				return new File(getProcessedMappingsCache(), "%s-nests+build.%d-intermediary-gen%d-server.tiny".formatted(minecraftVersion, build, getIntermediaryGen()));
			}
		});
		this.nestedMergedIntermediaryMappings = new Versioned<>(minecraftVersion -> {
			int build = keratin.getNestsBuild(minecraftVersion, GameSide.MERGED);
			int clientBuild = keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT);
			int serverBuild = keratin.getNestsBuild(minecraftVersion, GameSide.SERVER);

			if (build < 1) {
				if (clientBuild < 1 && serverBuild < 1) {
					return null;
				} else {
					return new File(getProcessedMappingsCache(), "%s-nests+build.(%d-%d)-intermediary-gen%d-merged.tiny".formatted(minecraftVersion, clientBuild, serverBuild, getIntermediaryGen()));
				}
			} else {
				return new File(getProcessedMappingsCache(), "%s-nests+build.%d-intermediary-gen%d-merged.tiny".formatted(minecraftVersion, build, getIntermediaryGen()));
			}
		});
		this.processedClientIntermediaryMappings = new Versioned<>(minecraftVersion -> new File(getProcessedMappingsCache(), "%s-processed-intermediary-gen%d-client.tiny".formatted(minecraftVersion, getIntermediaryGen())));
		this.processedServerIntermediaryMappings = new Versioned<>(minecraftVersion -> new File(getProcessedMappingsCache(), "%s-processed-intermediary-gen%d-server.tiny".formatted(minecraftVersion, getIntermediaryGen())));
		this.processedMergedIntermediaryMappings = new Versioned<>(minecraftVersion -> new File(getProcessedMappingsCache(), "%s-processed-intermediary-gen%d-merged.tiny".formatted(minecraftVersion, getIntermediaryGen())));

		this.namedMappings = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-named.tiny".formatted(minecraftVersion)));
		this.processedNamedMappings = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-processed-named.tiny".formatted(minecraftVersion)));
		this.completedNamedMappings = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-completed.tiny".formatted(minecraftVersion)));
		this.tinyV1NamedMappings = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-tiny-v1.tiny".formatted(minecraftVersion)));
		this.tinyV2NamedMappings = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-tiny-v2.tiny".formatted(minecraftVersion)));
		this.mergedTinyV1NamedMappings = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-merged-tiny-v1.tiny".formatted(minecraftVersion)));
		this.mergedTinyV2NamedMappings = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-merged-tiny-v2.tiny".formatted(minecraftVersion)));

		this.clientNests = new Versioned<>(minecraftVersion -> {
			int build = keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT);

			if (build < 1) {
				return null;
			} else {
				return new File(getNestsCache(), "%s-nests+build.%d-client.nest".formatted(minecraftVersion, build));
			}
		});
		this.serverNests = new Versioned<>(minecraftVersion -> {
			int build = keratin.getNestsBuild(minecraftVersion, GameSide.SERVER);

			if (build < 1) {
				return null;
			} else {
				return new File(getNestsCache(), "%s-nests+build.%d-server.nest".formatted(minecraftVersion, build));
			}
		});
		this.mergedNests = new Versioned<>(minecraftVersion -> {
			int build = keratin.getNestsBuild(minecraftVersion, GameSide.MERGED);

			if (build < 1) {
				return null;
			} else {
				return new File(getNestsCache(), "%s-nests+build.%d-merged.nest".formatted(minecraftVersion, build));
			}
		});
		this.intermediaryClientNests = new Versioned<>(minecraftVersion -> {
			int build = keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT);

			if (build < 1) {
				return null;
			} else {
				return new File(getNestsCache(), "%s-intermediary-gen%d-nests+build.%d-client.nest".formatted(minecraftVersion, getIntermediaryGen(), build));
			}
		});
		this.intermediaryServerNests = new Versioned<>(minecraftVersion -> {
			int build = keratin.getNestsBuild(minecraftVersion, GameSide.SERVER);

			if (build < 1) {
				return null;
			} else {
				return new File(getNestsCache(), "%s-intermediary-gen%d-nests+build.%d-server.nest".formatted(minecraftVersion, getIntermediaryGen(), build));
			}
		});
		this.intermediaryMergedNests = new Versioned<>(minecraftVersion -> {
			int build = keratin.getNestsBuild(minecraftVersion, GameSide.MERGED);
			int clientBuild = keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT);
			int serverBuild = keratin.getNestsBuild(minecraftVersion, GameSide.SERVER);

			if (build < 1) {
				if (clientBuild < 1 && serverBuild < 1) {
					return null;
				} else {
					return new File(getNestsCache(), "%s-intermediary-gen%d-nests+build.(%d-%d)-merged.nest".formatted(minecraftVersion, getIntermediaryGen(), clientBuild, serverBuild));
				}
			} else {
				return new File(getNestsCache(), "%s-intermediary-gen%d-nests+build.%d-merged.nest".formatted(minecraftVersion, getIntermediaryGen(), build));
			}
		});

		this.namedNests = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-named-nests.nest".formatted(minecraftVersion)));

		this.clientSparrowFile = new Versioned<>(minecraftVersion -> {
			int build = keratin.getSparrowBuild(minecraftVersion, GameSide.CLIENT);

			if (build < 1) {
				return null;
			} else {
				return new File(getSparrowCache(), "%s-sparrow+build.%d-client.sigs".formatted(minecraftVersion, build));
			}
		});
		this.serverSparrowFile = new Versioned<>(minecraftVersion -> {
			int build = keratin.getSparrowBuild(minecraftVersion, GameSide.SERVER);

			if (build < 1) {
				return null;
			} else {
				return new File(getSparrowCache(), "%s-sparrow+build.%d-server.sigs".formatted(minecraftVersion, build));
			}
		});
		this.mergedSparrowFile = new Versioned<>(minecraftVersion -> {
			int build = keratin.getSparrowBuild(minecraftVersion, GameSide.MERGED);

			if (build < 1) {
				return null;
			} else {
				return new File(getSparrowCache(), "%s-sparrow+build.%d-merged.sigs".formatted(minecraftVersion, build));
			}
		});
		this.intermediaryClientSparrowFile = new Versioned<>(minecraftVersion -> {
			int build = keratin.getSparrowBuild(minecraftVersion, GameSide.CLIENT);

			if (build < 1) {
				return null;
			} else {
				return new File(getSparrowCache(), "%s-intermediary-gen%d-sparrow+build.%d-client.sigs".formatted(minecraftVersion, getIntermediaryGen(), build));
			}
		});
		this.intermediaryServerSparrowFile = new Versioned<>(minecraftVersion -> {
			int build = keratin.getSparrowBuild(minecraftVersion, GameSide.SERVER);

			if (build < 1) {
				return null;
			} else {
				return new File(getSparrowCache(), "%s-intermediary-gen%d-sparrow+build.%d-server.sigs".formatted(minecraftVersion, getIntermediaryGen(), build));
			}
		});
		this.intermediaryMergedSparrowFile = new Versioned<>(minecraftVersion -> {
			int build = keratin.getSparrowBuild(minecraftVersion, GameSide.MERGED);
			int clientBuild = keratin.getSparrowBuild(minecraftVersion, GameSide.CLIENT);
			int serverBuild = keratin.getSparrowBuild(minecraftVersion, GameSide.SERVER);

			if (build < 1) {
				if (clientBuild < 1 && serverBuild < 1) {
					return null;
				} else {
					return new File(getSparrowCache(), "%s-intermediary-gen%d-sparrow+build.(%d-%d)-merged.sigs".formatted(minecraftVersion, getIntermediaryGen(), clientBuild, serverBuild));
				}
			} else {
				return new File(getSparrowCache(), "%s-intermediary-gen%d-sparrow+build.%d-merged.sigs".formatted(minecraftVersion, getIntermediaryGen(), build));
			}
		});

		this.namedSparrowFile = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-named-sparrow.sigs".formatted(minecraftVersion)));
	}

	private Property<File> fileProperty(Callable<File> provider) {
		Property<File> property = this.project.getObjects().property(File.class);
		property.convention(this.project.provider(provider));
		property.finalizeValueOnRead();
		return property;
	}

	private Property<File> cacheDirectoryProperty(Callable<File> provider) {
		Property<File> property = fileProperty(provider);
		cacheDirs.add(property);
		return property;
	}

	private int getIntermediaryGen() {
		return keratin.getIntermediaryGen().get();
	}

	private File pickFileForEnvironment(String minecraftVersion, Versioned<File> client, Versioned<File> server, Versioned<File> merged) {
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (details.sharedMappings()) {
			return merged.get(minecraftVersion);
		} else {
			if (details.client()) {
				return client.get(minecraftVersion);
			}
			if (details.server()) {
				return server.get(minecraftVersion);
			}
		}

		throw new RuntimeException("somehow Minecraft version " + minecraftVersion + " is neither client, server, nor merged!");
	}

	private File pickFileForPresentSides(String minecraftVersion, Versioned<File> client, Versioned<File> server, Versioned<File> merged) {
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (details.client() && details.server()) {
			return merged.get(minecraftVersion);
		} else {
			if (details.client()) {
				return client.get(minecraftVersion);
			}
			if (details.server()) {
				return server.get(minecraftVersion);
			}
		}

		throw new RuntimeException("somehow Minecraft version " + minecraftVersion + " is neither client nor server!");
	}

	public Set<File> getCacheDirectories() {
		return cacheDirs.stream().map(Property::get).collect(Collectors.toSet());
	}

	@Override
	public File getGlobalBuildCache() {
		return globalBuildCache.get();
	}

	@Override
	public File getLocalBuildCache() {
		return localBuildCache.get();
	}

	@Override
	public File getVersionJsonsCache() {
		return versionJsonsCache.get();
	}

	@Override
	public File getGameJarsCache() {
		return gameJarsCache.get();
	}

	@Override
	public File getMappedJarsCache() {
		return mappedJarsCache.get();
	}

	@Override
	public File getProcessedJarsCache() {
		return processedJarsCache.get();
	}

	@Override
	public File getLibrariesCache() {
		return librariesCache.get();
	}

	@Override
	public File getMappingsCache() {
		return mappingsCache.get();
	}

	@Override
	public File getProcessedMappingsCache() {
		return processedMappingsCache.get();
	}

	@Override
	public File getNestsCache() {
		return nestsCache.get();
	}

	@Override
	public File getSparrowCache() {
		return sparrowCache.get();
	}

	@Override
	public File getNestsBuildsCache() {
		return nestsBuildsCache.get();
	}

	@Override
	public File getSparrowBuildsCache() {
		return sparrowBuildsCache.get();
	}

	@Override
	public File getMappingsDirectory() {
		return mappingsDir.get();
	}

	@Override
	public File getMatchesDirectory() {
		return matchesDir.get();
	}

	@Override
	public File getRunDirectory(String minecraftVersion) {
		return runDir.get(minecraftVersion);
	}

	@Override
	public File getDecompiledSourceDirectory(String minecraftVersion) {
		return decompiledSrcDir.get(minecraftVersion);
	}

	@Override
	public File getFakeSourceDirectory(String minecraftVersion) {
		return fakeSrcDir.get(minecraftVersion);
	}

	@Override
	public File getJavadocDirectory(String minecraftVersion) {
		return javadocDir.get(minecraftVersion);
	}

	@Override
	public File getVersionsManifest() {
		return versionsManifest.get();
	}

	@Override
	public File getVersionInfo(String minecraftVersion) {
		return versionInfos.get(minecraftVersion);
	}

	@Override
	public File getVersionDetails(String minecraftVersion) {
		return versionDetails.get(minecraftVersion);
	}

	@Override
	public Collection<File> getLibraries(String minecraftVersion) {
		return project.getConfigurations().getByName(Configurations.minecraftLibraries(minecraftVersion)).getFiles();
	}

	@Override
	public File getClientJar(String minecraftVersion) {
		return clientJar.get(minecraftVersion);
	}

	@Override
	public File getServerJar(String minecraftVersion) {
		return serverJar.get(minecraftVersion);
	}

	@Override
	public File getMergedJar(String minecraftVersion) {
		return mergedJar.get(minecraftVersion);
	}

	@Override
	public File getMainJar(String minecraftVersion) {
		return pickFileForEnvironment(minecraftVersion, clientJar, serverJar, mergedJar);
	}

	@Override
	public File getIntermediaryClientJar(String minecraftVersion) {
		return intermediaryClientJar.get(minecraftVersion);
	}

	@Override
	public File getIntermediaryServerJar(String minecraftVersion) {
		return intermediaryServerJar.get(minecraftVersion);
	}

	@Override
	public File getIntermediaryMergedJar(String minecraftVersion) {
		return intermediaryMergedJar.get(minecraftVersion);
	}

	@Override
	public File getMainIntermediaryJar(String minecraftVersion) {
		return pickFileForPresentSides(minecraftVersion, intermediaryClientJar, intermediaryServerJar, intermediaryMergedJar);
	}

	@Override
	public File getNestedIntermediaryClientJar(String minecraftVersion) {
		return nestedIntermediaryClientJar.get(minecraftVersion);
	}

	@Override
	public File getNestedIntermediaryServerJar(String minecraftVersion) {
		return nestedIntermediaryServerJar.get(minecraftVersion);
	}

	@Override
	public File getNestedIntermediaryMergedJar(String minecraftVersion) {
		return nestedIntermediaryMergedJar.get(minecraftVersion);
	}

	@Override
	public File getMainNestedIntermediaryJar(String minecraftVersion) {
		return pickFileForPresentSides(minecraftVersion, nestedIntermediaryClientJar, nestedIntermediaryServerJar, nestedIntermediaryMergedJar);
	}

	@Override
	public File getSignaturePatchedIntermediaryClientJar(String minecraftVersion) {
		return signaturePatchedIntermediaryClientJar.get(minecraftVersion);
	}

	@Override
	public File getSignaturePatchedIntermediaryServerJar(String minecraftVersion) {
		return signaturePatchedIntermediaryServerJar.get(minecraftVersion);
	}

	@Override
	public File getSignaturePatchedIntermediaryMergedJar(String minecraftVersion) {
		return signaturePatchedIntermediaryMergedJar.get(minecraftVersion);
	}

	@Override
	public File getMainSignaturePatchedIntermediaryJar(String minecraftVersion) {
		return pickFileForPresentSides(minecraftVersion, signaturePatchedIntermediaryClientJar, signaturePatchedIntermediaryServerJar, signaturePatchedIntermediaryMergedJar);
	}

	@Override
	public File getProcessedIntermediaryClientJar(String minecraftVersion) {
		return processedIntermediaryClientJar.get(minecraftVersion);
	}

	@Override
	public File getProcessedIntermediaryServerJar(String minecraftVersion) {
		return processedIntermediaryServerJar.get(minecraftVersion);
	}

	@Override
	public File getProcessedIntermediaryMergedJar(String minecraftVersion) {
		return processedIntermediaryMergedJar.get(minecraftVersion);
	}

	@Override
	public File getMainProcessedIntermediaryJar(String minecraftVersion) {
		return pickFileForPresentSides(minecraftVersion, processedIntermediaryClientJar, processedIntermediaryServerJar, processedIntermediaryMergedJar);
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
	public File getClientIntermediaryMappings(String minecraftVersion) {
		return clientIntermediaryMappings.get(minecraftVersion);
	}

	@Override
	public File getServerIntermediaryMappings(String minecraftVersion) {
		return serverIntermediaryMappings.get(minecraftVersion);
	}

	@Override
	public File getMergedIntermediaryMappings(String minecraftVersion) {
		return mergedIntermediaryMappings.get(minecraftVersion);
	}

	@Override
	public File getMainIntermediaryMappings(String minecraftVersion) {
		return pickFileForPresentSides(minecraftVersion, clientIntermediaryMappings, serverIntermediaryMappings, mergedIntermediaryMappings);
	}

	@Override
	public File getNestedClientIntermediaryMappings(String minecraftVersion) {
		return nestedClientIntermediaryMappings.get(minecraftVersion);
	}

	@Override
	public File getNestedServerIntermediaryMappings(String minecraftVersion) {
		return nestedServerIntermediaryMappings.get(minecraftVersion);
	}

	@Override
	public File getNestedMergedIntermediaryMappings(String minecraftVersion) {
		return nestedMergedIntermediaryMappings.get(minecraftVersion);
	}

	@Override
	public File getMainNestedIntermediaryMappings(String minecraftVersion) {
		return pickFileForPresentSides(minecraftVersion, nestedClientIntermediaryMappings, nestedServerIntermediaryMappings, nestedMergedIntermediaryMappings);
	}

	@Override
	public File getProcessedClientIntermediaryMappings(String minecraftVersion) {
		return processedClientIntermediaryMappings.get(minecraftVersion);
	}

	@Override
	public File getProcessedServerIntermediaryMappings(String minecraftVersion) {
		return processedServerIntermediaryMappings.get(minecraftVersion);
	}

	@Override
	public File getProcessedMergedIntermediaryMappings(String minecraftVersion) {
		return processedMergedIntermediaryMappings.get(minecraftVersion);
	}

	@Override
	public File getMainProcessedIntermediaryMappings(String minecraftVersion) {
		return pickFileForPresentSides(minecraftVersion, processedClientIntermediaryMappings, processedServerIntermediaryMappings, processedMergedIntermediaryMappings);
	}

	@Override
	public File getNamedMappings(String minecraftVersion) {
		return namedMappings.get(minecraftVersion);
	}

	@Override
	public File getProcessedNamedMappings(String minecraftVersion) {
		return processedNamedMappings.get(minecraftVersion);
	}

	@Override
	public File getCompletedNamedMappings(String minecraftVersion) {
		return completedNamedMappings.get(minecraftVersion);
	}

	@Override
	public File getTinyV1NamedMappings(String minecraftVersion) {
		return tinyV1NamedMappings.get(minecraftVersion);
	}

	@Override
	public File getTinyV2NamedMappings(String minecraftVersion) {
		return tinyV2NamedMappings.get(minecraftVersion);
	}

	@Override
	public File getMergedTinyV1NamedMappings(String minecraftVersion) {
		return mergedTinyV1NamedMappings.get(minecraftVersion);
	}

	@Override
	public File getMergedTinyV2NamedMappings(String minecraftVersion) {
		return mergedTinyV2NamedMappings.get(minecraftVersion);
	}

	@Override
	public File getClientNests(String minecraftVersion) {
		return clientNests.get(minecraftVersion);
	}

	@Override
	public File getServerNests(String minecraftVersion) {
		return serverNests.get(minecraftVersion);
	}

	@Override
	public File getMergedNests(String minecraftVersion) {
		return mergedNests.get(minecraftVersion);
	}

	@Override
	public File getMainNests(String minecraftVersion) {
		return pickFileForEnvironment(minecraftVersion, clientNests, serverNests, mergedNests);
	}

	@Override
	public File getIntermediaryClientNests(String minecraftVersion) {
		return intermediaryClientNests.get(minecraftVersion);
	}

	@Override
	public File getIntermediaryServerNests(String minecraftVersion) {
		return intermediaryServerNests.get(minecraftVersion);
	}

	@Override
	public File getIntermediaryMergedNests(String minecraftVersion) {
		return intermediaryMergedNests.get(minecraftVersion);
	}

	@Override
	public File getMainIntermediaryNests(String minecraftVersion) {
		return pickFileForPresentSides(minecraftVersion, intermediaryClientNests, intermediaryServerNests, intermediaryMergedNests);
	}

	@Override
	public File getNamedNests(String minecraftVersion) {
		return namedNests.get(minecraftVersion);
	}

	@Override
	public File getClientSparrowFile(String minecraftVersion) {
		return clientSparrowFile.get(minecraftVersion);
	}

	@Override
	public File getServerSparrowFile(String minecraftVersion) {
		return serverSparrowFile.get(minecraftVersion);
	}

	@Override
	public File getMergedSparrowFile(String minecraftVersion) {
		return mergedSparrowFile.get(minecraftVersion);
	}

	@Override
	public File getMainSparrowFile(String minecraftVersion) {
		return pickFileForEnvironment(minecraftVersion, clientSparrowFile, serverSparrowFile, mergedSparrowFile);
	}

	@Override
	public File getIntermediaryClientSparrowFile(String minecraftVersion) {
		return intermediaryClientSparrowFile.get(minecraftVersion);
	}

	@Override
	public File getIntermediaryServerSparrowFile(String minecraftVersion) {
		return intermediaryServerSparrowFile.get(minecraftVersion);
	}

	@Override
	public File getIntermediaryMergedSparrowFile(String minecraftVersion) {
		return intermediaryMergedSparrowFile.get(minecraftVersion);
	}

	@Override
	public File getMainIntermediarySparrowFile(String minecraftVersion) {
		return pickFileForPresentSides(minecraftVersion, intermediaryClientSparrowFile, intermediaryServerSparrowFile, intermediaryMergedSparrowFile);
	}

	@Override
	public File getNamedSparrowFile(String minecraftVersion) {
		return namedSparrowFile.get(minecraftVersion);
	}
}
