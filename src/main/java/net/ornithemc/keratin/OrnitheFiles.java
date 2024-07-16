package net.ornithemc.keratin;

import java.io.File;
import java.nio.file.NoSuchFileException;
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
import net.ornithemc.keratin.api.task.enigma.EnigmaSession;
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
	private final Property<File> ravenCache;
	private final Property<File> sparrowCache;
	private final Property<File> nestsCache;

	private final Property<File> ravenBuildsCache;
	private final Property<File> sparrowBuildsCache;
	private final Property<File> nestsBuildsCache;
	private final Property<File> enigmaProfile;
	private final Property<File> mappingsDir;
	private final Property<File> exceptionsDir;
	private final Property<File> signaturesDir;
	private final Property<File> matchesDir;
	private final Property<File> runDir;
	private final Versioned<File> workingDir;
	private final Versioned<File> enigmaSessionLock;
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
	private final Versioned<File> exceptionsPatchedIntermediaryClientJar;
	private final Versioned<File> exceptionsPatchedIntermediaryServerJar;
	private final Versioned<File> exceptionsPatchedIntermediaryMergedJar;
	private final Versioned<File> signaturePatchedIntermediaryClientJar;
	private final Versioned<File> signaturePatchedIntermediaryServerJar;
	private final Versioned<File> signaturePatchedIntermediaryMergedJar;
	private final Versioned<File> nestedIntermediaryClientJar;
	private final Versioned<File> nestedIntermediaryServerJar;
	private final Versioned<File> nestedIntermediaryMergedJar;
	private final Versioned<File> processedIntermediaryClientJar;
	private final Versioned<File> processedIntermediaryServerJar;
	private final Versioned<File> processedIntermediaryMergedJar;

	private final Versioned<File> namedJar;
	private final Versioned<File> processedNamedJar;

	private final Versioned<File> clientIntermediaryMappings;
	private final Versioned<File> serverIntermediaryMappings;
	private final Versioned<File> mergedIntermediaryMappings;

	private final Versioned<File> namedMappings;
	private final Versioned<File> processedNamedMappings;
	private final Versioned<File> completedNamedMappings;
	private final Versioned<File> tinyV1NamedMappings;
	private final Versioned<File> tinyV2NamedMappings;
	private final Versioned<File> mergedTinyV1NamedMappings;
	private final Versioned<File> mergedTinyV2NamedMappings;

	private final Versioned<File> clientRavenFile;
	private final Versioned<File> serverRavenFile;
	private final Versioned<File> mergedRavenFile;
	private final Versioned<File> intermediaryClientRavenFile;
	private final Versioned<File> intermediaryServerRavenFile;
	private final Versioned<File> intermediaryMergedRavenFile;

	private final Versioned<File> namedRavenFile;

	private final Versioned<File> clientSparrowFile;
	private final Versioned<File> serverSparrowFile;
	private final Versioned<File> mergedSparrowFile;
	private final Versioned<File> intermediaryClientSparrowFile;
	private final Versioned<File> intermediaryServerSparrowFile;
	private final Versioned<File> intermediaryMergedSparrowFile;

	private final Versioned<File> namedSparrowFile;

	private final Versioned<File> clientNests;
	private final Versioned<File> serverNests;
	private final Versioned<File> mergedNests;
	private final Versioned<File> intermediaryClientNests;
	private final Versioned<File> intermediaryServerNests;
	private final Versioned<File> intermediaryMergedNests;

	private final Versioned<File> namedNests;

	private final Versioned<File> featherMappings;

	private final Versioned<File> clientExceptions;
	private final Versioned<File> serverExceptions;
	private final Versioned<File> mergedExceptions;
	private final Versioned<File> clientSignatures;
	private final Versioned<File> serverSignatures;
	private final Versioned<File> mergedSignatures;

	private final Versioned<File> setupClientIntermediaryMappings;
	private final Versioned<File> setupServerIntermediaryMappings;
	private final Versioned<File> setupMergedIntermediaryMappings;
	private final Versioned<File> setupClientNamedMappings;
	private final Versioned<File> setupServerNamedMappings;
	private final Versioned<File> setupMergedNamedMappings;
	private final Versioned<File> combinedSetupClientMappings;
	private final Versioned<File> combinedSetupServerMappings;
	private final Versioned<File> combinedSetupMergedMappings;

	private final Versioned<File> setupClientExceptions;
	private final Versioned<File> setupServerExceptions;
	private final Versioned<File> setupMergedExceptions;
	private final Versioned<File> setupClientSignatures;
	private final Versioned<File> setupServerSignatures;
	private final Versioned<File> setupMergedSignatures;

	private final Versioned<File> setupClientJar;
	private final Versioned<File> setupServerJar;
	private final Versioned<File> setupMergedJar;
	private final Versioned<File> namedSetupClientJar;
	private final Versioned<File> namedSetupServerJar;
	private final Versioned<File> namedSetupMergedJar;

	private final Versioned<File> baseClientExceptions;
	private final Versioned<File> baseServerExceptions;
	private final Versioned<File> baseMergedExceptions;
	private final Versioned<File> baseClientSignatures;
	private final Versioned<File> baseServerSignatures;
	private final Versioned<File> baseMergedSignatures;

	private final Versioned<File> generatedClientJar;
	private final Versioned<File> generatedServerJar;
	private final Versioned<File> generatedMergedJar;
	private final Versioned<File> namedGeneratedClientJar;
	private final Versioned<File> namedGeneratedServerJar;
	private final Versioned<File> namedGeneratedMergedJar;

	private final Versioned<File> generatedClientExceptions;
	private final Versioned<File> generatedServerExceptions;
	private final Versioned<File> generatedMergedExceptions;
	private final Versioned<File> generatedClientSignatures;
	private final Versioned<File> generatedServerSignatures;
	private final Versioned<File> generatedMergedSignatures;

	public OrnitheFiles(KeratinGradleExtension keratin) {
		this.project = keratin.getProject();
		this.keratin = keratin;

		this.cacheDirs = new LinkedHashSet<>();

		this.globalBuildCache = cacheDirectoryProperty(() -> new File(this.project.getGradle().getGradleUserHomeDir(), "caches/%s".formatted(keratin.getGlobalCacheDirectory().get())));
		this.localBuildCache = cacheDirectoryProperty(() -> this.project.getLayout().getBuildDirectory().dir(keratin.getLocalCacheDirectory().get()).get().getAsFile());

		this.versionJsonsCache = cacheDirectoryProperty(() -> new File(getGlobalBuildCache(), "version-jsons"));
		this.gameJarsCache = cacheDirectoryProperty(() -> new File(getGlobalBuildCache(), "game-jars"));
		this.mappedJarsCache = cacheDirectoryProperty(() -> new File(getGlobalBuildCache(), "mapped-jars"));
		this.processedJarsCache = cacheDirectoryProperty(() -> new File(getGlobalBuildCache(), "processed-jars"));
		this.librariesCache = cacheDirectoryProperty(() -> new File(getGlobalBuildCache(), "libraries"));
		this.mappingsCache = cacheDirectoryProperty(() -> new File(getGlobalBuildCache(), "mappings"));
		this.ravenCache = cacheDirectoryProperty(() -> new File(getGlobalBuildCache(), "raven"));
		this.sparrowCache = cacheDirectoryProperty(() -> new File(getGlobalBuildCache(), "sparrow"));
		this.nestsCache = cacheDirectoryProperty(() -> new File(getGlobalBuildCache(), "nests"));

		this.ravenBuildsCache = fileProperty(() -> this.project.file("raven-builds.json"));
		this.sparrowBuildsCache = fileProperty(() -> this.project.file("sparrow-builds.json"));
		this.nestsBuildsCache = fileProperty(() -> this.project.file("nests-builds.json"));
		this.enigmaProfile = fileProperty(() -> this.project.file("enigma_profile.json"));
		this.mappingsDir = fileProperty(() -> this.project.file("mappings"));
		this.exceptionsDir = fileProperty(() -> this.project.file("exceptions"));
		this.signaturesDir = fileProperty(() -> this.project.file("signatures"));
		this.matchesDir = fileProperty(() -> this.project.file("matches/matches"));
		this.runDir = fileProperty(() -> this.project.file("run"));
		this.workingDir = new Versioned<>(minecraftVersion -> new File(getRunDirectory(), minecraftVersion));
		this.enigmaSessionLock = new Versioned<>(minecraftVersion -> new File(getWorkingDirectory(minecraftVersion), EnigmaSession.LOCK_FILE));
		this.decompiledSrcDir = new Versioned<>(minecraftVersion -> this.project.file("%s-decompiledSrc".formatted(minecraftVersion)));
		this.fakeSrcDir = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-fakeSrc".formatted(minecraftVersion)));
		this.javadocDir = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-javadoc".formatted(minecraftVersion)));

		this.versionsManifest = fileProperty(() -> new File(getGlobalBuildCache(), "versions-manifest.json"));
		this.versionInfos = new Versioned<>(minecraftVersion -> new File(getVersionJsonsCache(), "%s-info.json".formatted(minecraftVersion)));
		this.versionDetails = new Versioned<>(minecraftVersion -> new File(getVersionJsonsCache(), "%s-details.json".formatted(minecraftVersion)));

		this.clientJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client()) {
				throw new NoSuchFileException("client jar for Minecraft version " + minecraftVersion + " does not exist!");
			} else {
				return new File(getGameJarsCache(), "%s-client.jar".formatted(minecraftVersion));
			}
		});
		this.serverJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.server()) {
				throw new NoSuchFileException("server jar for Minecraft version " + minecraftVersion + " does not exist!");
			} else {
				return new File(getGameJarsCache(), "%s-server.jar".formatted(minecraftVersion));
			}
		});
		this.mergedJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client() || !details.server()) {
				throw new NoSuchFileException("game jars for Minecraft version " + minecraftVersion + " cannot be merged: either the client or server jar does not exist!");
			} else if (!details.sharedMappings()) {
				throw new NoSuchFileException("game jars for Minecraft version " + minecraftVersion + " cannot be merged: the client and server jars do not have shared mappings!");
			} else {
				return new File(getGameJarsCache(), "%s-merged.jar".formatted(minecraftVersion));
			}
		});
		this.intermediaryClientJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client()) {
				throw new NoSuchFileException("client jar for Minecraft version " + minecraftVersion + " does not exist!");
			} else if (details.server() && details.sharedMappings()) {
				throw new NoSuchFileException("intermediray client jar for Minecraft version " + minecraftVersion + " does not exist: please use the merged jar!");
			} else {
				return new File(getMappedJarsCache(), "%s-intermediary-gen%d-client.jar".formatted(minecraftVersion, getIntermediaryGen()));
			}
		});
		this.intermediaryServerJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.server()) {
				throw new NoSuchFileException("server jar for Minecraft version " + minecraftVersion + " does not exist!");
			} else if (details.client() && details.sharedMappings()) {
				throw new NoSuchFileException("intermediray server jar for Minecraft version " + minecraftVersion + " does not exist: please use the merged jar!");
			} else {
				return new File(getMappedJarsCache(), "%s-intermediary-gen%d-server.jar".formatted(minecraftVersion, getIntermediaryGen()));
			}
		});
		this.intermediaryMergedJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client() || !details.server()) {
				throw new NoSuchFileException("intermediary jars for Minecraft version " + minecraftVersion + " cannot be merged: either the client or server jar does not exist!");
			} else {
				return new File(getMappedJarsCache(), "%s-intermediary-gen%d-merged.jar".formatted(minecraftVersion, getIntermediaryGen()));
			}
		});
		this.exceptionsPatchedIntermediaryClientJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client() || details.server()) {
				throw new NoSuchFileException("intermediary client jar for Minecraft version " + minecraftVersion + " does not exist!");
			} else {
				int build = keratin.getRavenBuild(minecraftVersion, GameSide.CLIENT);

				if (build < 1) {
					return null;
				} else {
					return new File(getProcessedJarsCache(), "%s-raven+build.%d-intermediary-gen%d-client.jar".formatted(minecraftVersion, build, getIntermediaryGen()));
				}
			}
		});
		this.exceptionsPatchedIntermediaryServerJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.server() || details.client()) {
				throw new NoSuchFileException("intermediary server jar for Minecraft version " + minecraftVersion + " does not exist!");
			} else {
				int build = keratin.getRavenBuild(minecraftVersion, GameSide.SERVER);

				if (build < 1) {
					return null;
				} else {
					return new File(getProcessedJarsCache(), "%s-raven+build.%d-intermediary-gen%d-server.jar".formatted(minecraftVersion, build, getIntermediaryGen()));
				}
			}
		});
		this.exceptionsPatchedIntermediaryMergedJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client() || !details.server()) {
				throw new NoSuchFileException("intermediary jars for Minecraft version " + minecraftVersion + " cannot be merged: either the client or server jar does not exist!");
			} else {
				int build = keratin.getRavenBuild(minecraftVersion, GameSide.MERGED);
				int clientBuild = keratin.getRavenBuild(minecraftVersion, GameSide.CLIENT);
				int serverBuild = keratin.getRavenBuild(minecraftVersion, GameSide.SERVER);

				if (build < 1) {
					if (clientBuild < 1 && serverBuild < 1) {
						return null;
					} else {
						return new File(getProcessedJarsCache(), "%s-raven+build.(%d-%d)-intermediary-gen%d-merged.jar".formatted(minecraftVersion, clientBuild, serverBuild, getIntermediaryGen()));
					}
				} else {
					return new File(getProcessedJarsCache(), "%s-raven+build.%d-intermediary-gen%d-merged.jar".formatted(minecraftVersion, build, getIntermediaryGen()));
				}
			}
		});
		this.signaturePatchedIntermediaryClientJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client() || details.server()) {
				throw new NoSuchFileException("intermediary client jar for Minecraft version " + minecraftVersion + " does not exist!");
			} else {
				int build = keratin.getSparrowBuild(minecraftVersion, GameSide.CLIENT);

				if (build < 1) {
					return null;
				} else {
					return new File(getProcessedJarsCache(), "%s-sparrow+build.%d-intermediary-gen%d-client.jar".formatted(minecraftVersion, build, getIntermediaryGen()));
				}
			}
		});
		this.signaturePatchedIntermediaryServerJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.server() || details.client()) {
				throw new NoSuchFileException("intermediary server jar for Minecraft version " + minecraftVersion + " does not exist!");
			} else {
				int build = keratin.getSparrowBuild(minecraftVersion, GameSide.SERVER);

				if (build < 1) {
					return null;
				} else {
					return new File(getProcessedJarsCache(), "%s-sparrow+build.%d-intermediary-gen%d-server.jar".formatted(minecraftVersion, build, getIntermediaryGen()));
				}
			}
		});
		this.signaturePatchedIntermediaryMergedJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client() || !details.server()) {
				throw new NoSuchFileException("intermediary jars for Minecraft version " + minecraftVersion + " cannot be merged: either the client or server jar does not exist!");
			} else {
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
			}
		});
		this.nestedIntermediaryClientJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client() || details.server()) {
				throw new NoSuchFileException("intermediary client jar for Minecraft version " + minecraftVersion + " does not exist!");
			} else {
				int build = keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT);

				if (build < 1) {
					return null;
				} else {
					return new File(getProcessedJarsCache(), "%s-nests+build.%d-intermediary-gen%d-client.jar".formatted(minecraftVersion, build, getIntermediaryGen()));
				}
			}
		});
		this.nestedIntermediaryServerJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.server() || details.client()) {
				throw new NoSuchFileException("intermediary server jar for Minecraft version " + minecraftVersion + " does not exist!");
			} else {
				int build = keratin.getNestsBuild(minecraftVersion, GameSide.SERVER);

				if (build < 1) {
					return null;
				} else {
					return new File(getProcessedJarsCache(), "%s-nests+build.%d-intermediary-gen%d-server.jar".formatted(minecraftVersion, build, getIntermediaryGen()));
				}
			}
		});
		this.nestedIntermediaryMergedJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client() || !details.server()) {
				throw new NoSuchFileException("intermediary jars for Minecraft version " + minecraftVersion + " cannot be merged: either the client or server jar does not exist!");
			} else {
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
			}
		});
		this.processedIntermediaryClientJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client() || details.server()) {
				throw new NoSuchFileException("intermediary client jar for Minecraft version " + minecraftVersion + " does not exist!");
			} else {
				return new File(getProcessedJarsCache(), "%s-processed-intermediary-gen%d-client.jar".formatted(minecraftVersion, getIntermediaryGen()));
			}
		});
		this.processedIntermediaryServerJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.server() || details.client()) {
				throw new NoSuchFileException("intermediary server jar for Minecraft version " + minecraftVersion + " does not exist!");
			} else {
				return new File(getProcessedJarsCache(), "%s-processed-intermediary-gen%d-server.jar".formatted(minecraftVersion, getIntermediaryGen()));
			}
		});
		this.processedIntermediaryMergedJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client() || !details.server()) {
				throw new NoSuchFileException("intermediary jars for Minecraft version " + minecraftVersion + " cannot be merged: either the client or server jar does not exist!");
			} else {
				return new File(getProcessedJarsCache(), "%s-processed-intermediary-gen%d-merged.jar".formatted(minecraftVersion, getIntermediaryGen()));
			}
		});

		this.namedJar = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-named.jar".formatted(minecraftVersion)));
		this.processedNamedJar = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-processed-named.jar".formatted(minecraftVersion)));

		this.clientIntermediaryMappings = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client()) {
				throw new NoSuchFileException("client intermediary mappings for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.server() && details.sharedMappings()) {
				throw new NoSuchFileException("client intermediary mappings for Minecraft version " + minecraftVersion + " do not exist: please use the merged mappings!");
			} else {
				return new File(getMappingsCache(), "%s-intermediary-gen%d-client.tiny".formatted(minecraftVersion, getIntermediaryGen()));
			}
		});
		this.serverIntermediaryMappings = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.server()) {
				throw new NoSuchFileException("server intermediary mappings for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.client() && details.sharedMappings()) {
				throw new NoSuchFileException("server intermediary mappings for Minecraft version " + minecraftVersion + " do not exist: please use the merged mappings!");
			} else {
				return new File(getMappingsCache(), "%s-intermediary-gen%d-server.tiny".formatted(minecraftVersion, getIntermediaryGen()));
			}
		});
		this.mergedIntermediaryMappings = new Versioned<>(minecraftVersion -> new File(getMappingsCache(), "%s-intermediary-gen%d-merged.tiny".formatted(minecraftVersion, getIntermediaryGen())));

		this.namedMappings = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-named.tiny".formatted(minecraftVersion)));
		this.processedNamedMappings = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-processed-named.tiny".formatted(minecraftVersion)));
		this.completedNamedMappings = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-completed.tiny".formatted(minecraftVersion)));
		this.tinyV1NamedMappings = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-tiny-v1.tiny".formatted(minecraftVersion)));
		this.tinyV2NamedMappings = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-tiny-v2.tiny".formatted(minecraftVersion)));
		this.mergedTinyV1NamedMappings = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-merged-tiny-v1.tiny".formatted(minecraftVersion)));
		this.mergedTinyV2NamedMappings = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-merged-tiny-v2.tiny".formatted(minecraftVersion)));

		this.clientRavenFile = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client()) {
				throw new NoSuchFileException("client raven for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.server() && details.sharedMappings()) {
				throw new NoSuchFileException("client raven for Minecraft version " + minecraftVersion + " do not exist: please use the merged raven!");
			} else {
				int build = keratin.getRavenBuild(minecraftVersion, GameSide.CLIENT);

				if (build < 1) {
					return null;
				} else {
					return new File(getRavenCache(), "%s-raven+build.%d-client.excs".formatted(minecraftVersion, build));
				}
			}
		});
		this.serverRavenFile = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.server()) {
				throw new NoSuchFileException("server raven for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.client() && details.sharedMappings()) {
				throw new NoSuchFileException("server raven for Minecraft version " + minecraftVersion + " do not exist: please use the merged raven!");
			} else {
				int build = keratin.getRavenBuild(minecraftVersion, GameSide.SERVER);

				if (build < 1) {
					return null;
				} else {
					return new File(getRavenCache(), "%s-raven+build.%d-server.excs".formatted(minecraftVersion, build));
				}
			}
		});
		this.mergedRavenFile = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.sharedMappings()) {
				throw new NoSuchFileException("raven for Minecraft version " + minecraftVersion + " cannot be merged: the client and server raven do not have shared mappings!");
			} else {
				int build = keratin.getRavenBuild(minecraftVersion, GameSide.MERGED);

				if (build < 1) {
					return null;
				} else {
					return new File(getRavenCache(), "%s-raven+build.%d-merged.excs".formatted(minecraftVersion, build));
				}
			}
		});
		this.intermediaryClientRavenFile = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client()) {
				throw new NoSuchFileException("client raven for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.server() && details.sharedMappings()) {
				throw new NoSuchFileException("intermediary client raven for Minecraft version " + minecraftVersion + " do not exist: please use the merged raven!");
			} else {
				int build = keratin.getRavenBuild(minecraftVersion, GameSide.CLIENT);

				if (build < 1) {
					return null;
				} else {
					return new File(getRavenCache(), "%s-intermediary-gen%d-raven+build.%d-client.excs".formatted(minecraftVersion, getIntermediaryGen(), build));
				}
			}
		});
		this.intermediaryServerRavenFile = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.server()) {
				throw new NoSuchFileException("server raven for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.client() && details.sharedMappings()) {
				throw new NoSuchFileException("intermediary server raven for Minecraft version " + minecraftVersion + " do not exist: please use the merged raven!");
			} else {
				int build = keratin.getRavenBuild(minecraftVersion, GameSide.SERVER);

				if (build < 1) {
					return null;
				} else {
					return new File(getRavenCache(), "%s-intermediary-gen%d-raven+build.%d-server.excs".formatted(minecraftVersion, getIntermediaryGen(), build));
				}
			}
		});
		this.intermediaryMergedRavenFile = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client() || !details.server()) {
				throw new NoSuchFileException("intermediary raven for Minecraft version " + minecraftVersion + " cannot be merged: either the client or server raven do not exist!");
			} else {
				int build = keratin.getRavenBuild(minecraftVersion, GameSide.MERGED);
				int clientBuild = keratin.getRavenBuild(minecraftVersion, GameSide.CLIENT);
				int serverBuild = keratin.getRavenBuild(minecraftVersion, GameSide.SERVER);

				if (build < 1) {
					if (clientBuild < 1 && serverBuild < 1) {
						return null;
					} else {
						return new File(getRavenCache(), "%s-intermediary-gen%d-raven+build.(%d-%d)-merged.excs".formatted(minecraftVersion, getIntermediaryGen(), clientBuild, serverBuild));
					}
				} else {
					return new File(getRavenCache(), "%s-intermediary-gen%d-raven+build.%d-merged.excs".formatted(minecraftVersion, getIntermediaryGen(), build));
				}
			}
		});

		this.namedRavenFile = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-named-raven.excs".formatted(minecraftVersion)));

		this.clientSparrowFile = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client()) {
				throw new NoSuchFileException("client sparrow for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.server() && details.sharedMappings()) {
				throw new NoSuchFileException("client sparrow for Minecraft version " + minecraftVersion + " do not exist: please use the merged sparrow!");
			} else {
				int build = keratin.getSparrowBuild(minecraftVersion, GameSide.CLIENT);

				if (build < 1) {
					return null;
				} else {
					return new File(getSparrowCache(), "%s-sparrow+build.%d-client.sigs".formatted(minecraftVersion, build));
				}
			}
		});
		this.serverSparrowFile = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.server()) {
				throw new NoSuchFileException("server sparrow for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.client() && details.sharedMappings()) {
				throw new NoSuchFileException("server sparrow for Minecraft version " + minecraftVersion + " do not exist: please use the merged sparrow!");
			} else {
				int build = keratin.getSparrowBuild(minecraftVersion, GameSide.SERVER);

				if (build < 1) {
					return null;
				} else {
					return new File(getSparrowCache(), "%s-sparrow+build.%d-server.sigs".formatted(minecraftVersion, build));
				}
			}
		});
		this.mergedSparrowFile = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.sharedMappings()) {
				throw new NoSuchFileException("sparrow for Minecraft version " + minecraftVersion + " cannot be merged: the client and server sparrow do not have shared mappings!");
			} else {
				int build = keratin.getSparrowBuild(minecraftVersion, GameSide.MERGED);

				if (build < 1) {
					return null;
				} else {
					return new File(getSparrowCache(), "%s-sparrow+build.%d-merged.sigs".formatted(minecraftVersion, build));
				}
			}
		});
		this.intermediaryClientSparrowFile = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client()) {
				throw new NoSuchFileException("client sparrow for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.server() && details.sharedMappings()) {
				throw new NoSuchFileException("intermediary client sparrow for Minecraft version " + minecraftVersion + " do not exist: please use the merged sparrow!");
			} else {
				int build = keratin.getSparrowBuild(minecraftVersion, GameSide.CLIENT);

				if (build < 1) {
					return null;
				} else {
					return new File(getSparrowCache(), "%s-intermediary-gen%d-sparrow+build.%d-client.sigs".formatted(minecraftVersion, getIntermediaryGen(), build));
				}
			}
		});
		this.intermediaryServerSparrowFile = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.server()) {
				throw new NoSuchFileException("server sparrow for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.client() && details.sharedMappings()) {
				throw new NoSuchFileException("intermediary server sparrow for Minecraft version " + minecraftVersion + " do not exist: please use the merged sparrow!");
			} else {
				int build = keratin.getSparrowBuild(minecraftVersion, GameSide.SERVER);

				if (build < 1) {
					return null;
				} else {
					return new File(getSparrowCache(), "%s-intermediary-gen%d-sparrow+build.%d-server.sigs".formatted(minecraftVersion, getIntermediaryGen(), build));
				}
			}
		});
		this.intermediaryMergedSparrowFile = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client() || !details.server()) {
				throw new NoSuchFileException("intermediary sparrow for Minecraft version " + minecraftVersion + " cannot be merged: either the client or server sparrow do not exist!");
			} else {
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
			}
		});

		this.namedSparrowFile = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-named-sparrow.sigs".formatted(minecraftVersion)));

		this.clientNests = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client()) {
				throw new NoSuchFileException("client nests for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.server() && details.sharedMappings()) {
				throw new NoSuchFileException("client nests for Minecraft version " + minecraftVersion + " do not exist: please use the merged nests!");
			} else {
				int build = keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT);

				if (build < 1) {
					return null;
				} else {
					return new File(getNestsCache(), "%s-nests+build.%d-client.nest".formatted(minecraftVersion, build));
				}
			}
		});
		this.serverNests = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.server()) {
				throw new NoSuchFileException("server nests for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.client() && details.sharedMappings()) {
				throw new NoSuchFileException("server nests for Minecraft version " + minecraftVersion + " do not exist: please use the merged nests!");
			} else {
				int build = keratin.getNestsBuild(minecraftVersion, GameSide.SERVER);

				if (build < 1) {
					return null;
				} else {
					return new File(getNestsCache(), "%s-nests+build.%d-server.nest".formatted(minecraftVersion, build));
				}
			}
		});
		this.mergedNests = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.sharedMappings()) {
				throw new NoSuchFileException("nests for Minecraft version " + minecraftVersion + " cannot be merged: the client and server nests do not have shared mappings!");
			} else {
				int build = keratin.getNestsBuild(minecraftVersion, GameSide.MERGED);

				if (build < 1) {
					return null;
				} else {
					return new File(getNestsCache(), "%s-nests+build.%d-merged.nest".formatted(minecraftVersion, build));
				}
			}
		});
		this.intermediaryClientNests = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client()) {
				throw new NoSuchFileException("client nests for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.server() && details.sharedMappings()) {
				throw new NoSuchFileException("intermediary client nests for Minecraft version " + minecraftVersion + " do not exist: please use the merged nests!");
			} else {
				int build = keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT);

				if (build < 1) {
					return null;
				} else {
					return new File(getNestsCache(), "%s-intermediary-gen%d-nests+build.%d-client.nest".formatted(minecraftVersion, getIntermediaryGen(), build));
				}
			}
		});
		this.intermediaryServerNests = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.server()) {
				throw new NoSuchFileException("server nests for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.client() && details.sharedMappings()) {
				throw new NoSuchFileException("intermediary server nests for Minecraft version " + minecraftVersion + " do not exist: please use the merged nests!");
			} else {
				int build = keratin.getNestsBuild(minecraftVersion, GameSide.SERVER);

				if (build < 1) {
					return null;
				} else {
					return new File(getNestsCache(), "%s-intermediary-gen%d-nests+build.%d-server.nest".formatted(minecraftVersion, getIntermediaryGen(), build));
				}
			}
		});
		this.intermediaryMergedNests = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client() || !details.server()) {
				throw new NoSuchFileException("intermediary nests for Minecraft version " + minecraftVersion + " cannot be merged: either the client or server nests do not exist!");
			} else {
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
			}
		});

		this.namedNests = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-named-nests.nest".formatted(minecraftVersion)));

		this.featherMappings = new Versioned<>(minecraftVersion -> {
			int build = keratin.getNextFeatherBuild(minecraftVersion) - 1;

			if (build < 1) {
				throw new NoSuchFileException("no Feather builds for Minecraft version " + minecraftVersion + " exist yet!");
			}

			return new File(getMappingsCache(), "%s-feather-gen%d+build.%d.tiny".formatted(minecraftVersion, getIntermediaryGen(), build));
		});

		this.clientExceptions = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client()) {
				throw new NoSuchFileException("client jar for Minecraft version " + minecraftVersion + " does not exist!");
			} else {
				return new File(getExceptionsDirectory(), "%s-client.excs".formatted(minecraftVersion));
			}
		});
		this.serverExceptions = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.server()) {
				throw new NoSuchFileException("server jar for Minecraft version " + minecraftVersion + " does not exist!");
			} else {
				return new File(getExceptionsDirectory(), "%s-server.excs".formatted(minecraftVersion));
			}
		});
		this.mergedExceptions = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client() || !details.server()) {
				throw new NoSuchFileException("game jars for Minecraft version " + minecraftVersion + " cannot be merged: either the client or server jar does not exist!");
			} else if (!details.sharedMappings()) {
				throw new NoSuchFileException("game jars for Minecraft version " + minecraftVersion + " cannot be merged: the client and server jars do not have shared mappings!");
			} else {
				return new File(getExceptionsDirectory(), "%s.excs".formatted(minecraftVersion));
			}
		});
		this.clientSignatures = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client()) {
				throw new NoSuchFileException("client jar for Minecraft version " + minecraftVersion + " does not exist!");
			} else {
				return new File(getSignaturesDirectory(), "%s-client.sigs".formatted(minecraftVersion));
			}
		});
		this.serverSignatures = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.server()) {
				throw new NoSuchFileException("server jar for Minecraft version " + minecraftVersion + " does not exist!");
			} else {
				return new File(getSignaturesDirectory(), "%s-server.sigs".formatted(minecraftVersion));
			}
		});
		this.mergedSignatures = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client() || !details.server()) {
				throw new NoSuchFileException("game jars for Minecraft version " + minecraftVersion + " cannot be merged: either the client or server jar does not exist!");
			} else if (!details.sharedMappings()) {
				throw new NoSuchFileException("game jars for Minecraft version " + minecraftVersion + " cannot be merged: the client and server jars do not have shared mappings!");
			} else {
				return new File(getSignaturesDirectory(), "%s.sigs".formatted(minecraftVersion));
			}
		});

		this.setupClientIntermediaryMappings = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client()) {
				throw new NoSuchFileException("client intermediary mappings for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.server() && details.sharedMappings()) {
				throw new NoSuchFileException("client intermediary mappings for Minecraft version " + minecraftVersion + " do not exist: please use the merged mappings!");
			} else {
				return new File(getLocalBuildCache(), "%s-setup-intermediary-gen%d-client.tiny".formatted(minecraftVersion, getIntermediaryGen()));
			}
		});
		this.setupServerIntermediaryMappings = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.server()) {
				throw new NoSuchFileException("server intermediary mappings for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.client() && details.sharedMappings()) {
				throw new NoSuchFileException("server intermediary mappings for Minecraft version " + minecraftVersion + " do not exist: please use the merged mappings!");
			} else {
				return new File(getLocalBuildCache(), "%s-setup-intermediary-gen%d-server.tiny".formatted(minecraftVersion, getIntermediaryGen()));
			}
		});
		this.setupMergedIntermediaryMappings = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-setup-intermediary-gen%d-merged.tiny".formatted(minecraftVersion, getIntermediaryGen())));
		this.setupClientNamedMappings = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client()) {
				throw new NoSuchFileException("client named mappings for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.server() && details.sharedMappings()) {
				throw new NoSuchFileException("client named mappings for Minecraft version " + minecraftVersion + " do not exist: please use the merged mappings!");
			} else {
				return new File(getLocalBuildCache(), "%s-setup-named-client.tiny".formatted(minecraftVersion));
			}
		});
		this.setupServerNamedMappings = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.server()) {
				throw new NoSuchFileException("server named mappings for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.client() && details.sharedMappings()) {
				throw new NoSuchFileException("server named mappings for Minecraft version " + minecraftVersion + " do not exist: please use the merged mappings!");
			} else {
				return new File(getLocalBuildCache(), "%s-setup-named-server.tiny".formatted(minecraftVersion));
			}
		});
		this.setupMergedNamedMappings = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-setup-named-merged.tiny".formatted(minecraftVersion)));
		this.combinedSetupClientMappings = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client()) {
				throw new NoSuchFileException("client mappings for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.server() && details.sharedMappings()) {
				throw new NoSuchFileException("client mappings for Minecraft version " + minecraftVersion + " do not exist: please use the merged mappings!");
			} else {
				return new File(getLocalBuildCache(), "%s-combined-setup-client.tiny".formatted(minecraftVersion));
			}
		});
		this.combinedSetupServerMappings = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.server()) {
				throw new NoSuchFileException("server mappings for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.client() && details.sharedMappings()) {
				throw new NoSuchFileException("server mappings for Minecraft version " + minecraftVersion + " do not exist: please use the merged mappings!");
			} else {
				return new File(getLocalBuildCache(), "%s-combined-setup-server.tiny".formatted(minecraftVersion));
			}
		});
		this.combinedSetupMergedMappings = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-combined-setup-merged.tiny".formatted(minecraftVersion)));

		this.setupClientExceptions = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client()) {
				throw new NoSuchFileException("client exceptions for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.server() && details.sharedMappings()) {
				throw new NoSuchFileException("client exceptions for Minecraft version " + minecraftVersion + " do not exist: please use the merged exceptions!");
			} else {
				return new File(getLocalBuildCache(), "%s-setup-client.excs".formatted(minecraftVersion));
			}
		});
		this.setupServerExceptions = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.server()) {
				throw new NoSuchFileException("server exceptions for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.client() && details.sharedMappings()) {
				throw new NoSuchFileException("server exceptions for Minecraft version " + minecraftVersion + " do not exist: please use the merged exceptions!");
			} else {
				return new File(getLocalBuildCache(), "%s-setup-server.excs".formatted(minecraftVersion));
			}
		});
		this.setupMergedExceptions = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-setup-merged.excs".formatted(minecraftVersion)));
		this.setupClientSignatures = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client()) {
				throw new NoSuchFileException("client signatures for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.server() && details.sharedMappings()) {
				throw new NoSuchFileException("client signatures for Minecraft version " + minecraftVersion + " do not exist: please use the merged signatures!");
			} else {
				return new File(getLocalBuildCache(), "%s-setup-client.sigs".formatted(minecraftVersion));
			}
		});
		this.setupServerSignatures = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.server()) {
				throw new NoSuchFileException("server signatures for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.client() && details.sharedMappings()) {
				throw new NoSuchFileException("server signatures for Minecraft version " + minecraftVersion + " do not exist: please use the merged signatures!");
			} else {
				return new File(getLocalBuildCache(), "%s-setup-server.sigs".formatted(minecraftVersion));
			}
		});
		this.setupMergedSignatures = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-setup-merged.sigs".formatted(minecraftVersion)));

		this.setupClientJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client()) {
				throw new NoSuchFileException("client jar for Minecraft version " + minecraftVersion + " does not exist!");
			} else if (details.server() && details.sharedMappings()) {
				throw new NoSuchFileException("setup client jar for Minecraft version " + minecraftVersion + " does not exist: please use the merged jar!");
			} else {
				return new File(getMappedJarsCache(), "%s-setup-client.jar".formatted(minecraftVersion));
			}
		});
		this.setupServerJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.server()) {
				throw new NoSuchFileException("server jar for Minecraft version " + minecraftVersion + " does not exist!");
			} else if (details.client() && details.sharedMappings()) {
				throw new NoSuchFileException("setup server jar for Minecraft version " + minecraftVersion + " does not exist: please use the merged jar!");
			} else {
				return new File(getLocalBuildCache(), "%s-setup-server.jar".formatted(minecraftVersion));
			}
		});
		this.setupMergedJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client() || !details.server()) {
				throw new NoSuchFileException("setup jars for Minecraft version " + minecraftVersion + " cannot be merged: either the client or server jar does not exist!");
			} else {
				return new File(getLocalBuildCache(), "%s-setup-merged.jar".formatted(minecraftVersion));
			}
		});
		this.namedSetupClientJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client()) {
				throw new NoSuchFileException("client jar for Minecraft version " + minecraftVersion + " does not exist!");
			} else if (details.server() && details.sharedMappings()) {
				throw new NoSuchFileException("setup client jar for Minecraft version " + minecraftVersion + " does not exist: please use the merged jar!");
			} else {
				return new File(getLocalBuildCache(), "%s-named-setup-client.jar".formatted(minecraftVersion));
			}
		});
		this.namedSetupServerJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.server()) {
				throw new NoSuchFileException("server jar for Minecraft version " + minecraftVersion + " does not exist!");
			} else if (details.client() && details.sharedMappings()) {
				throw new NoSuchFileException("setup server jar for Minecraft version " + minecraftVersion + " does not exist: please use the merged jar!");
			} else {
				return new File(getLocalBuildCache(), "%s-named-setup-server.jar".formatted(minecraftVersion));
			}
		});
		this.namedSetupMergedJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client() || !details.server()) {
				throw new NoSuchFileException("setup jars for Minecraft version " + minecraftVersion + " cannot be merged: either the client or server jar does not exist!");
			} else {
				return new File(getLocalBuildCache(), "%s-named-setup-merged.jar".formatted(minecraftVersion));
			}
		});

		this.baseClientExceptions = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client()) {
				throw new NoSuchFileException("client exceptions for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.server() && details.sharedMappings()) {
				throw new NoSuchFileException("client exceptions for Minecraft version " + minecraftVersion + " do not exist: please use the merged exceptions!");
			} else {
				return new File(getLocalBuildCache(), "%s-base-client.excs".formatted(minecraftVersion));
			}
		});
		this.baseServerExceptions = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.server()) {
				throw new NoSuchFileException("server exceptions for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.client() && details.sharedMappings()) {
				throw new NoSuchFileException("server exceptions for Minecraft version " + minecraftVersion + " do not exist: please use the merged exceptions!");
			} else {
				return new File(getLocalBuildCache(), "%s-base-server.excs".formatted(minecraftVersion));
			}
		});
		this.baseMergedExceptions = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-base-merged.excs".formatted(minecraftVersion)));
		this.baseClientSignatures = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client()) {
				throw new NoSuchFileException("client signatures for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.server() && details.sharedMappings()) {
				throw new NoSuchFileException("client signatures for Minecraft version " + minecraftVersion + " do not exist: please use the merged signatures!");
			} else {
				return new File(getLocalBuildCache(), "%s-base-client.sigs".formatted(minecraftVersion));
			}
		});
		this.baseServerSignatures = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.server()) {
				throw new NoSuchFileException("server signatures for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.client() && details.sharedMappings()) {
				throw new NoSuchFileException("server signatures for Minecraft version " + minecraftVersion + " do not exist: please use the merged signatures!");
			} else {
				return new File(getLocalBuildCache(), "%s-base-server.sigs".formatted(minecraftVersion));
			}
		});
		this.baseMergedSignatures = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-base-merged.sigs".formatted(minecraftVersion)));

		this.generatedClientJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client()) {
				throw new NoSuchFileException("client jar for Minecraft version " + minecraftVersion + " does not exist!");
			} else if (details.server() && details.sharedMappings()) {
				throw new NoSuchFileException("generated client jar for Minecraft version " + minecraftVersion + " does not exist: please use the merged jar!");
			} else {
				return new File(getLocalBuildCache(), "%s-generated-client.jar".formatted(minecraftVersion));
			}
		});
		this.generatedServerJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.server()) {
				throw new NoSuchFileException("server jar for Minecraft version " + minecraftVersion + " does not exist!");
			} else if (details.client() && details.sharedMappings()) {
				throw new NoSuchFileException("generated server jar for Minecraft version " + minecraftVersion + " does not exist: please use the merged jar!");
			} else {
				return new File(getLocalBuildCache(), "%s-generated-server.jar".formatted(minecraftVersion));
			}
		});
		this.generatedMergedJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client() || !details.server()) {
				throw new NoSuchFileException("generated jars for Minecraft version " + minecraftVersion + " cannot be merged: either the client or server jar does not exist!");
			} else {
				return new File(getLocalBuildCache(), "%s-generated-merged.jar".formatted(minecraftVersion));
			}
		});
		this.namedGeneratedClientJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client()) {
				throw new NoSuchFileException("client jar for Minecraft version " + minecraftVersion + " does not exist!");
			} else if (details.server() && details.sharedMappings()) {
				throw new NoSuchFileException("generated client jar for Minecraft version " + minecraftVersion + " does not exist: please use the merged jar!");
			} else {
				return new File(getLocalBuildCache(), "%s-named-generated-client.jar".formatted(minecraftVersion));
			}
		});
		this.namedGeneratedServerJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.server()) {
				throw new NoSuchFileException("server jar for Minecraft version " + minecraftVersion + " does not exist!");
			} else if (details.client() && details.sharedMappings()) {
				throw new NoSuchFileException("generated server jar for Minecraft version " + minecraftVersion + " does not exist: please use the merged jar!");
			} else {
				return new File(getLocalBuildCache(), "%s-named-generated-server.jar".formatted(minecraftVersion));
			}
		});
		this.namedGeneratedMergedJar = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client() || !details.server()) {
				throw new NoSuchFileException("generated jars for Minecraft version " + minecraftVersion + " cannot be merged: either the client or server jar does not exist!");
			} else {
				return new File(getLocalBuildCache(), "%s-named-generated-merged.jar".formatted(minecraftVersion));
			}
		});

		this.generatedClientExceptions = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client()) {
				throw new NoSuchFileException("client exceptions for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.server() && details.sharedMappings()) {
				throw new NoSuchFileException("client exceptions for Minecraft version " + minecraftVersion + " do not exist: please use the merged exceptions!");
			} else {
				return new File(getLocalBuildCache(), "%s-generated-client.excs".formatted(minecraftVersion));
			}
		});
		this.generatedServerExceptions = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.server()) {
				throw new NoSuchFileException("server exceptions for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.client() && details.sharedMappings()) {
				throw new NoSuchFileException("server exceptions for Minecraft version " + minecraftVersion + " do not exist: please use the merged exceptions!");
			} else {
				return new File(getLocalBuildCache(), "%s-generated-server.excs".formatted(minecraftVersion));
			}
		});
		this.generatedMergedExceptions = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-generated-merged.excs".formatted(minecraftVersion)));
		this.generatedClientSignatures = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.client()) {
				throw new NoSuchFileException("client signatures for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.server() && details.sharedMappings()) {
				throw new NoSuchFileException("client signatures for Minecraft version " + minecraftVersion + " do not exist: please use the merged signatures!");
			} else {
				return new File(getLocalBuildCache(), "%s-generated-client.sigs".formatted(minecraftVersion));
			}
		});
		this.generatedServerSignatures = new Versioned<>(minecraftVersion -> {
			VersionDetails details = keratin.getVersionDetails(minecraftVersion);

			if (!details.server()) {
				throw new NoSuchFileException("server signatures for Minecraft version " + minecraftVersion + " do not exist!");
			} else if (details.client() && details.sharedMappings()) {
				throw new NoSuchFileException("server signatures for Minecraft version " + minecraftVersion + " do not exist: please use the merged signatures!");
			} else {
				return new File(getLocalBuildCache(), "%s-generated-server.sigs".formatted(minecraftVersion));
			}
		});
		this.generatedMergedSignatures = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-generated-merged.sigs".formatted(minecraftVersion)));
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
	public File getRavenCache() {
		return ravenCache.get();
	}

	@Override
	public File getSparrowCache() {
		return sparrowCache.get();
	}

	@Override
	public File getNestsCache() {
		return nestsCache.get();
	}

	@Override
	public File getRavenBuildsCache() {
		return ravenBuildsCache.get();
	}

	@Override
	public File getSparrowBuildsCache() {
		return sparrowBuildsCache.get();
	}

	@Override
	public File getNestsBuildsCache() {
		return nestsBuildsCache.get();
	}

	@Override
	public File getEnigmaProfile() {
		return enigmaProfile.get();
	}

	@Override
	public File getMappingsDirectory() {
		return mappingsDir.get();
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
	public File getMatchesDirectory() {
		return matchesDir.get();
	}

	@Override
	public File getRunDirectory() {
		return runDir.get();
	}

	@Override
	public File getWorkingDirectory(String minecraftVersion) {
		return workingDir.get(minecraftVersion);
	}

	@Override
	public File getEnigmaSessionLock(String minecraftVersion) {
		return enigmaSessionLock.get(minecraftVersion);
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
	public File getExceptionsPatchedIntermediaryClientJar(String minecraftVersion) {
		return exceptionsPatchedIntermediaryClientJar.get(minecraftVersion);
	}

	@Override
	public File getExceptionsPatchedIntermediaryServerJar(String minecraftVersion) {
		return exceptionsPatchedIntermediaryServerJar.get(minecraftVersion);
	}

	@Override
	public File getExceptionsPatchedIntermediaryMergedJar(String minecraftVersion) {
		return exceptionsPatchedIntermediaryMergedJar.get(minecraftVersion);
	}

	@Override
	public File getMainExceptionsPatchedIntermediaryJar(String minecraftVersion) {
		return pickFileForPresentSides(minecraftVersion, exceptionsPatchedIntermediaryClientJar, exceptionsPatchedIntermediaryServerJar, exceptionsPatchedIntermediaryMergedJar);
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
	public File getClientRavenFile(String minecraftVersion) {
		return clientRavenFile.get(minecraftVersion);
	}

	@Override
	public File getServerRavenFile(String minecraftVersion) {
		return serverRavenFile.get(minecraftVersion);
	}

	@Override
	public File getMergedRavenFile(String minecraftVersion) {
		return mergedRavenFile.get(minecraftVersion);
	}

	@Override
	public File getIntermediaryClientRavenFile(String minecraftVersion) {
		return intermediaryClientRavenFile.get(minecraftVersion);
	}

	@Override
	public File getIntermediaryServerRavenFile(String minecraftVersion) {
		return intermediaryServerRavenFile.get(minecraftVersion);
	}

	@Override
	public File getIntermediaryMergedRavenFile(String minecraftVersion) {
		return intermediaryMergedRavenFile.get(minecraftVersion);
	}

	@Override
	public File getMainIntermediaryRavenFile(String minecraftVersion) {
		return pickFileForPresentSides(minecraftVersion, intermediaryClientRavenFile, intermediaryServerRavenFile, intermediaryMergedRavenFile);
	}

	@Override
	public File getNamedRavenFile(String minecraftVersion) {
		return namedRavenFile.get(minecraftVersion);
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

	@Override
	public File getNamedSparrowFile(String minecraftVersion) {
		return namedSparrowFile.get(minecraftVersion);
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
	public File getNamedNests(String minecraftVersion) {
		return namedNests.get(minecraftVersion);
	}

	@Override
	public File getFeatherMappings(String minecraftVersion) {
		return featherMappings.get(minecraftVersion);
	}

	@Override
	public File getClientExceptions(String minecraftVersion) {
		return clientExceptions.get(minecraftVersion);
	}

	@Override
	public File getServerExceptions(String minecraftVersion) {
		return serverExceptions.get(minecraftVersion);
	}

	@Override
	public File getMergedExceptions(String minecraftVersion) {
		return mergedExceptions.get(minecraftVersion);
	}

	@Override
	public File getClientSignatures(String minecraftVersion) {
		return clientSignatures.get(minecraftVersion);
	}

	@Override
	public File getServerSignatures(String minecraftVersion) {
		return serverSignatures.get(minecraftVersion);
	}

	@Override
	public File getMergedSignatures(String minecraftVersion) {
		return mergedSignatures.get(minecraftVersion);
	}

	@Override
	public File getSetupClientIntermediaryMappings(String minecraftVersion) {
		return setupClientIntermediaryMappings.get(minecraftVersion);
	}

	@Override
	public File getSetupServerIntermediaryMappings(String minecraftVersion) {
		return setupServerIntermediaryMappings.get(minecraftVersion);
	}

	@Override
	public File getSetupMergedIntermediaryMappings(String minecraftVersion) {
		return setupMergedIntermediaryMappings.get(minecraftVersion);
	}

	@Override
	public File getSetupClientNamedMappings(String minecraftVersion) {
		return setupClientNamedMappings.get(minecraftVersion);
	}

	@Override
	public File getSetupServerNamedMappings(String minecraftVersion) {
		return setupServerNamedMappings.get(minecraftVersion);
	}

	@Override
	public File getSetupMergedNamedMappings(String minecraftVersion) {
		return setupMergedNamedMappings.get(minecraftVersion);
	}

	@Override
	public File getCombinedSetupClientMappings(String minecraftVersion) {
		return combinedSetupClientMappings.get(minecraftVersion);
	}

	@Override
	public File getCombinedSetupServerMappings(String minecraftVersion) {
		return combinedSetupServerMappings.get(minecraftVersion);
	}

	@Override
	public File getCombinedSetupMergedMappings(String minecraftVersion) {
		return combinedSetupMergedMappings.get(minecraftVersion);
	}

	@Override
	public File getSetupClientExceptions(String minecraftVersion) {
		return setupClientExceptions.get(minecraftVersion);
	}

	@Override
	public File getSetupServerExceptions(String minecraftVersion) {
		return setupServerExceptions.get(minecraftVersion);
	}

	@Override
	public File getSetupMergedExceptions(String minecraftVersion) {
		return setupMergedExceptions.get(minecraftVersion);
	}

	@Override
	public File getSetupClientSignatures(String minecraftVersion) {
		return setupClientSignatures.get(minecraftVersion);
	}

	@Override
	public File getSetupServerSignatures(String minecraftVersion) {
		return setupServerSignatures.get(minecraftVersion);
	}

	@Override
	public File getSetupMergedSignatures(String minecraftVersion) {
		return setupMergedSignatures.get(minecraftVersion);
	}

	@Override
	public File getSetupClientJar(String minecraftVersion) {
		return setupClientJar.get(minecraftVersion);
	}

	@Override
	public File getSetupServerJar(String minecraftVersion) {
		return setupServerJar.get(minecraftVersion);
	}

	@Override
	public File getSetupMergedJar(String minecraftVersion) {
		return setupMergedJar.get(minecraftVersion);
	}

	@Override
	public File getNamedSetupClientJar(String minecraftVersion) {
		return namedSetupClientJar.get(minecraftVersion);
	}

	@Override
	public File getNamedSetupServerJar(String minecraftVersion) {
		return namedSetupServerJar.get(minecraftVersion);
	}

	@Override
	public File getNamedSetupMergedJar(String minecraftVersion) {
		return namedSetupMergedJar.get(minecraftVersion);
	}

	@Override
	public File getBaseClientExceptions(String minecraftVersion) {
		return baseClientExceptions.get(minecraftVersion);
	}

	@Override
	public File getBaseServerExceptions(String minecraftVersion) {
		return baseServerExceptions.get(minecraftVersion);
	}

	@Override
	public File getBaseMergedExceptions(String minecraftVersion) {
		return baseMergedExceptions.get(minecraftVersion);
	}

	@Override
	public File getBaseClientSignatures(String minecraftVersion) {
		return baseClientSignatures.get(minecraftVersion);
	}

	@Override
	public File getBaseServerSignatures(String minecraftVersion) {
		return baseServerSignatures.get(minecraftVersion);
	}

	@Override
	public File getBaseMergedSignatures(String minecraftVersion) {
		return baseMergedSignatures.get(minecraftVersion);
	}

	@Override
	public File getGeneratedClientJar(String minecraftVersion) {
		return generatedClientJar.get(minecraftVersion);
	}

	@Override
	public File getGeneratedServerJar(String minecraftVersion) {
		return generatedServerJar.get(minecraftVersion);
	}

	@Override
	public File getGeneratedMergedJar(String minecraftVersion) {
		return generatedMergedJar.get(minecraftVersion);
	}

	@Override
	public File getNamedGeneratedClientJar(String minecraftVersion) {
		return namedGeneratedClientJar.get(minecraftVersion);
	}

	@Override
	public File getNamedGeneratedServerJar(String minecraftVersion) {
		return namedGeneratedServerJar.get(minecraftVersion);
	}

	@Override
	public File getNamedGeneratedMergedJar(String minecraftVersion) {
		return namedGeneratedMergedJar.get(minecraftVersion);
	}

	@Override
	public File getGeneratedClientExceptions(String minecraftVersion) {
		return generatedClientExceptions.get(minecraftVersion);
	}

	@Override
	public File getGeneratedServerExceptions(String minecraftVersion) {
		return generatedServerExceptions.get(minecraftVersion);
	}

	@Override
	public File getGeneratedMergedExceptions(String minecraftVersion) {
		return generatedMergedExceptions.get(minecraftVersion);
	}

	@Override
	public File getGeneratedClientSignatures(String minecraftVersion) {
		return generatedClientSignatures.get(minecraftVersion);
	}

	@Override
	public File getGeneratedServerSignatures(String minecraftVersion) {
		return generatedServerSignatures.get(minecraftVersion);
	}

	@Override
	public File getGeneratedMergedSignatures(String minecraftVersion) {
		return generatedMergedSignatures.get(minecraftVersion);
	}
}
