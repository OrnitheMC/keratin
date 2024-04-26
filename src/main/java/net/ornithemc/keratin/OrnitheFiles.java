package net.ornithemc.keratin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;

import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.util.Versioned;

public class OrnitheFiles implements OrnitheFilesAPI {

	private final Project project;
	private final KeratinGradleExtension keratin;

	private final Property<File> globalCacheDir;
	private final Property<File> localCacheDir;

	private final Property<File> versionJsonsDir;
	private final Property<File> gameJarsDir;
	private final Property<File> mappedJarsDir;
	private final Property<File> processedJarsDir;
	private final Property<File> librariesDir;
	private final Property<File> mappingsDir;
	private final Property<File> processedMappingsDir;
	private final Property<File> nestsDir;
	private final Property<File> sparrowDir;

	private final Property<File> nestsBuildsCache;
	private final Property<File> sparrowBuildsCache;

	private final Property<File> versionsManifest;
	private final Versioned<File> versionInfos;
	private final Versioned<File> versionDetails;

	private final Versioned<List<File>> libraries;

	private final Versioned<File> clientJar;
	private final Versioned<File> serverJar;
	private final Versioned<File> mergedJar;
	private final Versioned<File> intermediaryClientJar;
	private final Versioned<File> intermediaryServerJar;
	private final Versioned<File> intermediaryMergedJar;
	private final Versioned<File> namedClientJar;
	private final Versioned<File> namedServerJar;
	private final Versioned<File> namedMergedJar;
	private final Versioned<File> nestedIntermediaryClientJar;
	private final Versioned<File> nestedIntermediaryServerJar;
	private final Versioned<File> nestedIntermediaryMergedJar;
	private final Versioned<File> signaturePatchedIntermediaryClientJar;
	private final Versioned<File> signaturePatchedIntermediaryServerJar;
	private final Versioned<File> signaturePatchedIntermediaryMergedJar;
	private final Versioned<File> processedIntermediaryClientJar;
	private final Versioned<File> processedIntermediaryServerJar;
	private final Versioned<File> processedIntermediaryMergedJar;
	private final Versioned<File> processedNamedClientJar;
	private final Versioned<File> processedNamedServerJar;
	private final Versioned<File> processedNamedMergedJar;

	private final Versioned<File> clientIntermediaryMappings;
	private final Versioned<File> serverIntermediaryMappings;
	private final Versioned<File> mergedIntermediaryMappings;
	private final Versioned<File> clientNamedMappings;
	private final Versioned<File> serverNamedMappings;
	private final Versioned<File> mergedNamedMappings;

	private final Versioned<File> clientNests;
	private final Versioned<File> serverNests;
	private final Versioned<File> mergedNests;
	private final Versioned<File> intermediaryClientNests;
	private final Versioned<File> intermediaryServerNests;
	private final Versioned<File> intermediaryMergedNests;

	private final Versioned<File> clientSparrowFile;
	private final Versioned<File> serverSparrowFile;
	private final Versioned<File> mergedSparrowFile;
	private final Versioned<File> intermediaryClientSparrowFile;
	private final Versioned<File> intermediaryServerSparrowFile;
	private final Versioned<File> intermediaryMergedSparrowFile;

