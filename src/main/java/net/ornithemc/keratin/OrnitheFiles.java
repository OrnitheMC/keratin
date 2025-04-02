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
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
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
	private final Property<File> exceptionsCache;
	private final Property<File> signaturesCache;
	private final Property<File> nestsCache;

	private final Property<File> namedMappingsBuildsCache;
	private final Property<File> exceptionsBuildsCache;
	private final Property<File> signaturesBuildsCache;
	private final Property<File> nestsBuildsCache;
	private final Property<File> enigmaProfile;
	private final Property<File> mappingsDir;
	private final Property<File> exceptionsDir;
	private final Property<File> signaturesDir;
	private final Property<File> matchesDir;
	private final Property<File> runDir;
	private final Versioned<MinecraftVersion, File> workingDir;
	private final Versioned<MinecraftVersion, File> enigmaSessionLock;
	private final Versioned<MinecraftVersion, File> decompiledSrcDir;
	private final Versioned<String, File> fakeSrcDir;
	private final Versioned<String, File> javadocDir;

	private final Property<File> versionsManifest;
	private final Versioned<String, File> versionInfos;
	private final Versioned<String, File> versionDetails;

	private final Versioned<MinecraftVersion, File> clientJar;
	private final Versioned<MinecraftVersion, File> serverJar;
	private final Versioned<MinecraftVersion, File> mergedJar;
	private final Versioned<MinecraftVersion, File> intermediaryClientJar;
	private final Versioned<MinecraftVersion, File> intermediaryServerJar;
	private final Versioned<MinecraftVersion, File> intermediaryMergedJar;
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

	private final Versioned<String, File> namedJar;
	private final Versioned<String, File> processedNamedJar;
	private final Versioned<String, File> javadocNamedJar;

	private final Versioned<MinecraftVersion, File> clientIntermediaryMappingsJar;
	private final Versioned<MinecraftVersion, File> serverIntermediaryMappingsJar;
	private final Versioned<MinecraftVersion, File> mergedIntermediaryMappingsJar;
	private final Versioned<MinecraftVersion, File> clientIntermediaryMappings;
	private final Versioned<MinecraftVersion, File> serverIntermediaryMappings;
	private final Versioned<MinecraftVersion, File> mergedIntermediaryMappings;
	private final Versioned<MinecraftVersion, File> filledClientIntermediaryMappings;
	private final Versioned<MinecraftVersion, File> filledServerIntermediaryMappings;
	private final Versioned<MinecraftVersion, File> filledMergedIntermediaryMappings;

	private final Versioned<MinecraftVersion, File> namedMappings;
	private final Versioned<MinecraftVersion, File> processedNamedMappings;
	private final Versioned<MinecraftVersion, File> completedNamedMappings;
	private final Versioned<String, File> tinyV1NamedMappings;
	private final Versioned<String, File> tinyV2NamedMappings;
	private final Versioned<String, File> mergedTinyV1NamedMappings;
	private final Versioned<String, File> mergedTinyV2NamedMappings;
	private final Versioned<String, File> compressedMergedTinyV1NamedMappings;

	private final Versioned<MinecraftVersion, File> clientExceptionsJar;
	private final Versioned<MinecraftVersion, File> serverExceptionsJar;
	private final Versioned<MinecraftVersion, File> mergedExceptionsJar;
	private final Versioned<MinecraftVersion, File> clientExceptionsFile;
	private final Versioned<MinecraftVersion, File> serverExceptionsFile;
	private final Versioned<MinecraftVersion, File> mergedExceptionsFile;
	private final Versioned<MinecraftVersion, File> intermediaryClientExceptionsFile;
	private final Versioned<MinecraftVersion, File> intermediaryServerExceptionsFile;
	private final Versioned<MinecraftVersion, File> intermediaryMergedExceptionsFile;

	private final Versioned<MinecraftVersion, File> namedExceptionsFile;

	private final Versioned<MinecraftVersion, File> clientSignaturesJar;
	private final Versioned<MinecraftVersion, File> serverSignaturesJar;
	private final Versioned<MinecraftVersion, File> mergedSignaturesJar;
	private final Versioned<MinecraftVersion, File> clientSignaturesFile;
	private final Versioned<MinecraftVersion, File> serverSignaturesFile;
	private final Versioned<MinecraftVersion, File> mergedSignaturesFile;
	private final Versioned<MinecraftVersion, File> intermediaryClientSignaturesFile;
	private final Versioned<MinecraftVersion, File> intermediaryServerSignaturesFile;
	private final Versioned<MinecraftVersion, File> intermediaryMergedSignaturesFile;

	private final Versioned<MinecraftVersion, File> namedSignaturesFile;

	private final Versioned<MinecraftVersion, File> clientNestsJar;
	private final Versioned<MinecraftVersion, File> serverNestsJar;
	private final Versioned<MinecraftVersion, File> mergedNestsJar;
	private final Versioned<MinecraftVersion, File> clientNestsFile;
	private final Versioned<MinecraftVersion, File> serverNestsFile;
	private final Versioned<MinecraftVersion, File> mergedNestsFile;
	private final Versioned<MinecraftVersion, File> intermediaryClientNestsFile;
	private final Versioned<MinecraftVersion, File> intermediaryServerNestsFile;
	private final Versioned<MinecraftVersion, File> intermediaryMergedNestsFile;

	private final Versioned<MinecraftVersion, File> namedNestsFile;

	private final Versioned<String, File> intermediaryFile;
	private final Versioned<String, File> intermediaryV2File;

	private final Versioned<String, File> namedMappingsJar;
	private final Versioned<String, File> namedMappingsFile;

	private final Versioned<MinecraftVersion, File> clientExceptions;
	private final Versioned<MinecraftVersion, File> serverExceptions;
	private final Versioned<MinecraftVersion, File> mergedExceptions;
	private final Versioned<MinecraftVersion, File> clientSignatures;
	private final Versioned<MinecraftVersion, File> serverSignatures;
	private final Versioned<MinecraftVersion, File> mergedSignatures;

	private final Versioned<MinecraftVersion, File> setupClientJar;
	private final Versioned<MinecraftVersion, File> setupServerJar;
	private final Versioned<MinecraftVersion, File> setupMergedJar;
	private final Versioned<MinecraftVersion, File> intermediarySetupClientJar;
	private final Versioned<MinecraftVersion, File> intermediarySetupServerJar;
	private final Versioned<MinecraftVersion, File> intermediarySetupMergedJar;

	private final Versioned<MinecraftVersion, File> sourceClientJar;
	private final Versioned<MinecraftVersion, File> sourceServerJar;
	private final Versioned<MinecraftVersion, File> sourceMergedJar;
	private final Versioned<MinecraftVersion, File> namedSourceClientJar;
	private final Versioned<MinecraftVersion, File> namedSourceServerJar;
	private final Versioned<MinecraftVersion, File> namedSourceMergedJar;
	private final Versioned<MinecraftVersion, File> processedNamedSourceJar;

	private final Versioned<MinecraftVersion, File> setupClientIntermediaryMappings;
	private final Versioned<MinecraftVersion, File> setupServerIntermediaryMappings;
	private final Versioned<MinecraftVersion, File> setupMergedIntermediaryMappings;
	private final Versioned<MinecraftVersion, File> setupClientNamedMappings;
	private final Versioned<MinecraftVersion, File> setupServerNamedMappings;
	private final Versioned<MinecraftVersion, File> setupMergedNamedMappings;

	private final Versioned<MinecraftVersion, File> sourceClientMappings;
	private final Versioned<MinecraftVersion, File> sourceServerMappings;
	private final Versioned<MinecraftVersion, File> sourceMergedMappings;

	private final Versioned<MinecraftVersion, File> setupClientExceptions;
	private final Versioned<MinecraftVersion, File> setupServerExceptions;
	private final Versioned<MinecraftVersion, File> setupMergedExceptions;
	private final Versioned<MinecraftVersion, File> setupClientSignatures;
	private final Versioned<MinecraftVersion, File> setupServerSignatures;
	private final Versioned<MinecraftVersion, File> setupMergedSignatures;

	private final Versioned<MinecraftVersion, File> baseClientExceptions;
	private final Versioned<MinecraftVersion, File> baseServerExceptions;
	private final Versioned<MinecraftVersion, File> baseMergedExceptions;
	private final Versioned<MinecraftVersion, File> baseClientSignatures;
	private final Versioned<MinecraftVersion, File> baseServerSignatures;
	private final Versioned<MinecraftVersion, File> baseMergedSignatures;

	private final Versioned<MinecraftVersion, File> generatedClientJar;
	private final Versioned<MinecraftVersion, File> generatedServerJar;
	private final Versioned<MinecraftVersion, File> generatedMergedJar;
	private final Versioned<MinecraftVersion, File> namedGeneratedClientJar;
	private final Versioned<MinecraftVersion, File> namedGeneratedServerJar;
	private final Versioned<MinecraftVersion, File> namedGeneratedMergedJar;

	private final Versioned<MinecraftVersion, File> generatedClientExceptions;
	private final Versioned<MinecraftVersion, File> generatedServerExceptions;
	private final Versioned<MinecraftVersion, File> generatedMergedExceptions;
	private final Versioned<MinecraftVersion, File> generatedClientSignatures;
	private final Versioned<MinecraftVersion, File> generatedServerSignatures;
	private final Versioned<MinecraftVersion, File> generatedMergedSignatures;

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
		this.exceptionsCache = cacheDirectoryProperty(() -> new File(getGlobalBuildCache(), "exceptions"));
		this.signaturesCache = cacheDirectoryProperty(() -> new File(getGlobalBuildCache(), "signatures"));
		this.nestsCache = cacheDirectoryProperty(() -> new File(getGlobalBuildCache(), "nests"));

		this.namedMappingsBuildsCache = fileProperty(() -> this.project.file("named-mappings-builds.json"));
		this.exceptionsBuildsCache = fileProperty(() -> this.project.file("exceptions-builds.json"));
		this.signaturesBuildsCache = fileProperty(() -> this.project.file("signatures-builds.json"));
		this.nestsBuildsCache = fileProperty(() -> this.project.file("nests-builds.json"));
		this.enigmaProfile = fileProperty(() -> this.project.file("enigma/enigma_profile.json"));
		this.mappingsDir = fileProperty(() -> this.project.file("mappings"));
		this.exceptionsDir = fileProperty(() -> this.project.file("exceptions"));
		this.signaturesDir = fileProperty(() -> this.project.file("signatures"));
		this.matchesDir = fileProperty(() -> this.project.file("matches/matches"));
		this.runDir = fileProperty(() -> this.project.file("run"));
		this.workingDir = new Versioned<>(minecraftVersion -> new File(getRunDirectory(), minecraftVersion.id()));
		this.enigmaSessionLock = new Versioned<>(minecraftVersion -> new File(getWorkingDirectory(minecraftVersion), EnigmaSession.LOCK_FILE));
		this.decompiledSrcDir = new Versioned<>(minecraftVersion -> this.project.file("%s-decompiledSrc".formatted(minecraftVersion.id())));
		this.fakeSrcDir = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-fakeSrc".formatted(minecraftVersion)));
		this.javadocDir = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-javadoc".formatted(minecraftVersion)));

		this.versionsManifest = fileProperty(() -> {
			String url = keratin.getVersionsManifestUrl().get();

			if (Constants.VERSIONS_MANIFEST_URL.equals(url)) {
				return new File(getGlobalBuildCache(), "versions-manifest.json");
			} else {
				return new File(getGlobalBuildCache(), "versions-manifest-" + Integer.toHexString(url.hashCode()) + ".json");
			}
		});
		this.versionInfos = new Versioned<>(minecraftVersion -> new File(getVersionJsonsCache(), "%s-info.json".formatted(minecraftVersion)));
		this.versionDetails = new Versioned<>(minecraftVersion -> new File(getVersionJsonsCache(), "%s-details.json".formatted(minecraftVersion)));

		this.clientJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient()) {
				throw new NoSuchFileException("client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else {
				return new File(getGameJarsCache(), "%s-client.jar".formatted(minecraftVersion.client().id()));
			}
		});
		this.serverJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasServer()) {
				throw new NoSuchFileException("server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else {
				return new File(getGameJarsCache(), "%s-server.jar".formatted(minecraftVersion.server().id()));
			}
		});
		this.mergedJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
				throw new NoSuchFileException("game jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
			} else if (!minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("game jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: the client and server jars do not have shared mappings!");
			} else {
				return new File(getGameJarsCache(), "%s-merged.jar".formatted(minecraftVersion.id()));
			}
		});
		this.intermediaryClientJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient()) {
				throw new NoSuchFileException("client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("intermediray client jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
			} else {
				return new File(getMappedJarsCache(), "%s-intermediary-gen%d-client.jar".formatted(minecraftVersion.client().id(), getIntermediaryGen()));
			}
		});
		this.intermediaryServerJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasServer()) {
				throw new NoSuchFileException("server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("intermediray server jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
			} else {
				return new File(getMappedJarsCache(), "%s-intermediary-gen%d-server.jar".formatted(minecraftVersion.server().id(), getIntermediaryGen()));
			}
		});
		this.intermediaryMergedJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
				throw new NoSuchFileException("intermediary jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
			} else {
				return new File(getMappedJarsCache(), "%s-intermediary-gen%d-merged.jar".formatted(minecraftVersion.id(), getIntermediaryGen()));
			}
		});
		this.lvtPatchedIntermediaryClientJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient() || minecraftVersion.hasServer()) {
				throw new NoSuchFileException("intermediary client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else {
				return new File(getProcessedJarsCache(), "%s-lvt-patched-intermediary-gen%d-client.jar".formatted(minecraftVersion.client().id(), getIntermediaryGen()));
			}
		});
		this.lvtPatchedIntermediaryServerJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasServer() || minecraftVersion.hasClient()) {
				throw new NoSuchFileException("intermediary server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else {
				return new File(getProcessedJarsCache(), "%s-lvt-patched-intermediary-gen%d-server.jar".formatted(minecraftVersion.server().id(), getIntermediaryGen()));
			}
		});
		this.lvtPatchedIntermediaryMergedJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
				throw new NoSuchFileException("intermediary jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
			} else {
				return new File(getProcessedJarsCache(), "%s-lvt-patched-intermediary-gen%d-merged.jar".formatted(minecraftVersion.id(), getIntermediaryGen()));
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
					return new File(getProcessedJarsCache(), "%s-exceptions+build.%d-intermediary-gen%d-client.jar".formatted(minecraftVersion.client().id(), build, getIntermediaryGen()));
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
					return new File(getProcessedJarsCache(), "%s-exceptions+build.%d-intermediary-gen%d-server.jar".formatted(minecraftVersion.server().id(), build, getIntermediaryGen()));
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
						return new File(getProcessedJarsCache(), "%s-exceptions+build.(%d-%d)-intermediary-gen%d-merged.jar".formatted(minecraftVersion.id(), clientBuild, serverBuild, getIntermediaryGen()));
					}
				} else {
					return new File(getProcessedJarsCache(), "%s-exceptions+build.%d-intermediary-gen%d-merged.jar".formatted(minecraftVersion.id(), build, getIntermediaryGen()));
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
					return new File(getProcessedJarsCache(), "%s-signatures+build.%d-intermediary-gen%d-client.jar".formatted(minecraftVersion.client().id(), build, getIntermediaryGen()));
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
					return new File(getProcessedJarsCache(), "%s-signatures+build.%d-intermediary-gen%d-server.jar".formatted(minecraftVersion.server().id(), build, getIntermediaryGen()));
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
						return new File(getProcessedJarsCache(), "%s-signatures+build.(%d-%d)-intermediary-gen%d-merged.jar".formatted(minecraftVersion.id(), clientBuild, serverBuild, getIntermediaryGen()));
					}
				} else {
					return new File(getProcessedJarsCache(), "%s-signatures+build.%d-intermediary-gen%d-merged.jar".formatted(minecraftVersion.id(), build, getIntermediaryGen()));
				}
			}
		});
		this.preenedIntermediaryClientJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient() || minecraftVersion.hasServer()) {
				throw new NoSuchFileException("intermediary client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else {
				return new File(getProcessedJarsCache(), "%s-preened-intermediary-gen%d-client.jar".formatted(minecraftVersion.client().id(), getIntermediaryGen()));
			}
		});
		this.preenedIntermediaryServerJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasServer() || minecraftVersion.hasClient()) {
				throw new NoSuchFileException("intermediary server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else {
				return new File(getProcessedJarsCache(), "%s-preened-intermediary-gen%d-server.jar".formatted(minecraftVersion.server().id(), getIntermediaryGen()));
			}
		});
		this.preenedIntermediaryMergedJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
				throw new NoSuchFileException("intermediary jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
			} else {
				return new File(getProcessedJarsCache(), "%s-preened-intermediary-gen%d-merged.jar".formatted(minecraftVersion.id(), getIntermediaryGen()));
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
					return new File(getProcessedJarsCache(), "%s-nests+build.%d-intermediary-gen%d-client.jar".formatted(minecraftVersion.client().id(), build, getIntermediaryGen()));
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
					return new File(getProcessedJarsCache(), "%s-nests+build.%d-intermediary-gen%d-server.jar".formatted(minecraftVersion.server().id(), build, getIntermediaryGen()));
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
						return new File(getProcessedJarsCache(), "%s-nests+build.(%d-%d)-intermediary-gen%d-merged.jar".formatted(minecraftVersion.id(), clientBuild, serverBuild, getIntermediaryGen()));
					}
				} else {
					return new File(getProcessedJarsCache(), "%s-nests+build.%d-intermediary-gen%d-merged.jar".formatted(minecraftVersion.id(), build, getIntermediaryGen()));
				}
			}
		});
		this.processedIntermediaryClientJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient() || minecraftVersion.hasServer()) {
				throw new NoSuchFileException("intermediary client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else {
				return new File(getProcessedJarsCache(), "%s-processed-intermediary-gen%d-client.jar".formatted(minecraftVersion.client().id(), getIntermediaryGen()));
			}
		});
		this.processedIntermediaryServerJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasServer() || minecraftVersion.hasClient()) {
				throw new NoSuchFileException("intermediary server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else {
				return new File(getProcessedJarsCache(), "%s-processed-intermediary-gen%d-server.jar".formatted(minecraftVersion.server().id(), getIntermediaryGen()));
			}
		});
		this.processedIntermediaryMergedJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
				throw new NoSuchFileException("intermediary jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
			} else {
				return new File(getProcessedJarsCache(), "%s-processed-intermediary-gen%d-merged.jar".formatted(minecraftVersion.id(), getIntermediaryGen()));
			}
		});

		this.namedJar = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-named.jar".formatted(minecraftVersion)));
		this.processedNamedJar = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-processed-named.jar".formatted(minecraftVersion)));
		this.javadocNamedJar = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-javadoc-named.jar".formatted(minecraftVersion)));

		this.clientIntermediaryMappingsJar = new Versioned<>(minecraftVersion -> new File(getMappingsCache(), "%s-intermediary-gen%d-client.jar".formatted(minecraftVersion.client().id(), getIntermediaryGen())));
		this.serverIntermediaryMappingsJar = new Versioned<>(minecraftVersion -> new File(getMappingsCache(), "%s-intermediary-gen%d-server.jar".formatted(minecraftVersion.server().id(), getIntermediaryGen())));
		this.mergedIntermediaryMappingsJar = new Versioned<>(minecraftVersion -> new File(getMappingsCache(), "%s-intermediary-gen%d-merged.jar".formatted(minecraftVersion.id(), getIntermediaryGen())));
		this.clientIntermediaryMappings = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient()) {
				throw new NoSuchFileException("client intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("client intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
			} else {
				return new File(getMappingsCache(), "%s-intermediary-gen%d-client.tiny".formatted(minecraftVersion.client().id(), getIntermediaryGen()));
			}
		});
		this.serverIntermediaryMappings = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasServer()) {
				throw new NoSuchFileException("server intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("server intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
			} else {
				return new File(getMappingsCache(), "%s-intermediary-gen%d-server.tiny".formatted(minecraftVersion.server().id(), getIntermediaryGen()));
			}
		});
		this.mergedIntermediaryMappings = new Versioned<>(minecraftVersion -> new File(getMappingsCache(), "%s-intermediary-gen%d-merged.tiny".formatted(minecraftVersion.id(), getIntermediaryGen())));
		this.filledClientIntermediaryMappings = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient()) {
				throw new NoSuchFileException("client intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("client intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
			} else {
				return new File(getMappingsCache(), "%s-filled-intermediary-gen%d-client.tiny".formatted(minecraftVersion.client().id(), getIntermediaryGen()));
			}
		});
		this.filledServerIntermediaryMappings = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasServer()) {
				throw new NoSuchFileException("server intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("server intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
			} else {
				return new File(getMappingsCache(), "%s-filled-intermediary-gen%d-server.tiny".formatted(minecraftVersion.server().id(), getIntermediaryGen()));
			}
		});
		this.filledMergedIntermediaryMappings = new Versioned<>(minecraftVersion -> new File(getMappingsCache(), "%s-filled-intermediary-gen%d-merged.tiny".formatted(minecraftVersion.id(), getIntermediaryGen())));

		this.namedMappings = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-named.tiny".formatted(minecraftVersion.id())));
		this.processedNamedMappings = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-processed-named.tiny".formatted(minecraftVersion.id())));
		this.completedNamedMappings = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-completed.tiny".formatted(minecraftVersion.id())));
		this.tinyV1NamedMappings = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-tiny-v1.tiny".formatted(minecraftVersion)));
		this.tinyV2NamedMappings = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-tiny-v2.tiny".formatted(minecraftVersion)));
		this.mergedTinyV1NamedMappings = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-merged-tiny-v1.tiny".formatted(minecraftVersion)));
		this.mergedTinyV2NamedMappings = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-merged-tiny-v2.tiny".formatted(minecraftVersion)));
		this.compressedMergedTinyV1NamedMappings = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-merged-tiny-v1.gz".formatted(minecraftVersion)));

		this.clientExceptionsJar = new Versioned<>(minecraftVersion -> new File(getExceptionsCache(), "%s-exceptions+build.%d-client.jar".formatted(minecraftVersion.client().id(), keratin.getExceptionsBuild(minecraftVersion, GameSide.CLIENT))));
		this.serverExceptionsJar = new Versioned<>(minecraftVersion -> new File(getExceptionsCache(), "%s-exceptions+build.%d-server.jar".formatted(minecraftVersion.server().id(), keratin.getExceptionsBuild(minecraftVersion, GameSide.SERVER))));
		this.mergedExceptionsJar = new Versioned<>(minecraftVersion -> new File(getExceptionsCache(), "%s-exceptions+build.%d-merged.jar".formatted(minecraftVersion.id(), keratin.getExceptionsBuild(minecraftVersion, GameSide.MERGED))));
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
					return new File(getExceptionsCache(), "%s-exceptions+build.%d-client.excs".formatted(minecraftVersion.client().id(), build));
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
					return new File(getExceptionsCache(), "%s-exceptions+build.%d-server.excs".formatted(minecraftVersion.server().id(), build));
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
					return new File(getExceptionsCache(), "%s-exceptions+build.%d-merged.excs".formatted(minecraftVersion.id(), build));
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
					return new File(getExceptionsCache(), "%s-intermediary-gen%d-exceptions+build.%d-client.excs".formatted(minecraftVersion.client().id(), getIntermediaryGen(), build));
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
					return new File(getExceptionsCache(), "%s-intermediary-gen%d-exceptions+build.%d-server.excs".formatted(minecraftVersion.server().id(), getIntermediaryGen(), build));
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
						return new File(getExceptionsCache(), "%s-intermediary-gen%d-exceptions+build.(%d-%d)-merged.excs".formatted(minecraftVersion.id(), getIntermediaryGen(), clientBuild, serverBuild));
					}
				} else {
					return new File(getExceptionsCache(), "%s-intermediary-gen%d-exceptions+build.%d-merged.excs".formatted(minecraftVersion.id(), getIntermediaryGen(), build));
				}
			}
		});

		this.namedExceptionsFile = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-named-exceptions.excs".formatted(minecraftVersion.id())));

		this.clientSignaturesJar = new Versioned<>(minecraftVersion -> new File(getSignaturesCache(), "%s-signatures+build.%d-client.jar".formatted(minecraftVersion.client().id(), keratin.getSignaturesBuild(minecraftVersion, GameSide.CLIENT))));
		this.serverSignaturesJar = new Versioned<>(minecraftVersion -> new File(getSignaturesCache(), "%s-signatures+build.%d-server.jar".formatted(minecraftVersion.server().id(), keratin.getSignaturesBuild(minecraftVersion, GameSide.SERVER))));
		this.mergedSignaturesJar = new Versioned<>(minecraftVersion -> new File(getSignaturesCache(), "%s-signatures+build.%d-merged.jar".formatted(minecraftVersion.id(), keratin.getSignaturesBuild(minecraftVersion, GameSide.MERGED))));
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
					return new File(getSignaturesCache(), "%s-signatures+build.%d-client.sigs".formatted(minecraftVersion.client().id(), build));
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
					return new File(getSignaturesCache(), "%s-signatures+build.%d-server.sigs".formatted(minecraftVersion.server().id(), build));
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
					return new File(getSignaturesCache(), "%s-signatures+build.%d-merged.sigs".formatted(minecraftVersion.id(), build));
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
					return new File(getSignaturesCache(), "%s-intermediary-gen%d-signatures+build.%d-client.sigs".formatted(minecraftVersion.client().id(), getIntermediaryGen(), build));
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
					return new File(getSignaturesCache(), "%s-intermediary-gen%d-signatures+build.%d-server.sigs".formatted(minecraftVersion.server().id(), getIntermediaryGen(), build));
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
						return new File(getSignaturesCache(), "%s-intermediary-gen%d-signatures+build.(%d-%d)-merged.sigs".formatted(minecraftVersion.id(), getIntermediaryGen(), clientBuild, serverBuild));
					}
				} else {
					return new File(getSignaturesCache(), "%s-intermediary-gen%d-signatures+build.%d-merged.sigs".formatted(minecraftVersion.id(), getIntermediaryGen(), build));
				}
			}
		});

		this.namedSignaturesFile = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-named-signatures.sigs".formatted(minecraftVersion.id())));

		this.clientNestsJar = new Versioned<>(minecraftVersion -> new File(getNestsCache(), "%s-nests+build.%d-client.jar".formatted(minecraftVersion.client().id(), keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT))));
		this.serverNestsJar = new Versioned<>(minecraftVersion -> new File(getNestsCache(), "%s-nests+build.%d-server.jar".formatted(minecraftVersion.server().id(), keratin.getNestsBuild(minecraftVersion, GameSide.SERVER))));
		this.mergedNestsJar = new Versioned<>(minecraftVersion -> new File(getNestsCache(), "%s-nests+build.%d-merged.jar".formatted(minecraftVersion.id(), keratin.getNestsBuild(minecraftVersion, GameSide.MERGED))));
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
					return new File(getNestsCache(), "%s-nests+build.%d-client.nest".formatted(minecraftVersion.client().id(), build));
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
					return new File(getNestsCache(), "%s-nests+build.%d-server.nest".formatted(minecraftVersion.server().id(), build));
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
					return new File(getNestsCache(), "%s-nests+build.%d-merged.nest".formatted(minecraftVersion.id(), build));
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
					return new File(getNestsCache(), "%s-intermediary-gen%d-nests+build.%d-client.nest".formatted(minecraftVersion.client().id(), getIntermediaryGen(), build));
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
					return new File(getNestsCache(), "%s-intermediary-gen%d-nests+build.%d-server.nest".formatted(minecraftVersion.server().id(), getIntermediaryGen(), build));
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
						return new File(getNestsCache(), "%s-intermediary-gen%d-nests+build.(%d-%d)-merged.nest".formatted(minecraftVersion.id(), getIntermediaryGen(), clientBuild, serverBuild));
					}
				} else {
					return new File(getNestsCache(), "%s-intermediary-gen%d-nests+build.%d-merged.nest".formatted(minecraftVersion.id(), getIntermediaryGen(), build));
				}
			}
		});

		this.namedNestsFile = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-named-nests.nest".formatted(minecraftVersion.id())));

		this.intermediaryFile = new Versioned<>(minecraftVersion -> new File(getMappingsDirectory(), "%s.tiny".formatted(minecraftVersion)));
		this.intermediaryV2File = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s.tiny".formatted(minecraftVersion)));

		this.namedMappingsJar = new Versioned<>(minecraftVersion -> new File(getMappingsCache(), "%s-named-gen%d+build.%d.jar".formatted(minecraftVersion, getIntermediaryGen(), keratin.getNamedMappingsBuild(minecraftVersion))));
		this.namedMappingsFile = new Versioned<>(minecraftVersion -> {
			int build = keratin.getNamedMappingsBuild(minecraftVersion);

			if (build < 1) {
				throw new NoSuchFileException("no named mappings builds for Minecraft version " + minecraftVersion + " exist yet!");
			}

			return new File(getMappingsCache(), "%s-named-gen%d+build.%d.tiny".formatted(minecraftVersion, getIntermediaryGen(), build));
		});

		this.clientExceptions = new Versioned<>(minecraftVersion -> {
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
		this.serverExceptions = new Versioned<>(minecraftVersion -> {
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
		this.mergedExceptions = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("game jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: the client and server jars do not have shared mappings!");
			} else {
				return new File(getExceptionsDirectory(), "%s.excs".formatted(minecraftVersion.id()));
			}
		});
		this.clientSignatures = new Versioned<>(minecraftVersion -> {
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
		this.serverSignatures = new Versioned<>(minecraftVersion -> {
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
		this.mergedSignatures = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("game jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: the client and server jars do not have shared mappings!");
			} else {
				return new File(getSignaturesDirectory(), "%s.sigs".formatted(minecraftVersion.id()));
			}
		});

		this.setupClientJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient()) {
				throw new NoSuchFileException("client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("setup client jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
			} else {
				return new File(getMappedJarsCache(), "%s-setup-client.jar".formatted(minecraftVersion.client().id()));
			}
		});
		this.setupServerJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasServer()) {
				throw new NoSuchFileException("server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("setup server jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
			} else {
				return new File(getLocalBuildCache(), "%s-setup-server.jar".formatted(minecraftVersion.server().id()));
			}
		});
		this.setupMergedJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
				throw new NoSuchFileException("setup jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
			} else {
				return new File(getLocalBuildCache(), "%s-setup-merged.jar".formatted(minecraftVersion.id()));
			}
		});
		this.intermediarySetupClientJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient()) {
				throw new NoSuchFileException("client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("setup client jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
			} else {
				return new File(getLocalBuildCache(), "%s-intermediary-setup-client.jar".formatted(minecraftVersion.client().id()));
			}
		});
		this.intermediarySetupServerJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasServer()) {
				throw new NoSuchFileException("server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("setup server jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
			} else {
				return new File(getLocalBuildCache(), "%s-intermediary-setup-server.jar".formatted(minecraftVersion.server().id()));
			}
		});
		this.intermediarySetupMergedJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
				throw new NoSuchFileException("setup jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
			} else {
				return new File(getLocalBuildCache(), "%s-intermediary-setup-merged.jar".formatted(minecraftVersion.id()));
			}
		});

		this.sourceClientJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient()) {
				throw new NoSuchFileException("client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("source client jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
			} else {
				return new File(getMappedJarsCache(), "%s-source-client.jar".formatted(minecraftVersion.client().id()));
			}
		});
		this.sourceServerJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasServer()) {
				throw new NoSuchFileException("server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("source server jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
			} else {
				return new File(getLocalBuildCache(), "%s-source-server.jar".formatted(minecraftVersion.server().id()));
			}
		});
		this.sourceMergedJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
				throw new NoSuchFileException("source jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
			} else {
				return new File(getLocalBuildCache(), "%s-source-merged.jar".formatted(minecraftVersion.id()));
			}
		});
		this.namedSourceClientJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient()) {
				throw new NoSuchFileException("client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("source client jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
			} else {
				return new File(getLocalBuildCache(), "%s-named-source-client.jar".formatted(minecraftVersion.client().id()));
			}
		});
		this.namedSourceServerJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasServer()) {
				throw new NoSuchFileException("server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("source server jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
			} else {
				return new File(getLocalBuildCache(), "%s-named-source-server.jar".formatted(minecraftVersion.server().id()));
			}
		});
		this.namedSourceMergedJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
				throw new NoSuchFileException("source jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
			} else {
				return new File(getLocalBuildCache(), "%s-named-source-merged.jar".formatted(minecraftVersion.id()));
			}
		});
		this.processedNamedSourceJar = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-processed-named-source.jar".formatted(minecraftVersion.id())));

		this.setupClientIntermediaryMappings = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient()) {
				throw new NoSuchFileException("client intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("client intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
			} else {
				return new File(getLocalBuildCache(), "%s-setup-intermediary-gen%d-client.tiny".formatted(minecraftVersion.client().id(), getIntermediaryGen()));
			}
		});
		this.setupServerIntermediaryMappings = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasServer()) {
				throw new NoSuchFileException("server intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("server intermediary mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
			} else {
				return new File(getLocalBuildCache(), "%s-setup-intermediary-gen%d-server.tiny".formatted(minecraftVersion.server().id(), getIntermediaryGen()));
			}
		});
		this.setupMergedIntermediaryMappings = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-setup-intermediary-gen%d-merged.tiny".formatted(minecraftVersion.id(), getIntermediaryGen())));
		this.setupClientNamedMappings = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient()) {
				throw new NoSuchFileException("client named mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("client named mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
			} else {
				return new File(getLocalBuildCache(), "%s-setup-named-client.tiny".formatted(minecraftVersion.client().id()));
			}
		});
		this.setupServerNamedMappings = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasServer()) {
				throw new NoSuchFileException("server named mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("server named mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
			} else {
				return new File(getLocalBuildCache(), "%s-setup-named-server.tiny".formatted(minecraftVersion.server().id()));
			}
		});
		this.setupMergedNamedMappings = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-setup-named-merged.tiny".formatted(minecraftVersion.id())));

		this.sourceClientMappings = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient()) {
				throw new NoSuchFileException("client mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("client mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
			} else {
				return new File(getLocalBuildCache(), "%s-source-client.tiny".formatted(minecraftVersion.client().id()));
			}
		});
		this.sourceServerMappings = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasServer()) {
				throw new NoSuchFileException("server mappings for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("server mappings for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged mappings!");
			} else {
				return new File(getLocalBuildCache(), "%s-source-server.tiny".formatted(minecraftVersion.server().id()));
			}
		});
		this.sourceMergedMappings = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-source-merged.tiny".formatted(minecraftVersion.id())));

		this.setupClientExceptions = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient()) {
				throw new NoSuchFileException("client exceptions for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("client exceptions for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged exceptions!");
			} else {
				return new File(getLocalBuildCache(), "%s-setup-client.excs".formatted(minecraftVersion.client().id()));
			}
		});
		this.setupServerExceptions = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasServer()) {
				throw new NoSuchFileException("server exceptions for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("server exceptions for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged exceptions!");
			} else {
				return new File(getLocalBuildCache(), "%s-setup-server.excs".formatted(minecraftVersion.server().id()));
			}
		});
		this.setupMergedExceptions = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-setup-merged.excs".formatted(minecraftVersion.id())));
		this.setupClientSignatures = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient()) {
				throw new NoSuchFileException("client signatures for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("client signatures for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged signatures!");
			} else {
				return new File(getLocalBuildCache(), "%s-setup-client.sigs".formatted(minecraftVersion.client().id()));
			}
		});
		this.setupServerSignatures = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasServer()) {
				throw new NoSuchFileException("server signatures for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("server signatures for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged signatures!");
			} else {
				return new File(getLocalBuildCache(), "%s-setup-server.sigs".formatted(minecraftVersion.server().id()));
			}
		});
		this.setupMergedSignatures = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-setup-merged.sigs".formatted(minecraftVersion.id())));

		this.baseClientExceptions = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient()) {
				throw new NoSuchFileException("client exceptions for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("client exceptions for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged exceptions!");
			} else {
				return new File(getLocalBuildCache(), "%s-base-client.excs".formatted(minecraftVersion.client().id()));
			}
		});
		this.baseServerExceptions = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasServer()) {
				throw new NoSuchFileException("server exceptions for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("server exceptions for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged exceptions!");
			} else {
				return new File(getLocalBuildCache(), "%s-base-server.excs".formatted(minecraftVersion.server().id()));
			}
		});
		this.baseMergedExceptions = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-base-merged.excs".formatted(minecraftVersion.id())));
		this.baseClientSignatures = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient()) {
				throw new NoSuchFileException("client signatures for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("client signatures for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged signatures!");
			} else {
				return new File(getLocalBuildCache(), "%s-base-client.sigs".formatted(minecraftVersion.client().id()));
			}
		});
		this.baseServerSignatures = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasServer()) {
				throw new NoSuchFileException("server signatures for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("server signatures for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged signatures!");
			} else {
				return new File(getLocalBuildCache(), "%s-base-server.sigs".formatted(minecraftVersion.server().id()));
			}
		});
		this.baseMergedSignatures = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-base-merged.sigs".formatted(minecraftVersion.id())));

		this.generatedClientJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient()) {
				throw new NoSuchFileException("client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("generated client jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
			} else {
				return new File(getLocalBuildCache(), "%s-generated-client.jar".formatted(minecraftVersion.client().id()));
			}
		});
		this.generatedServerJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasServer()) {
				throw new NoSuchFileException("server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("generated server jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
			} else {
				return new File(getLocalBuildCache(), "%s-generated-server.jar".formatted(minecraftVersion.server().id()));
			}
		});
		this.generatedMergedJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
				throw new NoSuchFileException("generated jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
			} else {
				return new File(getLocalBuildCache(), "%s-generated-merged.jar".formatted(minecraftVersion.id()));
			}
		});
		this.namedGeneratedClientJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient()) {
				throw new NoSuchFileException("client jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("generated client jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
			} else {
				return new File(getLocalBuildCache(), "%s-named-generated-client.jar".formatted(minecraftVersion.client().id()));
			}
		});
		this.namedGeneratedServerJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasServer()) {
				throw new NoSuchFileException("server jar for Minecraft version " + minecraftVersion.id() + " does not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("generated server jar for Minecraft version " + minecraftVersion.id() + " does not exist: please use the merged jar!");
			} else {
				return new File(getLocalBuildCache(), "%s-named-generated-server.jar".formatted(minecraftVersion.server().id()));
			}
		});
		this.namedGeneratedMergedJar = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
				throw new NoSuchFileException("generated jars for Minecraft version " + minecraftVersion.id() + " cannot be merged: either the client or server jar does not exist!");
			} else {
				return new File(getLocalBuildCache(), "%s-named-generated-merged.jar".formatted(minecraftVersion.id()));
			}
		});

		this.generatedClientExceptions = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient()) {
				throw new NoSuchFileException("client exceptions for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("client exceptions for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged exceptions!");
			} else {
				return new File(getLocalBuildCache(), "%s-generated-client.excs".formatted(minecraftVersion.client().id()));
			}
		});
		this.generatedServerExceptions = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasServer()) {
				throw new NoSuchFileException("server exceptions for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("server exceptions for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged exceptions!");
			} else {
				return new File(getLocalBuildCache(), "%s-generated-server.excs".formatted(minecraftVersion.server().id()));
			}
		});
		this.generatedMergedExceptions = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-generated-merged.excs".formatted(minecraftVersion.id())));
		this.generatedClientSignatures = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasClient()) {
				throw new NoSuchFileException("client signatures for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasServer() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("client signatures for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged signatures!");
			} else {
				return new File(getLocalBuildCache(), "%s-generated-client.sigs".formatted(minecraftVersion.client().id()));
			}
		});
		this.generatedServerSignatures = new Versioned<>(minecraftVersion -> {
			if (!minecraftVersion.hasServer()) {
				throw new NoSuchFileException("server signatures for Minecraft version " + minecraftVersion.id() + " do not exist!");
			} else if (minecraftVersion.hasClient() && minecraftVersion.hasSharedObfuscation()) {
				throw new NoSuchFileException("server signatures for Minecraft version " + minecraftVersion.id() + " do not exist: please use the merged signatures!");
			} else {
				return new File(getLocalBuildCache(), "%s-generated-server.sigs".formatted(minecraftVersion.server().id()));
			}
		});
		this.generatedMergedSignatures = new Versioned<>(minecraftVersion -> new File(getLocalBuildCache(), "%s-generated-merged.sigs".formatted(minecraftVersion.id())));
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

	private File pickFileForPresentSides(MinecraftVersion minecraftVersion, Versioned<MinecraftVersion, File> client, Versioned<MinecraftVersion, File> server, Versioned<MinecraftVersion, File> merged) {
		if (minecraftVersion.hasClient() && minecraftVersion.hasServer()) {
			return merged.get(minecraftVersion);
		} else {
			if (minecraftVersion.hasClient()) {
				return client.get(minecraftVersion);
			}
			if (minecraftVersion.hasServer()) {
				return server.get(minecraftVersion);
			}
		}

		throw new RuntimeException("somehow Minecraft version " + minecraftVersion.id() + " is neither client nor server!");
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
	public File getExceptionsCache() {
		return exceptionsCache.get();
	}

	@Override
	public File getSignaturesCache() {
		return signaturesCache.get();
	}

	@Override
	public File getNestsCache() {
		return nestsCache.get();
	}

	@Override
	public File getNamedMappingsBuildsCache() {
		return namedMappingsBuildsCache.get();
	}

	@Override
	public File getExceptionsBuildsCache() {
		return exceptionsBuildsCache.get();
	}

	@Override
	public File getSignaturesBuildsCache() {
		return signaturesBuildsCache.get();
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
	public File getWorkingDirectory(MinecraftVersion minecraftVersion) {
		return workingDir.get(minecraftVersion);
	}

	@Override
	public File getEnigmaSessionLock(MinecraftVersion minecraftVersion) {
		return enigmaSessionLock.get(minecraftVersion);
	}

	@Override
	public File getDecompiledSourceDirectory(MinecraftVersion minecraftVersion) {
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

	@Override
	public File getMainIntermediaryJar(MinecraftVersion minecraftVersion) {
		return pickFileForPresentSides(minecraftVersion, intermediaryClientJar, intermediaryServerJar, intermediaryMergedJar);
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

	@Override
	public File getNamedJar(String minecraftVersion) {
		return namedJar.get(minecraftVersion);
	}

	@Override
	public File getProcessedNamedJar(String minecraftVersion) {
		return processedNamedJar.get(minecraftVersion);
	}

	@Override
	public File getJavadocNamedJar(String minecraftVersion) {
		return javadocNamedJar.get(minecraftVersion);
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
	public File getClientIntermediaryMappings(MinecraftVersion minecraftVersion) {
		return clientIntermediaryMappings.get(minecraftVersion);
	}

	@Override
	public File getServerIntermediaryMappings(MinecraftVersion minecraftVersion) {
		return serverIntermediaryMappings.get(minecraftVersion);
	}

	@Override
	public File getMergedIntermediaryMappings(MinecraftVersion minecraftVersion) {
		return mergedIntermediaryMappings.get(minecraftVersion);
	}

	@Override
	public File getMainIntermediaryMappings(MinecraftVersion minecraftVersion) {
		return pickFileForPresentSides(minecraftVersion, clientIntermediaryMappings, serverIntermediaryMappings, mergedIntermediaryMappings);
	}

	@Override
	public File getFilledClientIntermediaryMappings(MinecraftVersion minecraftVersion) {
		return filledClientIntermediaryMappings.get(minecraftVersion);
	}

	@Override
	public File getFilledServerIntermediaryMappings(MinecraftVersion minecraftVersion) {
		return filledServerIntermediaryMappings.get(minecraftVersion);
	}

	@Override
	public File getFilledMergedIntermediaryMappings(MinecraftVersion minecraftVersion) {
		return filledMergedIntermediaryMappings.get(minecraftVersion);
	}

	@Override
	public File getMainFilledIntermediaryMappings(MinecraftVersion minecraftVersion) {
		return pickFileForPresentSides(minecraftVersion, filledClientIntermediaryMappings, filledServerIntermediaryMappings, filledMergedIntermediaryMappings);
	}

	@Override
	public File getNamedMappings(MinecraftVersion minecraftVersion) {
		return namedMappings.get(minecraftVersion);
	}

	@Override
	public File getProcessedNamedMappings(MinecraftVersion minecraftVersion) {
		return processedNamedMappings.get(minecraftVersion);
	}

	@Override
	public File getCompletedNamedMappings(MinecraftVersion minecraftVersion) {
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
	public File getCompressedMergedTinyV1NamedMappings(String minecraftVersion) {
		return compressedMergedTinyV1NamedMappings.get(minecraftVersion);
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

	@Override
	public File getNamedExceptionsFile(MinecraftVersion minecraftVersion) {
		return namedExceptionsFile.get(minecraftVersion);
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

	@Override
	public File getNamedSignaturesFile(MinecraftVersion minecraftVersion) {
		return namedSignaturesFile.get(minecraftVersion);
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

	@Override
	public File getNamedNestsFile(MinecraftVersion minecraftVersion) {
		return namedNestsFile.get(minecraftVersion);
	}

	@Override
	public File getIntermediaryFile(String minecraftVersion) {
		return intermediaryFile.get(minecraftVersion);
	}

	@Override
	public File getIntermediaryV2File(String minecraftVersion) {
		return intermediaryV2File.get(minecraftVersion);
	}

	@Override
	public File getNamedMappingsJar(String minecraftVersion) {
		return namedMappingsJar.get(minecraftVersion);
	}

	@Override
	public File getNamedMappingsFile(String minecraftVersion) {
		return namedMappingsFile.get(minecraftVersion);
	}

	@Override
	public File getClientExceptions(MinecraftVersion minecraftVersion) {
		return clientExceptions.get(minecraftVersion);
	}

	@Override
	public File getServerExceptions(MinecraftVersion minecraftVersion) {
		return serverExceptions.get(minecraftVersion);
	}

	@Override
	public File getMergedExceptions(MinecraftVersion minecraftVersion) {
		return mergedExceptions.get(minecraftVersion);
	}

	@Override
	public File getClientSignatures(MinecraftVersion minecraftVersion) {
		return clientSignatures.get(minecraftVersion);
	}

	@Override
	public File getServerSignatures(MinecraftVersion minecraftVersion) {
		return serverSignatures.get(minecraftVersion);
	}

	@Override
	public File getMergedSignatures(MinecraftVersion minecraftVersion) {
		return mergedSignatures.get(minecraftVersion);
	}

	@Override
	public File getSetupClientJar(MinecraftVersion minecraftVersion) {
		return setupClientJar.get(minecraftVersion);
	}

	@Override
	public File getSetupServerJar(MinecraftVersion minecraftVersion) {
		return setupServerJar.get(minecraftVersion);
	}

	@Override
	public File getSetupMergedJar(MinecraftVersion minecraftVersion) {
		return setupMergedJar.get(minecraftVersion);
	}

	@Override
	public File getIntermediarySetupClientJar(MinecraftVersion minecraftVersion) {
		return intermediarySetupClientJar.get(minecraftVersion);
	}

	@Override
	public File getIntermediarySetupServerJar(MinecraftVersion minecraftVersion) {
		return intermediarySetupServerJar.get(minecraftVersion);
	}

	@Override
	public File getIntermediarySetupMergedJar(MinecraftVersion minecraftVersion) {
		return intermediarySetupMergedJar.get(minecraftVersion);
	}

	@Override
	public File getSourceClientJar(MinecraftVersion minecraftVersion) {
		return sourceClientJar.get(minecraftVersion);
	}

	@Override
	public File getSourceServerJar(MinecraftVersion minecraftVersion) {
		return sourceServerJar.get(minecraftVersion);
	}

	@Override
	public File getSourceMergedJar(MinecraftVersion minecraftVersion) {
		return sourceMergedJar.get(minecraftVersion);
	}

	@Override
	public File getNamedSourceClientJar(MinecraftVersion minecraftVersion) {
		return namedSourceClientJar.get(minecraftVersion);
	}

	@Override
	public File getNamedSourceServerJar(MinecraftVersion minecraftVersion) {
		return namedSourceServerJar.get(minecraftVersion);
	}

	@Override
	public File getNamedSourceMergedJar(MinecraftVersion minecraftVersion) {
		return namedSourceMergedJar.get(minecraftVersion);
	}

	@Override
	public File getProcessedNamedSourceJar(MinecraftVersion minecraftVersion) {
		return processedNamedSourceJar.get(minecraftVersion);
	}

	@Override
	public File getSetupClientIntermediaryMappings(MinecraftVersion minecraftVersion) {
		return setupClientIntermediaryMappings.get(minecraftVersion);
	}

	@Override
	public File getSetupServerIntermediaryMappings(MinecraftVersion minecraftVersion) {
		return setupServerIntermediaryMappings.get(minecraftVersion);
	}

	@Override
	public File getSetupMergedIntermediaryMappings(MinecraftVersion minecraftVersion) {
		return setupMergedIntermediaryMappings.get(minecraftVersion);
	}

	@Override
	public File getSetupClientNamedMappings(MinecraftVersion minecraftVersion) {
		return setupClientNamedMappings.get(minecraftVersion);
	}

	@Override
	public File getSetupServerNamedMappings(MinecraftVersion minecraftVersion) {
		return setupServerNamedMappings.get(minecraftVersion);
	}

	@Override
	public File getSetupMergedNamedMappings(MinecraftVersion minecraftVersion) {
		return setupMergedNamedMappings.get(minecraftVersion);
	}

	@Override
	public File getSourceClientMappings(MinecraftVersion minecraftVersion) {
		return sourceClientMappings.get(minecraftVersion);
	}

	@Override
	public File getSourceServerMappings(MinecraftVersion minecraftVersion) {
		return sourceServerMappings.get(minecraftVersion);
	}

	@Override
	public File getSourceMergedMappings(MinecraftVersion minecraftVersion) {
		return sourceMergedMappings.get(minecraftVersion);
	}

	@Override
	public File getSetupClientExceptions(MinecraftVersion minecraftVersion) {
		return setupClientExceptions.get(minecraftVersion);
	}

	@Override
	public File getSetupServerExceptions(MinecraftVersion minecraftVersion) {
		return setupServerExceptions.get(minecraftVersion);
	}

	@Override
	public File getSetupMergedExceptions(MinecraftVersion minecraftVersion) {
		return setupMergedExceptions.get(minecraftVersion);
	}

	@Override
	public File getSetupClientSignatures(MinecraftVersion minecraftVersion) {
		return setupClientSignatures.get(minecraftVersion);
	}

	@Override
	public File getSetupServerSignatures(MinecraftVersion minecraftVersion) {
		return setupServerSignatures.get(minecraftVersion);
	}

	@Override
	public File getSetupMergedSignatures(MinecraftVersion minecraftVersion) {
		return setupMergedSignatures.get(minecraftVersion);
	}

	@Override
	public File getBaseClientExceptions(MinecraftVersion minecraftVersion) {
		return baseClientExceptions.get(minecraftVersion);
	}

	@Override
	public File getBaseServerExceptions(MinecraftVersion minecraftVersion) {
		return baseServerExceptions.get(minecraftVersion);
	}

	@Override
	public File getBaseMergedExceptions(MinecraftVersion minecraftVersion) {
		return baseMergedExceptions.get(minecraftVersion);
	}

	@Override
	public File getBaseClientSignatures(MinecraftVersion minecraftVersion) {
		return baseClientSignatures.get(minecraftVersion);
	}

	@Override
	public File getBaseServerSignatures(MinecraftVersion minecraftVersion) {
		return baseServerSignatures.get(minecraftVersion);
	}

	@Override
	public File getBaseMergedSignatures(MinecraftVersion minecraftVersion) {
		return baseMergedSignatures.get(minecraftVersion);
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
	public File getGeneratedClientExceptions(MinecraftVersion minecraftVersion) {
		return generatedClientExceptions.get(minecraftVersion);
	}

	@Override
	public File getGeneratedServerExceptions(MinecraftVersion minecraftVersion) {
		return generatedServerExceptions.get(minecraftVersion);
	}

	@Override
	public File getGeneratedMergedExceptions(MinecraftVersion minecraftVersion) {
		return generatedMergedExceptions.get(minecraftVersion);
	}

	@Override
	public File getGeneratedClientSignatures(MinecraftVersion minecraftVersion) {
		return generatedClientSignatures.get(minecraftVersion);
	}

	@Override
	public File getGeneratedServerSignatures(MinecraftVersion minecraftVersion) {
		return generatedServerSignatures.get(minecraftVersion);
	}

	@Override
	public File getGeneratedMergedSignatures(MinecraftVersion minecraftVersion) {
		return generatedMergedSignatures.get(minecraftVersion);
	}
}
