package net.ornithemc.keratin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.manifest.VersionDetails;

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

	private final Property<File> versionsManifest;
	private final Property<File> versionInfo;
	private final Property<File> versionDetails;

	private final ListProperty<File> libraries;

	private final Property<File> nestsBuildsCache;
	private final Property<File> sparrowBuildsCache;

	private final Property<File> clientJar;
	private final Property<File> serverJar;
	private final Property<File> mergedJar;
	private final Property<File> intermediaryClientJar;
	private final Property<File> intermediaryServerJar;
	private final Property<File> intermediaryMergedJar;
	private final Property<File> namedClientJar;
	private final Property<File> namedServerJar;
	private final Property<File> namedMergedJar;
	private final Property<File> nestedIntermediaryClientJar;
	private final Property<File> nestedIntermediaryServerJar;
	private final Property<File> nestedIntermediaryMergedJar;
	private final Property<File> signaturePatchedIntermediaryClientJar;
	private final Property<File> signaturePatchedIntermediaryServerJar;
	private final Property<File> signaturePatchedIntermediaryMergedJar;
	private final Property<File> processedIntermediaryClientJar;
	private final Property<File> processedIntermediaryServerJar;
	private final Property<File> processedIntermediaryMergedJar;
	private final Property<File> processedNamedClientJar;
	private final Property<File> processedNamedServerJar;
	private final Property<File> processedNamedMergedJar;

	private final Property<File> clientIntermediaryMappings;
	private final Property<File> serverIntermediaryMappings;
	private final Property<File> mergedIntermediaryMappings;
	private final Property<File> clientNamedMappings;
	private final Property<File> serverNamedMappings;
	private final Property<File> mergedNamedMappings;

	private final Property<File> clientNests;
	private final Property<File> serverNests;
	private final Property<File> mergedNests;
	private final Property<File> intermediaryClientNests;
	private final Property<File> intermediaryServerNests;
	private final Property<File> intermediaryMergedNests;

	private final Property<File> clientSparrowFile;
	private final Property<File> serverSparrowFile;
	private final Property<File> mergedSparrowFile;
	private final Property<File> intermediaryClientSparrowFile;
	private final Property<File> intermediaryServerSparrowFile;
	private final Property<File> intermediaryMergedSparrowFile;

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

		this.versionsManifest = fileProperty(() -> new File(getGlobalCacheDir(), "versions-manifest.json"));
		this.versionInfo = fileProperty(() -> new File(getVersionJsonsDir(), "%s-info.json".formatted(getMinecraftVersion())));
		this.versionDetails = fileProperty(() -> new File(getVersionJsonsDir(), "%s-details.json".formatted(getMinecraftVersion())));

		this.libraries = this.project.getObjects().listProperty(File.class);
		this.libraries.set(new ArrayList<>());

		this.nestsBuildsCache = fileProperty(() -> this.project.file("nests-builds.json"));
		this.sparrowBuildsCache = fileProperty(() -> this.project.file("sparrow-builds.json"));

		this.clientJar = fileProperty(() -> new File(getGameJarsDir(), "%s-client.jar".formatted(getMinecraftVersion())));
		this.serverJar = fileProperty(() -> new File(getGameJarsDir(), "%s-server.jar".formatted(getMinecraftVersion())));
		this.mergedJar = fileProperty(() -> {
			if (!keratin.getVersionDetails().sharedMappings()) {
				throw new RuntimeException("game jars for Minecraft version " + getMinecraftVersion() + " cannot be merged!");
			} else {
				return new File(getGameJarsDir(), "%s-merged.jar".formatted(getMinecraftVersion()));
			}
		});
		this.intermediaryClientJar = fileProperty(() -> new File(getMappedJarsDir(), "%s-intermediary-gen%d-client.jar".formatted(getMinecraftVersion(), getIntermediaryGen())));
		this.intermediaryServerJar = fileProperty(() -> new File(getMappedJarsDir(), "%s-intermediary-gen%d-server.jar".formatted(getMinecraftVersion(), getIntermediaryGen())));
		this.intermediaryMergedJar = fileProperty(() -> new File(getMappedJarsDir(), "%s-intermediary-gen%d-merged.jar".formatted(getMinecraftVersion(), getIntermediaryGen())));
		this.namedClientJar = fileProperty(() -> new File(getLocalCacheDir(), "%s-named-client.jar".formatted(getMinecraftVersion())));
		this.namedServerJar = fileProperty(() -> new File(getLocalCacheDir(), "%s-named-server.jar".formatted(getMinecraftVersion())));
		this.namedMergedJar = fileProperty(() -> new File(getLocalCacheDir(), "%s-named-merged.jar".formatted(getMinecraftVersion())));
		this.nestedIntermediaryClientJar = fileProperty(() -> {
			int build = keratin.getNestsBuilds().getting(GameSide.CLIENT).getOrElse(-1);

			if (build < 1) {
				return null;
			} else {
				return new File(getProcessedJarsDir(), "%s-nests+build.%d-intermediary-gen%d-client.jar".formatted(getMinecraftVersion(), build, getIntermediaryGen()));
			}
		});
		this.nestedIntermediaryServerJar = fileProperty(() -> {
			int build = keratin.getNestsBuilds().getting(GameSide.SERVER).getOrElse(-1);

			if (build < 1) {
				return null;
			} else {
				return new File(getProcessedJarsDir(), "%s-nests+build.%d-intermediary-gen%d-server.jar".formatted(getMinecraftVersion(), build, getIntermediaryGen()));
			}
		});
		this.nestedIntermediaryMergedJar = fileProperty(() -> {
			int build = keratin.getNestsBuilds().getting(GameSide.MERGED).getOrElse(-1);

			if (build < 1) {
				return null;
			} else {
				return new File(getProcessedJarsDir(), "%s-nests+build.%d-intermediary-gen%d-merged.jar".formatted(getMinecraftVersion(), build, getIntermediaryGen()));
			}
		});
		this.signaturePatchedIntermediaryClientJar = fileProperty(() -> {
			int build = keratin.getSparrowBuilds().getting(GameSide.CLIENT).getOrElse(-1);

			if (build < 1) {
				return null;
			} else {
				return new File(getProcessedJarsDir(), "%s-sparrow+build.%d-intermediary-gen%d-client.jar".formatted(getMinecraftVersion(), build, getIntermediaryGen()));
			}
		});
		this.signaturePatchedIntermediaryServerJar = fileProperty(() -> {
			int build = keratin.getSparrowBuilds().getting(GameSide.SERVER).getOrElse(-1);

			if (build < 1) {
				return null;
			} else {
				return new File(getProcessedJarsDir(), "%s-sparrow+build.%d-intermediary-gen%d-server.jar".formatted(getMinecraftVersion(), build, getIntermediaryGen()));
			}
		});
		this.signaturePatchedIntermediaryMergedJar = fileProperty(() -> {
			int build = keratin.getSparrowBuilds().getting(GameSide.MERGED).getOrElse(-1);

			if (build < 1) {
				return null;
			} else {
				return new File(getProcessedJarsDir(), "%s-sparrow+build.%d-intermediary-gen%d-merged.jar".formatted(getMinecraftVersion(), build, getIntermediaryGen()));
			}
		});
		this.processedIntermediaryClientJar = fileProperty(() -> new File(getProcessedJarsDir(), "%s-processed-intermediary-gen%d-client.jar".formatted(getMinecraftVersion(), getIntermediaryGen())));
		this.processedIntermediaryServerJar = fileProperty(() -> new File(getProcessedJarsDir(), "%s-processed-intermediary-gen%d-server.jar".formatted(getMinecraftVersion(), getIntermediaryGen())));
		this.processedIntermediaryMergedJar = fileProperty(() -> new File(getProcessedJarsDir(), "%s-processed-intermediary-gen%d-merged.jar".formatted(getMinecraftVersion(), getIntermediaryGen())));
		this.processedNamedClientJar = fileProperty(() -> new File(getLocalCacheDir(), "%s-processed-named-client.jar".formatted(getMinecraftVersion())));
		this.processedNamedServerJar = fileProperty(() -> new File(getLocalCacheDir(), "%s-processed-named-server.jar".formatted(getMinecraftVersion())));
		this.processedNamedMergedJar = fileProperty(() -> new File(getLocalCacheDir(), "%s-processed-named-merged.jar".formatted(getMinecraftVersion())));

		this.clientIntermediaryMappings = fileProperty(() -> new File(getMappingsDir(), "%s-intermediary-gen%d-client.tiny".formatted(getMinecraftVersion(), getIntermediaryGen())));
		this.serverIntermediaryMappings = fileProperty(() -> new File(getMappingsDir(), "%s-intermediary-gen%d-server.tiny".formatted(getMinecraftVersion(), getIntermediaryGen())));
		this.mergedIntermediaryMappings = fileProperty(() -> new File(getMappingsDir(), "%s-intermediary-gen%d-merged.tiny".formatted(getMinecraftVersion(), getIntermediaryGen())));
		this.clientNamedMappings = fileProperty(() -> new File(getLocalCacheDir(), "%s-named-client.tiny".formatted(getMinecraftVersion())));
		this.serverNamedMappings = fileProperty(() -> new File(getLocalCacheDir(), "%s-named-server.tiny".formatted(getMinecraftVersion())));
		this.mergedNamedMappings = fileProperty(() -> new File(getLocalCacheDir(), "%s-named-merged.tiny".formatted(getMinecraftVersion())));

		this.clientNests = fileProperty(() -> {
			int build = keratin.getNestsBuilds().getting(GameSide.CLIENT).getOrElse(-1);

			if (build < 1) {
				return null;
			} else {
				return new File(getNestsDir(), "%s-nests+build.%d-client.nest".formatted(getMinecraftVersion(), build));
			}
		});
		this.serverNests = fileProperty(() -> {
			int build = keratin.getNestsBuilds().getting(GameSide.SERVER).getOrElse(-1);

			if (build < 1) {
				return null;
			} else {
				return new File(getNestsDir(), "%s-nests+build.%d-server.nest".formatted(getMinecraftVersion(), build));
			}
		});
		this.mergedNests = fileProperty(() -> {
			int build = keratin.getNestsBuilds().getting(GameSide.MERGED).getOrElse(-1);

			if (build < 1) {
				return null;
			} else {
				return new File(getNestsDir(), "%s-nests+build.%d-merged.nest".formatted(getMinecraftVersion(), build));
			}
		});
		this.intermediaryClientNests = fileProperty(() -> {
			int build = keratin.getNestsBuilds().getting(GameSide.CLIENT).getOrElse(-1);

			if (build < 1) {
				return null;
			} else {
				return new File(getNestsDir(), "%s-intermediary-gen%d-nests+build.%d-client.nest".formatted(getMinecraftVersion(), getIntermediaryGen(), build));
			}
		});
		this.intermediaryServerNests = fileProperty(() -> {
			int build = keratin.getNestsBuilds().getting(GameSide.SERVER).getOrElse(-1);

			if (build < 1) {
				return null;
			} else {
				return new File(getNestsDir(), "%s-intermediary-gen%d-nests+build.%d-server.nest".formatted(getMinecraftVersion(), getIntermediaryGen(), build));
			}
		});
		this.intermediaryMergedNests = fileProperty(() -> {
			int build = keratin.getNestsBuilds().getting(GameSide.MERGED).getOrElse(-1);

			if (build < 1) {
				return null;
			} else {
				return new File(getNestsDir(), "%s-intermediary-gen%d-nests+build.%d-merged.nest".formatted(getMinecraftVersion(), getIntermediaryGen(), build));
			}
		});

		this.clientSparrowFile = fileProperty(() -> {
			int build = keratin.getSparrowBuilds().getting(GameSide.CLIENT).getOrElse(-1);

			if (build < 1) {
				return null;
			} else {
				return new File(getSparrowDir(), "%s-sparrow+build.%d-client.nest".formatted(getMinecraftVersion(), build));
			}
		});
		this.serverSparrowFile = fileProperty(() -> {
			int build = keratin.getSparrowBuilds().getting(GameSide.SERVER).getOrElse(-1);

			if (build < 1) {
				return null;
			} else {
				return new File(getSparrowDir(), "%s-sparrow+build.%d-server.nest".formatted(getMinecraftVersion(), build));
			}
		});
		this.mergedSparrowFile = fileProperty(() -> {
			int build = keratin.getSparrowBuilds().getting(GameSide.MERGED).getOrElse(-1);

			if (build < 1) {
				return null;
			} else {
				return new File(getSparrowDir(), "%s-sparrow+build.%d-merged.nest".formatted(getMinecraftVersion(), build));
			}
		});
		this.intermediaryClientSparrowFile = fileProperty(() -> {
			int build = keratin.getSparrowBuilds().getting(GameSide.CLIENT).getOrElse(-1);

			if (build < 1) {
				return null;
			} else {
				return new File(getSparrowDir(), "%s-intermediary-gen%d-sparrow+build.%d-client.nest".formatted(getMinecraftVersion(), getIntermediaryGen(), build));
			}
		});
		this.intermediaryServerSparrowFile = fileProperty(() -> {
			int build = keratin.getSparrowBuilds().getting(GameSide.SERVER).getOrElse(-1);

			if (build < 1) {
				return null;
			} else {
				return new File(getSparrowDir(), "%s-intermediary-gen%d-sparrow+build.%d-server.nest".formatted(getMinecraftVersion(), getIntermediaryGen(), build));
			}
		});
		this.intermediaryMergedSparrowFile = fileProperty(() -> {
			int build = keratin.getSparrowBuilds().getting(GameSide.MERGED).getOrElse(-1);

			if (build < 1) {
				return null;
			} else {
				return new File(getSparrowDir(), "%s-intermediary-gen%d-sparrow+build.%d-merged.nest".formatted(getMinecraftVersion(), getIntermediaryGen(), build));
			}
		});
	}

	private Property<File> fileProperty(Callable<File> provider) {
		Property<File> property = this.project.getObjects().property(File.class);
		property.convention(this.project.provider(provider));
		property.finalizeValueOnRead();
		return property;
	}

	private String getMinecraftVersion() {
		return keratin.getMinecraftVersion().get();
	}

	private int getIntermediaryGen() {
		return keratin.getIntermediaryGen().get();
	}

	private File pickFileForEnvironment(Property<File> client, Property<File> server, Property<File> merged) {
		VersionDetails details = keratin.getVersionDetails();

		if (details.sharedMappings()) {
			return merged.get();
		} else {
			if (details.client()) {
				return client.get();
			}
			if (details.server()) {
				return server.get();
			}
		}

		throw new RuntimeException("somehow Minecraft version " + getMinecraftVersion() + " is neither client, server, nor merged!");
	}

	private File pickFileForPresentSides(Property<File> client, Property<File> server, Property<File> merged) {
		VersionDetails details = keratin.getVersionDetails();

		if (details.client() && details.server()) {
			return merged.get();
		} else {
			if (details.client()) {
				return client.get();
			}
			if (details.server()) {
				return server.get();
			}
		}

		throw new RuntimeException("somehow Minecraft version " + getMinecraftVersion() + " is neither client nor server!");
	}

	private File pickFileForNamespace(String namespace, Property<File> official, Property<File> intermediary, Property<File> named) {
		Property<File> property = switch (namespace) {
			case "official"     -> official;
			case "intermediary" -> intermediary;
			case "named"        -> named;
			default             -> null;
		};
		if (property == null) {
			throw new IllegalStateException("invalid namespace " + namespace);
		}

		return property.get();
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
	public File getVersionsManifest() {
		return versionsManifest.get();
	}

	@Override
	public File getVersionInfo() {
		return versionInfo.get();
	}

	@Override
	public File getVersionDetails() {
		return versionDetails.get();
	}

	@Override
	public List<File> getLibraries() {
		return libraries.get();
	}

	public void addLibrary(File library) {
		libraries.add(library);
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
	public File getClientJar() {
		return clientJar.get();
	}

	@Override
	public File getServerJar() {
		return serverJar.get();
	}

	@Override
	public File getMergedJar() {
		return mergedJar.get();
	}

	@Override
	public File getMainJar() {
		return pickFileForEnvironment(clientJar, serverJar, mergedJar);
	}

	@Override
	public File getIntermediaryClientJar() {
		return intermediaryClientJar.get();
	}

	@Override
	public File getIntermediaryServerJar() {
		return intermediaryServerJar.get();
	}

	@Override
	public File getIntermediaryMergedJar() {
		return intermediaryMergedJar.get();
	}

	@Override
	public File getMainIntermediaryJar() {
		return pickFileForPresentSides(intermediaryClientJar, intermediaryServerJar, intermediaryMergedJar);
	}

	@Override
	public File getNamedClientJar() {
		return namedClientJar.get();
	}

	@Override
	public File getNamedServerJar() {
		return namedServerJar.get();
	}

	@Override
	public File getNamedMergedJar() {
		return namedMergedJar.get();
	}

	@Override
	public File getMainNamedJar() {
		return pickFileForPresentSides(namedClientJar, namedServerJar, namedMergedJar);
	}

	@Override
	public File getNestedIntermediaryClientJar() {
		return nestedIntermediaryClientJar.get();
	}

	@Override
	public File getNestedIntermediaryServerJar() {
		return nestedIntermediaryServerJar.get();
	}

	@Override
	public File getNestedIntermediaryMergedJar() {
		return nestedIntermediaryMergedJar.get();
	}

	@Override
	public File getMainNestedIntermediaryJar() {
		return pickFileForPresentSides(nestedIntermediaryClientJar, nestedIntermediaryServerJar, nestedIntermediaryMergedJar);
	}

	@Override
	public File getSignaturePatchedIntermediaryClientJar() {
		return signaturePatchedIntermediaryClientJar.get();
	}

	@Override
	public File getSignaturePatchedIntermediaryServerJar() {
		return signaturePatchedIntermediaryServerJar.get();
	}

	@Override
	public File getSignaturePatchedIntermediaryMergedJar() {
		return signaturePatchedIntermediaryMergedJar.get();
	}

	@Override
	public File getMainSignaturePatchedIntermediaryJar() {
		return pickFileForPresentSides(signaturePatchedIntermediaryClientJar, signaturePatchedIntermediaryServerJar, signaturePatchedIntermediaryMergedJar);
	}

	@Override
	public File getProcessedIntermediaryClientJar() {
		return processedIntermediaryClientJar.get();
	}

	@Override
	public File getProcessedIntermediaryServerJar() {
		return processedIntermediaryServerJar.get();
	}

	@Override
	public File getProcessedIntermediaryMergedJar() {
		return processedIntermediaryMergedJar.get();
	}

	@Override
	public File getMainProcessedIntermediaryJar() {
		return pickFileForPresentSides(processedIntermediaryClientJar, processedIntermediaryServerJar, processedIntermediaryMergedJar);
	}

	@Override
	public File getProcessedNamedClientJar() {
		return processedNamedClientJar.get();
	}

	@Override
	public File getProcessedNamedServerJar() {
		return processedNamedServerJar.get();
	}

	@Override
	public File getProcessedNamedMergedJar() {
		return processedNamedMergedJar.get();
	}

	@Override
	public File getMainProcessedNamedJar() {
		return pickFileForPresentSides(processedNamedClientJar, processedNamedServerJar, processedNamedMergedJar);
	}

	@Override
	public File getClientJar(String namespace) {
		return pickFileForNamespace(namespace, clientJar, intermediaryClientJar, namedClientJar);
	}

	@Override
	public File getServerJar(String namespace) {
		return pickFileForNamespace(namespace, serverJar, intermediaryServerJar, namedServerJar);
	}

	@Override
	public File getMergedJar(String namespace) {
		return pickFileForNamespace(namespace, mergedJar, intermediaryMergedJar, namedMergedJar);
	}

	@Override
	public File getMainJar(String namespace) {
		return pickFileForNamespace(namespace, getMainJar(), getMainIntermediaryJar(), getMainNamedJar());
	}

	@Override
	public File getProcessedClientJar(String namespace) {
		return pickFileForNamespace(namespace, null, processedIntermediaryClientJar, processedNamedClientJar);
	}

	@Override
	public File getProcessedServerJar(String namespace) {
		return pickFileForNamespace(namespace, null, processedIntermediaryServerJar, processedNamedServerJar);
	}

	@Override
	public File getProcessedMergedJar(String namespace) {
		return pickFileForNamespace(namespace, null, processedIntermediaryMergedJar, processedNamedMergedJar);
	}

	@Override
	public File getMainProcessedJar(String namespace) {
		return pickFileForNamespace(namespace, null, getMainProcessedIntermediaryJar(), getMainProcessedNamedJar());
	}

	@Override
	public File getClientIntermediaryMappings() {
		return clientIntermediaryMappings.get();
	}

	@Override
	public File getServerIntermediaryMappings() {
		return serverIntermediaryMappings.get();
	}

	@Override
	public File getMergedIntermediaryMappings() {
		return mergedIntermediaryMappings.get();
	}

	@Override
	public File getClientNamedMappings() {
		return clientNamedMappings.get();
	}

	@Override
	public File getServerNamedMappings() {
		return serverNamedMappings.get();
	}

	@Override
	public File getMergedNamedMappings() {
		return mergedNamedMappings.get();
	}

	private File pickMappingsForTargetNamespace(String targetNamespace, Property<File> intermediary, Property<File> named) {
		if ("official".equals(targetNamespace)) {
			throw new IllegalStateException("mappings for " + targetNamespace + " do not exist!");
		}

		return pickFileForNamespace(targetNamespace, null, intermediary, named);
	}

	@Override
	public File getClientMappings(String targetNamespace) {
		return pickMappingsForTargetNamespace(targetNamespace, clientIntermediaryMappings, clientNamedMappings);
	}

	@Override
	public File getServerMappings(String targetNamespace) {
		return pickMappingsForTargetNamespace(targetNamespace, serverIntermediaryMappings, serverNamedMappings);
	}

	@Override
	public File getMergedMappings(String targetNamespace) {
		return pickMappingsForTargetNamespace(targetNamespace, mergedIntermediaryMappings, mergedNamedMappings);
	}

	@Override
	public File getClientNests() {
		return clientNests.get();
	}

	@Override
	public File getServerNests() {
		return serverNests.get();
	}

	@Override
	public File getMergedNests() {
		return mergedNests.get();
	}

	@Override
	public File getIntermediaryClientNests() {
		return intermediaryClientNests.get();
	}

	@Override
	public File getIntermediaryServerNests() {
		return intermediaryServerNests.get();
	}

	@Override
	public File getIntermediaryMergedNests() {
		return intermediaryMergedNests.get();
	}

	@Override
	public File getMainIntermediaryNests() {
		return pickFileForPresentSides(intermediaryClientNests, intermediaryServerNests, intermediaryMergedNests);
	}

	@Override
	public File getClientSparrowFile() {
		return clientSparrowFile.get();
	}

	@Override
	public File getServerSparrowFile() {
		return serverSparrowFile.get();
	}

	@Override
	public File getMergedSparrowFile() {
		return mergedSparrowFile.get();
	}

	@Override
	public File getIntermediaryClientSparrowFile() {
		return intermediaryClientSparrowFile.get();
	}

	@Override
	public File getIntermediaryServerSparrowFile() {
		return intermediaryServerSparrowFile.get();
	}

	@Override
	public File getIntermediaryMergedSparrowFile() {
		return intermediaryMergedSparrowFile.get();
	}

	@Override
	public File getMainIntermediarySparrowFile() {
		return pickFileForPresentSides(intermediaryClientSparrowFile, intermediaryServerSparrowFile, intermediaryMergedSparrowFile);
	}
}