	public OrnitheFiles(KeratinGradleExtension keratin) {
		this.project = keratin.getProject();
		this.keratin = keratin;

		this.globalCacheDir = fileProperty(() -> new File(this.project.getGradle().getGradleUserHomeDir(), "caches/%s".formatted(keratin.getGlobalCacheDir().get())));
		this.localCacheDir = fileProperty(() -> new File(this.project.getGradle().getGradleHomeDir(), keratin.getLocalCacheDir().get()));

		this.versionJsonsDir = fileProperty(() -> new File(getGlobalCacheDir(), "version-jsons"));
		this.gameJarsDir = fileProperty(() -> new File(getGlobalCacheDir(), "game-jars"));
		this.mappedJarsDir = fileProperty(() -> new File(getGlobalCacheDir(), "mapped-jars"));
		this.processedJarsDir = fileProperty(() -> new File(getGlobalCacheDir(), "processed-jars"));
		this.librariesDir = fileProperty(() -> new File(getGlobalCacheDir(), "libraries"));
		this.mappingsDir = fileProperty(() -> new File(getGlobalCacheDir(), "mappings"));
		this.processedMappingsDir = fileProperty(() -> new File(getGlobalCacheDir(), "processed-mappings"));
		this.nestsDir = fileProperty(() -> new File(getGlobalCacheDir(), "nests"));
		this.sparrowDir = fileProperty(() -> new File(getGlobalCacheDir(), "sparrow"));

		this.nestsBuildsCache = fileProperty(() -> this.project.file("nests-builds.json"));
		this.sparrowBuildsCache = fileProperty(() -> this.project.file("sparrow-builds.json"));

		this.versionsManifest = fileProperty(() -> new File(getGlobalCacheDir(), "versions-manifest.json"));
		this.versionInfos = new Versioned<>(minecraftVersion -> new File(getVersionJsonsDir(), "%s-info.json".formatted(minecraftVersion)));
		this.versionDetails = new Versioned<>(minecraftVersion -> new File(getVersionJsonsDir(), "%s-details.json".formatted(minecraftVersion)));

		this.libraries = new Versioned<>(minecraftVersion -> new ArrayList<>());

		this.clientJar = new Versioned<>(minecraftVersion -> new File(getGameJarsDir(), "%s-client.jar".formatted(minecraftVersion)));
		this.serverJar = new Versioned<>(minecraftVersion -> new File(getGameJarsDir(), "%s-server.jar".formatted(minecraftVersion)));
		this.mergedJar = new Versioned<>(minecraftVersion -> {
			if (!keratin.getVersionDetails(minecraftVersion).sharedMappings()) {
				throw new RuntimeException("game jars for Minecraft version " + minecraftVersion + " cannot be merged!");
			} else {
				return new File(getGameJarsDir(), "%s-merged.jar".formatted(minecraftVersion));
			}
		});
		this.intermediaryClientJar = new Versioned<>(minecraftVersion -> new File(getMappedJarsDir(), "%s-intermediary-gen%d-client.jar".formatted(minecraftVersion, getIntermediaryGen())));
		this.intermediaryServerJar = new Versioned<>(minecraftVersion -> new File(getMappedJarsDir(), "%s-intermediary-gen%d-server.jar".formatted(minecraftVersion, getIntermediaryGen())));
		this.intermediaryMergedJar = new Versioned<>(minecraftVersion -> new File(getMappedJarsDir(), "%s-intermediary-gen%d-merged.jar".formatted(minecraftVersion, getIntermediaryGen())));
		this.namedClientJar = new Versioned<>(minecraftVersion -> new File(getLocalCacheDir(), "%s-named-client.jar".formatted(minecraftVersion)));
		this.namedServerJar = new Versioned<>(minecraftVersion -> new File(getLocalCacheDir(), "%s-named-server.jar".formatted(minecraftVersion)));
		this.namedMergedJar = new Versioned<>(minecraftVersion -> new File(getLocalCacheDir(), "%s-named-merged.jar".formatted(minecraftVersion)));
		this.nestedIntermediaryClientJar = new Versioned<>(minecraftVersion -> {
			int build = keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT);

			if (build < 1) {
				return null;
			} else {
				return new File(getProcessedJarsDir(), "%s-nests+build.%d-intermediary-gen%d-client.jar".formatted(minecraftVersion, build, getIntermediaryGen()));
			}
		});
		this.nestedIntermediaryServerJar = new Versioned<>(minecraftVersion -> {
			int build = keratin.getNestsBuild(minecraftVersion, GameSide.SERVER);

			if (build < 1) {
				return null;
			} else {
				return new File(getProcessedJarsDir(), "%s-nests+build.%d-intermediary-gen%d-server.jar".formatted(minecraftVersion, build, getIntermediaryGen()));
			}
		});
		this.nestedIntermediaryMergedJar = new Versioned<>(minecraftVersion -> {
			int build = keratin.getNestsBuild(minecraftVersion, GameSide.MERGED);

			if (build < 1) {
				return null;
			} else {
				return new File(getProcessedJarsDir(), "%s-nests+build.%d-intermediary-gen%d-merged.jar".formatted(minecraftVersion, build, getIntermediaryGen()));
			}
		});
		this.signaturePatchedIntermediaryClientJar = new Versioned<>(minecraftVersion -> {
			int build = keratin.getSparrowBuild(minecraftVersion, GameSide.CLIENT);

			if (build < 1) {
				return null;
			} else {
				return new File(getProcessedJarsDir(), "%s-sparrow+build.%d-intermediary-gen%d-client.jar".formatted(minecraftVersion, build, getIntermediaryGen()));
			}
		});
		this.signaturePatchedIntermediaryServerJar = new Versioned<>(minecraftVersion -> {
			int build = keratin.getSparrowBuild(minecraftVersion, GameSide.SERVER);

			if (build < 1) {
				return null;
			} else {
				return new File(getProcessedJarsDir(), "%s-sparrow+build.%d-intermediary-gen%d-server.jar".formatted(minecraftVersion, build, getIntermediaryGen()));
			}
		});
		this.signaturePatchedIntermediaryMergedJar = new Versioned<>(minecraftVersion -> {
			int build = keratin.getSparrowBuild(minecraftVersion, GameSide.MERGED);

			if (build < 1) {
				return null;
			} else {
				return new File(getProcessedJarsDir(), "%s-sparrow+build.%d-intermediary-gen%d-merged.jar".formatted(minecraftVersion, build, getIntermediaryGen()));
			}
		});
		this.processedIntermediaryClientJar = new Versioned<>(minecraftVersion -> new File(getProcessedJarsDir(), "%s-processed-intermediary-gen%d-client.jar".formatted(minecraftVersion, getIntermediaryGen())));
		this.processedIntermediaryServerJar = new Versioned<>(minecraftVersion -> new File(getProcessedJarsDir(), "%s-processed-intermediary-gen%d-server.jar".formatted(minecraftVersion, getIntermediaryGen())));
		this.processedIntermediaryMergedJar = new Versioned<>(minecraftVersion -> new File(getProcessedJarsDir(), "%s-processed-intermediary-gen%d-merged.jar".formatted(minecraftVersion, getIntermediaryGen())));
		this.processedNamedClientJar = new Versioned<>(minecraftVersion -> new File(getLocalCacheDir(), "%s-processed-named-client.jar".formatted(minecraftVersion)));
		this.processedNamedServerJar = new Versioned<>(minecraftVersion -> new File(getLocalCacheDir(), "%s-processed-named-server.jar".formatted(minecraftVersion)));
		this.processedNamedMergedJar = new Versioned<>(minecraftVersion -> new File(getLocalCacheDir(), "%s-processed-named-merged.jar".formatted(minecraftVersion)));

		this.clientIntermediaryMappings = new Versioned<>(minecraftVersion -> new File(getMappingsDir(), "%s-intermediary-gen%d-client.tiny".formatted(minecraftVersion, getIntermediaryGen())));
		this.serverIntermediaryMappings = new Versioned<>(minecraftVersion -> new File(getMappingsDir(), "%s-intermediary-gen%d-server.tiny".formatted(minecraftVersion, getIntermediaryGen())));
		this.mergedIntermediaryMappings = new Versioned<>(minecraftVersion -> new File(getMappingsDir(), "%s-intermediary-gen%d-merged.tiny".formatted(minecraftVersion, getIntermediaryGen())));
		this.clientNamedMappings = new Versioned<>(minecraftVersion -> new File(getLocalCacheDir(), "%s-named-client.tiny".formatted(minecraftVersion)));
		this.serverNamedMappings = new Versioned<>(minecraftVersion -> new File(getLocalCacheDir(), "%s-named-server.tiny".formatted(minecraftVersion)));
		this.mergedNamedMappings = new Versioned<>(minecraftVersion -> new File(getLocalCacheDir(), "%s-named-merged.tiny".formatted(minecraftVersion)));

		this.clientNests = new Versioned<>(minecraftVersion -> {
			int build = keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT);

			if (build < 1) {
				return null;
			} else {
				return new File(getNestsDir(), "%s-nests+build.%d-client.nest".formatted(minecraftVersion, build));
			}
		});
		this.serverNests = new Versioned<>(minecraftVersion -> {
			int build = keratin.getNestsBuild(minecraftVersion, GameSide.SERVER);

			if (build < 1) {
				return null;
			} else {
				return new File(getNestsDir(), "%s-nests+build.%d-server.nest".formatted(minecraftVersion, build));
			}
		});
		this.mergedNests = new Versioned<>(minecraftVersion -> {
			int build = keratin.getNestsBuild(minecraftVersion, GameSide.MERGED);

			if (build < 1) {
				return null;
			} else {
				return new File(getNestsDir(), "%s-nests+build.%d-merged.nest".formatted(minecraftVersion, build));
			}
		});
		this.intermediaryClientNests = new Versioned<>(minecraftVersion -> {
			int build = keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT);

			if (build < 1) {
				return null;
			} else {
				return new File(getNestsDir(), "%s-intermediary-gen%d-nests+build.%d-client.nest".formatted(minecraftVersion, getIntermediaryGen(), build));
			}
		});
		this.intermediaryServerNests = new Versioned<>(minecraftVersion -> {
			int build = keratin.getNestsBuild(minecraftVersion, GameSide.SERVER);

			if (build < 1) {
				return null;
			} else {
				return new File(getNestsDir(), "%s-intermediary-gen%d-nests+build.%d-server.nest".formatted(minecraftVersion, getIntermediaryGen(), build));
			}
		});
		this.intermediaryMergedNests = new Versioned<>(minecraftVersion -> {
			int build = keratin.getNestsBuild(minecraftVersion, GameSide.MERGED);

			if (build < 1) {
				return null;
			} else {
				return new File(getNestsDir(), "%s-intermediary-gen%d-nests+build.%d-merged.nest".formatted(minecraftVersion, getIntermediaryGen(), build));
			}
		});

		this.clientSparrowFile = new Versioned<>(minecraftVersion -> {
			int build = keratin.getSparrowBuild(minecraftVersion, GameSide.CLIENT);

			if (build < 1) {
				return null;
			} else {
				return new File(getSparrowDir(), "%s-sparrow+build.%d-client.nest".formatted(minecraftVersion, build));
			}
		});
		this.serverSparrowFile = new Versioned<>(minecraftVersion -> {
			int build = keratin.getSparrowBuild(minecraftVersion, GameSide.SERVER);

			if (build < 1) {
				return null;
			} else {
				return new File(getSparrowDir(), "%s-sparrow+build.%d-server.nest".formatted(minecraftVersion, build));
			}
		});
		this.mergedSparrowFile = new Versioned<>(minecraftVersion -> {
			int build = keratin.getSparrowBuild(minecraftVersion, GameSide.MERGED);

			if (build < 1) {
				return null;
			} else {
				return new File(getSparrowDir(), "%s-sparrow+build.%d-merged.nest".formatted(minecraftVersion, build));
			}
		});
		this.intermediaryClientSparrowFile = new Versioned<>(minecraftVersion -> {
			int build = keratin.getSparrowBuild(minecraftVersion, GameSide.CLIENT);

			if (build < 1) {
				return null;
			} else {
				return new File(getSparrowDir(), "%s-intermediary-gen%d-sparrow+build.%d-client.nest".formatted(minecraftVersion, getIntermediaryGen(), build));
			}
		});
		this.intermediaryServerSparrowFile = new Versioned<>(minecraftVersion -> {
			int build = keratin.getSparrowBuild(minecraftVersion, GameSide.SERVER);

			if (build < 1) {
				return null;
			} else {
				return new File(getSparrowDir(), "%s-intermediary-gen%d-sparrow+build.%d-server.nest".formatted(minecraftVersion, getIntermediaryGen(), build));
			}
		});
		this.intermediaryMergedSparrowFile = new Versioned<>(minecraftVersion -> {
			int build = keratin.getSparrowBuild(minecraftVersion, GameSide.MERGED);

			if (build < 1) {
				return null;
			} else {
				return new File(getSparrowDir(), "%s-intermediary-gen%d-sparrow+build.%d-merged.nest".formatted(minecraftVersion, getIntermediaryGen(), build));
			}
		});
	}

	private Property<File> fileProperty(Callable<File> provider) {
		Property<File> property = this.project.getObjects().property(File.class);
		property.convention(this.project.provider(provider));
		property.finalizeValueOnRead();
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

	private File pickFileForNamespace(String minecraftVersion, String namespace, Versioned<File> official, Versioned<File> intermediary, Versioned<File> named) {
		Versioned<File> versioned = switch (namespace) {
			case "official"     -> official;
			case "intermediary" -> intermediary;
			case "named"        -> named;
			default             -> null;
		};
		if (versioned == null) {
			throw new IllegalStateException("invalid namespace " + namespace);
		}

		return versioned.get(minecraftVersion);
	}

	private File pickFileForNamespace(String namespace, File official, File intermediary, File named) {
		File file = switch (namespace) {
			case "official"     -> official;
			case "intermediary" -> intermediary;
			case "named"        -> named;
			default             -> null;
		};
		if (file == null) {
			throw new IllegalStateException("invalid namespace " + namespace);
		}

		return file;
	}

	@Override
	public File getGlobalCacheDir() {
		return globalCacheDir.get();
	}

	@Override
	public File getLocalCacheDir() {
		return localCacheDir.get();
	}

	@Override
	public File getVersionJsonsDir() {
		return versionJsonsDir.get();
	}

	@Override
	public File getGameJarsDir() {
		return gameJarsDir.get();
	}

	@Override
	public File getMappedJarsDir() {
		return mappedJarsDir.get();
	}

	@Override
	public File getProcessedJarsDir() {
		return processedJarsDir.get();
	}

	@Override
	public File getLibrariesDir() {
		return librariesDir.get();
	}

	@Override
	public File getMappingsDir() {
		return mappingsDir.get();
	}

	@Override
	public File getProcessedMappingsDir() {
		return processedMappingsDir.get();
	}

	@Override
	public File getNestsDir() {
		return nestsDir.get();
	}

	@Override
	public File getSparrowDir() {
		return sparrowDir.get();
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
	public List<File> getLibraries(String minecraftVersion) {
		return libraries.get(minecraftVersion);
	}

	public void addLibrary(String minecraftVersion, File library) {
		libraries.get(minecraftVersion).add(library);
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
	public File getNamedClientJar(String minecraftVersion) {
		return namedClientJar.get(minecraftVersion);
	}

	@Override
	public File getNamedServerJar(String minecraftVersion) {
		return namedServerJar.get(minecraftVersion);
	}

	@Override
	public File getNamedMergedJar(String minecraftVersion) {
		return namedMergedJar.get(minecraftVersion);
	}

	@Override
	public File getMainNamedJar(String minecraftVersion) {
		return pickFileForPresentSides(minecraftVersion, namedClientJar, namedServerJar, namedMergedJar);
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
	public File getProcessedNamedClientJar(String minecraftVersion) {
		return processedNamedClientJar.get(minecraftVersion);
	}

	@Override
	public File getProcessedNamedServerJar(String minecraftVersion) {
		return processedNamedServerJar.get(minecraftVersion);
	}

	@Override
	public File getProcessedNamedMergedJar(String minecraftVersion) {
		return processedNamedMergedJar.get(minecraftVersion);
	}

	@Override
	public File getMainProcessedNamedJar(String minecraftVersion) {
		return pickFileForPresentSides(minecraftVersion, processedNamedClientJar, processedNamedServerJar, processedNamedMergedJar);
	}

	@Override
	public File getClientJar(String minecraftVersion, String namespace) {
		return pickFileForNamespace(minecraftVersion, namespace, clientJar, intermediaryClientJar, namedClientJar);
	}

	@Override
	public File getServerJar(String minecraftVersion, String namespace) {
		return pickFileForNamespace(minecraftVersion, namespace, serverJar, intermediaryServerJar, namedServerJar);
	}

	@Override
	public File getMergedJar(String minecraftVersion, String namespace) {
		return pickFileForNamespace(minecraftVersion, namespace, mergedJar, intermediaryMergedJar, namedMergedJar);
	}

	@Override
	public File getMainJar(String minecraftVersion, String namespace) {
		return pickFileForNamespace(namespace, getMainJar(minecraftVersion), getMainIntermediaryJar(minecraftVersion), getMainNamedJar(minecraftVersion));
	}

	@Override
	public File getProcessedClientJar(String minecraftVersion, String namespace) {
		return pickFileForNamespace(minecraftVersion, namespace, null, processedIntermediaryClientJar, processedNamedClientJar);
	}

	@Override
	public File getProcessedServerJar(String minecraftVersion, String namespace) {
		return pickFileForNamespace(minecraftVersion, namespace, null, processedIntermediaryServerJar, processedNamedServerJar);
	}

	@Override
	public File getProcessedMergedJar(String minecraftVersion, String namespace) {
		return pickFileForNamespace(minecraftVersion, namespace, null, processedIntermediaryMergedJar, processedNamedMergedJar);
	}

	@Override
	public File getMainProcessedJar(String minecraftVersion, String namespace) {
		return pickFileForNamespace(namespace, null, getMainProcessedIntermediaryJar(minecraftVersion), getMainProcessedNamedJar(minecraftVersion));
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
	public File getClientNamedMappings(String minecraftVersion) {
		return clientNamedMappings.get(minecraftVersion);
	}

	@Override
	public File getServerNamedMappings(String minecraftVersion) {
		return serverNamedMappings.get(minecraftVersion);
	}

	@Override
	public File getMergedNamedMappings(String minecraftVersion) {
		return mergedNamedMappings.get(minecraftVersion);
	}

	private File pickMappingsForTargetNamespace(String minecraftVersion, String targetNamespace, Versioned<File> intermediary, Versioned<File> named) {
		if ("official".equals(targetNamespace)) {
			throw new IllegalStateException("mappings for " + targetNamespace + " do not exist!");
		}

		return pickFileForNamespace(minecraftVersion, targetNamespace, null, intermediary, named);
	}

	@Override
	public File getClientMappings(String minecraftVersion, String targetNamespace) {
		return pickMappingsForTargetNamespace(minecraftVersion, targetNamespace, clientIntermediaryMappings, clientNamedMappings);
	}

	@Override
	public File getServerMappings(String minecraftVersion, String targetNamespace) {
		return pickMappingsForTargetNamespace(minecraftVersion, targetNamespace, serverIntermediaryMappings, serverNamedMappings);
	}

	@Override
	public File getMergedMappings(String minecraftVersion, String targetNamespace) {
		return pickMappingsForTargetNamespace(minecraftVersion, targetNamespace, mergedIntermediaryMappings, mergedNamedMappings);
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
}
