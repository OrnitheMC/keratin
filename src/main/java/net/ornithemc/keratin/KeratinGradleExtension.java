package net.ornithemc.keratin;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.internal.tasks.DefaultSourceSetContainer;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.publish.PublicationContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.javadoc.Javadoc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.ornithemc.keratin.api.KeratinGradleExtensionAPI;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.PublicationsAPI;
import net.ornithemc.keratin.api.TaskSelection;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.manifest.VersionInfo;
import net.ornithemc.keratin.api.manifest.VersionInfo.Library;
import net.ornithemc.keratin.api.manifest.VersionInfo.Library.Downloads;
import net.ornithemc.keratin.api.manifest.VersionInfo.Library.Downloads.Artifact;
import net.ornithemc.keratin.api.manifest.VersionsManifest;
import net.ornithemc.keratin.api.maven.MetaSourcedMavenArtifactsAPI;
import net.ornithemc.keratin.api.settings.BuildNumbers;
import net.ornithemc.keratin.api.settings.ProcessorSettings;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.build.BuildMappingsJarTask;
import net.ornithemc.keratin.api.task.build.BuildMappingsTask;
import net.ornithemc.keratin.api.task.build.BuildProcessedMappingsTask;
import net.ornithemc.keratin.api.task.build.BuildUnpickDefinitionsTask;
import net.ornithemc.keratin.api.task.build.CheckMappingsTask;
import net.ornithemc.keratin.api.task.build.CompleteMappingsTask;
import net.ornithemc.keratin.api.task.build.CompressMappingsTask;
import net.ornithemc.keratin.api.task.build.PrepareBuildTask;
import net.ornithemc.keratin.api.task.decompiling.DecompileMinecraftWithCfrTask;
import net.ornithemc.keratin.api.task.decompiling.DecompileMinecraftWithVineflowerTask;
import net.ornithemc.keratin.api.task.enigma.LaunchEnigmaTask;
import net.ornithemc.keratin.api.task.generation.MakeBaseExceptionsTask;
import net.ornithemc.keratin.api.task.generation.MakeBaseSignaturesTask;
import net.ornithemc.keratin.api.task.generation.MakeGeneratedExceptionsTask;
import net.ornithemc.keratin.api.task.generation.MakeGeneratedJarsTask;
import net.ornithemc.keratin.api.task.generation.MakeGeneratedSignaturesTask;
import net.ornithemc.keratin.api.task.generation.MapGeneratedJarsTask;
import net.ornithemc.keratin.api.task.generation.SaveExceptionsTask;
import net.ornithemc.keratin.api.task.generation.SaveSignaturesTask;
import net.ornithemc.keratin.api.task.generation.SplitGeneratedJarTask;
import net.ornithemc.keratin.api.task.javadoc.GenerateFakeSourceTask;
import net.ornithemc.keratin.api.task.javadoc.MapMinecraftForJavadocTask;
import net.ornithemc.keratin.api.task.mapping.ConvertMappingsFromTinyV1ToTinyV2Task;
import net.ornithemc.keratin.api.task.mapping.DownloadIntermediaryTask;
import net.ornithemc.keratin.api.task.mapping.FillIntermediaryTask;
import net.ornithemc.keratin.api.task.mapping.GenerateIntermediaryTask;
import net.ornithemc.keratin.api.task.mapping.GenerateNewIntermediaryTask;
import net.ornithemc.keratin.api.task.mapping.MapExceptionsTask;
import net.ornithemc.keratin.api.task.mapping.MapMinecraftTask;
import net.ornithemc.keratin.api.task.mapping.MapNestsTask;
import net.ornithemc.keratin.api.task.mapping.MapProcessedMinecraftTask;
import net.ornithemc.keratin.api.task.mapping.MapSignaturesTask;
import net.ornithemc.keratin.api.task.mapping.Mapper;
import net.ornithemc.keratin.api.task.mapping.SplitIntermediaryTask;
import net.ornithemc.keratin.api.task.mapping.UpdateIntermediaryTask;
import net.ornithemc.keratin.api.task.mapping.graph.ExtendGraphTask;
import net.ornithemc.keratin.api.task.mapping.graph.LoadMappingsFromGraphTask;
import net.ornithemc.keratin.api.task.mapping.graph.ResetGraphTask;
import net.ornithemc.keratin.api.task.mapping.graph.SaveMappingsIntoGraphTask;
import net.ornithemc.keratin.api.task.merging.MergeExceptionsTask;
import net.ornithemc.keratin.api.task.merging.MergeIntermediaryTask;
import net.ornithemc.keratin.api.task.merging.MergeMinecraftJarsTask;
import net.ornithemc.keratin.api.task.merging.MergeNestsTask;
import net.ornithemc.keratin.api.task.merging.MergeSignaturesTask;
import net.ornithemc.keratin.api.task.minecraft.DownloadMinecraftJarsTask;
import net.ornithemc.keratin.api.task.processing.DownloadExceptionsTask;
import net.ornithemc.keratin.api.task.processing.DownloadNestsTask;
import net.ornithemc.keratin.api.task.processing.DownloadSignaturesTask;
import net.ornithemc.keratin.api.task.processing.ProcessMinecraftTask;
import net.ornithemc.keratin.api.task.processing.UpdateBuildsCacheTask;
import net.ornithemc.keratin.api.task.setup.DownloadNamedMappingsTask;
import net.ornithemc.keratin.api.task.setup.MakeSetupExceptionsTask;
import net.ornithemc.keratin.api.task.setup.MakeSetupJarsTask;
import net.ornithemc.keratin.api.task.setup.MakeSetupMappingsTask;
import net.ornithemc.keratin.api.task.setup.MakeSetupSignaturesTask;
import net.ornithemc.keratin.api.task.setup.MakeSourceJarsTask;
import net.ornithemc.keratin.api.task.setup.MakeSourceMappingsTask;
import net.ornithemc.keratin.api.task.setup.MakeSourceTask;
import net.ornithemc.keratin.api.task.setup.MapSetupJarsTask;
import net.ornithemc.keratin.api.task.setup.MapSourceJarsTask;
import net.ornithemc.keratin.api.task.setup.MergeSourceJarsTask;
import net.ornithemc.keratin.api.task.setup.ProcessSourceJarsTask;
import net.ornithemc.keratin.api.task.setup.SetUpSourceTask;
import net.ornithemc.keratin.api.task.unpick.LoadUnpickDefinitionsTask;
import net.ornithemc.keratin.api.task.unpick.MapUnpickDefinitionsToIntermediaryTask;
import net.ornithemc.keratin.api.task.unpick.UnpickMinecraftTask;
import net.ornithemc.keratin.cache.BuildNumbersCache;
import net.ornithemc.keratin.files.IntermediaryDevelopmentFiles;
import net.ornithemc.keratin.files.KeratinFiles;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles.BuildFiles;
import net.ornithemc.keratin.matching.Matches;
import net.ornithemc.keratin.maven.MetaSourcedMultipleBuildsMavenArtifacts;
import net.ornithemc.keratin.maven.MetaSourcedSingleBuildMavenArtifacts;
import net.ornithemc.keratin.util.Versioned;

import net.ornithemc.mappingutils.PropagationDirection;
import net.ornithemc.mappingutils.io.Format;
import net.ornithemc.mappingutils.io.diff.graph.VersionGraph;

public class KeratinGradleExtension implements KeratinGradleExtensionAPI {

	public static KeratinGradleExtension get(Project project) {
		return (KeratinGradleExtension)project.getExtensions().getByName("keratin");
	}

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private final Project project;
	private final PublicationsAPI publications;
	
	private final MetaSourcedSingleBuildMavenArtifacts intermediaryArtifacts;
	private final MetaSourcedMultipleBuildsMavenArtifacts namedMappingsArtifacts;
	private final MetaSourcedMultipleBuildsMavenArtifacts exceptionsArtifacts;
	private final MetaSourcedMultipleBuildsMavenArtifacts signaturesArtifacts;
	private final MetaSourcedMultipleBuildsMavenArtifacts nestsArtifacts;

	private final Property<String> globalCacheDir;
	private final Property<String> localCacheDir;
	private final Property<String> versionsManifestUrl;
	private final ListProperty<String> minecraftVersions;
	private final Property<Integer> intermediaryGen;

	private final Versioned<String, MinecraftVersion> minecraftVersionsById;
	private final Versioned<String, VersionInfo> versionInfos;
	private final Versioned<String, VersionDetails> versionDetails;
	private final Versioned<MinecraftVersion, ProcessorSettings> processorSettings;

	private KeratinFiles files;

	private VersionsManifest versionsManifest;
	private BuildNumbersCache namedMappingsBuilds;
	private BuildNumbersCache exceptionsBuilds;
	private BuildNumbersCache signaturesBuilds;
	private BuildNumbersCache nestsBuilds;

	private boolean configured;
	private boolean cacheInvalid;

	public KeratinGradleExtension(Project project) {
		this.project = project;
		this.publications = this.project.getObjects().newInstance(PublicationsAPI.class);

		this.intermediaryArtifacts = new MetaSourcedSingleBuildMavenArtifacts(this);
		this.intermediaryArtifacts.setMetaUrl(Constants.META_URL);
		this.intermediaryArtifacts.setMetaEndpoint(Constants.INTERMEDIARY_ENDPOINT);
		this.intermediaryArtifacts.setRepositoryUrl(Constants.MAVEN_URL);
		this.intermediaryArtifacts.setClassifier("v2");

		this.namedMappingsArtifacts = new MetaSourcedMultipleBuildsMavenArtifacts(this);
		this.namedMappingsArtifacts.setMetaUrl(Constants.META_URL);
		this.namedMappingsArtifacts.setMetaEndpoint(Constants.FEATHER_ENDPOINT);
		this.namedMappingsArtifacts.setRepositoryUrl(Constants.MAVEN_URL);
		this.namedMappingsArtifacts.setClassifier("mergedv2");

		this.exceptionsArtifacts = new MetaSourcedMultipleBuildsMavenArtifacts(this);
		this.exceptionsArtifacts.setMetaUrl(Constants.META_URL);
		this.exceptionsArtifacts.setMetaEndpoint(Constants.RAVEN_ENDPOINT);
		this.exceptionsArtifacts.setRepositoryUrl(Constants.MAVEN_URL);

		this.signaturesArtifacts = new MetaSourcedMultipleBuildsMavenArtifacts(this);
		this.signaturesArtifacts.setMetaUrl(Constants.META_URL);
		this.signaturesArtifacts.setMetaEndpoint(Constants.SPARROW_ENDPOINT);
		this.signaturesArtifacts.setRepositoryUrl(Constants.MAVEN_URL);

		this.nestsArtifacts = new MetaSourcedMultipleBuildsMavenArtifacts(this);
		this.nestsArtifacts.setMetaUrl(Constants.META_URL);
		this.nestsArtifacts.setMetaEndpoint(Constants.NESTS_ENDPOINT);
		this.nestsArtifacts.setRepositoryUrl(Constants.MAVEN_URL);

		this.globalCacheDir = this.project.getObjects().property(String.class);
		this.globalCacheDir.convention(Constants.ORNITHE_GLOBAL_CACHE_DIR);
		this.globalCacheDir.finalizeValueOnRead();
		this.localCacheDir = this.project.getObjects().property(String.class);
		this.localCacheDir.convention(Constants.ORNITHE_LOCAL_CACHE_DIR);
		this.localCacheDir.finalizeValueOnRead();
		this.versionsManifestUrl = this.project.getObjects().property(String.class);
		this.versionsManifestUrl.convention(Constants.VERSIONS_MANIFEST_URL);
		this.versionsManifestUrl.finalizeValueOnRead();

		this.minecraftVersions = this.project.getObjects().listProperty(String.class);
		this.minecraftVersions.convention(Collections.emptyList());
		this.minecraftVersions.finalizeValueOnRead();
		this.intermediaryGen = this.project.getObjects().property(Integer.class);
		this.intermediaryGen.convention(1);
		this.intermediaryGen.finalizeValueOnRead();

		this.minecraftVersionsById = new Versioned<>(minecraftVersionId -> {
			return MinecraftVersion.parse(this, minecraftVersionId);
		});
		this.versionInfos = new Versioned<>(minecraftVersion -> {
			File file = this.files.getGlobalCache().getMetadataCache().getVersionInfoJson(minecraftVersion);

			if (project.getGradle().getStartParameter().isRefreshDependencies() || !file.exists()) {
				VersionsManifest manifest = versionsManifest;
				VersionsManifest.Entry entry = manifest.findOrThrow(minecraftVersion);

				FileUtils.copyURLToFile(new URI(entry.url()).toURL(), file);
			}

			String json = FileUtils.readFileToString(file, Charset.defaultCharset());
			VersionInfo info = KeratinGradleExtension.GSON.fromJson(json, VersionInfo.class);

			return info;
		});
		this.versionDetails = new Versioned<>(minecraftVersion -> {
			File file = this.files.getGlobalCache().getMetadataCache().getVersionDetailJson(minecraftVersion);

			if (project.getGradle().getStartParameter().isRefreshDependencies() || !file.exists()) {
				VersionsManifest manifest = versionsManifest;
				VersionsManifest.Entry entry = manifest.findOrThrow(minecraftVersion);

				FileUtils.copyURLToFile(new URI(entry.details()).toURL(), file);
			}

			String json = FileUtils.readFileToString(file, Charset.defaultCharset());
			VersionDetails details = KeratinGradleExtension.GSON.fromJson(json, VersionDetails.class);

			return details;
		});
		this.processorSettings = new Versioned<>(minecraftVersion -> {
			return ProcessorSettings.init(ProcessorSettings.PROCESSOR_VERSION)
			                        .withExceptionsBuilds(this.exceptionsBuilds.getBuildNumbers(minecraftVersion))
			                        .withSignaturesBuilds(this.signaturesBuilds.getBuildNumbers(minecraftVersion))
			                        .withNestsBuilds(this.nestsBuilds.getBuildNumbers(minecraftVersion));
		});
	}

	public Project getProject() {
		return project;
	}

	@Override
	public Property<String> getGlobalCacheDirectory() {
		return globalCacheDir;
	}

	@Override
	public Property<String> getLocalCacheDirectory() {
		return localCacheDir;
	}

	@Override
	public Property<String> getVersionsManifestUrl() {
		return versionsManifestUrl;
	}

	@Override
	public void minecraftVersion(String minecraftVersion) {
		minecraftVersions(minecraftVersion);
	}

	@Override
	public void minecraftVersions(String... minecraftVersions) {
		for (String minecraftVersion : minecraftVersions) {
			this.minecraftVersions.add(minecraftVersion);
		}
	}

	@Override
	public Property<Integer> getIntermediaryGen() {
		return intermediaryGen;
	}

	private void findMinecraftVersions(TaskSelection selection, Set<MinecraftVersion> minecraftVersions) throws IOException {
		if (selection == TaskSelection.INTERMEDIARY) {
			File dir = files.getIntermediaryDevelopmentFiles().getMappingsDirectory();

			for (File file : dir.listFiles()) {
				if (!file.isFile() || !file.canRead()) {
					continue;
				}

				String fileName = file.getName();

				if (!fileName.endsWith(".tiny")) {
					continue;
				}

				String version = fileName.substring(0, fileName.length() - ".tiny".length());

				if (intermediaryArtifacts.contains(version)) {
					continue;
				}

				minecraftVersions.add(MinecraftVersion.parse(this, version));
			}
		}
		if (selection == TaskSelection.MAPPINGS) {
			File dir = files.getMappingsDevelopmentFiles().getMappingsDirectory();
			VersionGraph graph = VersionGraph.of(Format.TINY_V2, dir.toPath());

			graph.walk(version -> minecraftVersions.add(MinecraftVersion.parse(this, version.toString())), path -> { });
		}
	}

	@SuppressWarnings("unused")
	private Set<MinecraftVersion> configure(TaskSelection selection) throws Exception {
		if (intermediaryGen.get() < 2) {
			throw new RuntimeException("gen1 is no longer supported!");
		}

		files = new KeratinFiles(this);
		files.mkdirs(selection);

		File manifestFile = files.getGlobalCache().getVersionsManifestJson();

		if (project.getGradle().getStartParameter().isRefreshDependencies() || !manifestFile.exists()) {
			FileUtils.copyURLToFile(new URI(versionsManifestUrl.get()).toURL(), manifestFile);
		}

		String manifestJson = FileUtils.readFileToString(manifestFile, Charset.defaultCharset());

		versionsManifest = GSON.fromJson(manifestJson, VersionsManifest.class);
		namedMappingsBuilds = new BuildNumbersCache(this, files.getSharedFiles().getNamedMappingsBuildsJson(), false);
		exceptionsBuilds = new BuildNumbersCache(this, files.getSharedFiles().getExceptionsBuildsJson(), true);
		signaturesBuilds = new BuildNumbersCache(this, files.getSharedFiles().getSignaturesBuildsJson(), true);
		nestsBuilds = new BuildNumbersCache(this, files.getSharedFiles().getNestsBuildsJson(), true);

		configured = true;

		List<String> selectedMinecraftVersions = minecraftVersions.get();

		Set<MinecraftVersion> minecraftVersions = new LinkedHashSet<>();
		Set<String> minecraftVersionIds = new LinkedHashSet<>();

		if (selectedMinecraftVersions.isEmpty()) {
			findMinecraftVersions(selection, minecraftVersions);
		} else {
			for (String minecraftVersion : selectedMinecraftVersions) {
				minecraftVersions.add(MinecraftVersion.parse(this, minecraftVersion));
			}
		}

		for (MinecraftVersion minecraftVersion : minecraftVersions) {
			if (minecraftVersion.hasClient())
				minecraftVersionIds.add(minecraftVersion.client().id());
			if (minecraftVersion.hasServer())
				minecraftVersionIds.add(minecraftVersion.server().id());
		}

		ConfigurationContainer configurations = project.getConfigurations();
		DependencyHandler dependencies = project.getDependencies();
		DefaultSourceSetContainer sourceSets = (DefaultSourceSetContainer) project.getExtensions().getByName("sourceSets");

		for (String minecraftVersion : minecraftVersionIds) {
			Configuration minecraftLibraries = configurations.register(Configurations.minecraftLibraries(minecraftVersion)).get();
			Configuration javadocClasspath = configurations.register(Configurations.javadocClasspath(minecraftVersion)).get();

			dependencies.add(javadocClasspath.getName(), "net.fabricmc:fabric-loader:0.15.11");
			dependencies.add(javadocClasspath.getName(), "com.google.code.findbugs:jsr305:3.0.2");

			VersionInfo info = getVersionInfo(minecraftVersion);

			for (Library library : info.libraries()) {
				Downloads downloads = library.downloads();
				Artifact artifact = downloads.artifact();

				if (artifact != null) {
					dependencies.add(minecraftLibraries.getName(), library.name());

					if (selection == TaskSelection.EXCEPTIONS_AND_SIGNATURES) {
						dependencies.add(Configurations.IMPLEMENTATION, library.name());
					}
				}
			}
		}

		if (selection == TaskSelection.MAPPINGS) {
			Configuration decompileClasspath = configurations.register(Configurations.DECOMPILE_CLASSPATH).get();
			Configuration enigmaRuntime = configurations.register(Configurations.ENIGMA_RUNTIME).get();

			dependencies.add(decompileClasspath.getName(), "org.vineflower:vineflower:1.11.0");
			dependencies.add(decompileClasspath.getName(), "net.fabricmc:cfr:0.0.9");
			dependencies.add(enigmaRuntime.getName(), "net.ornithemc:enigma-swing:2.5.1");
			dependencies.add(enigmaRuntime.getName(), "org.quiltmc:quilt-enigma-plugin:2.3.1");

			SourceSet constants = sourceSets.register(SourceSets.CONSTANTS).get();
		}
		if (selection == TaskSelection.EXCEPTIONS_AND_SIGNATURES) {
			Configuration decompileClasspath = configurations.register(Configurations.DECOMPILE_CLASSPATH).get();

			dependencies.add(Configurations.IMPLEMENTATION, "net.fabricmc:fabric-loader:0.15.11");
			dependencies.add(decompileClasspath.getName(), "org.vineflower:vineflower:1.11.0");
		}

		publications.getGroupId().convention(project.provider(() -> "net.ornithemc"));

		if (selection == TaskSelection.INTERMEDIARY) {
			publications.getArtifactId().convention(project.provider(() -> "calamus-intermediary-gen%d".formatted(intermediaryGen.get())));
		}
		if (selection == TaskSelection.MAPPINGS) {
			publications.getArtifactId().convention(project.provider(() -> "feather-gen%d".formatted(intermediaryGen.get())));
		}

		return minecraftVersions;
	}

	@SuppressWarnings("unused")
	@Override
	public void tasks(TaskSelection selection) throws Exception {
		Set<MinecraftVersion> minecraftVersions = configure(selection);
		Set<String> minecraftVersionIds = new LinkedHashSet<>();

		for (MinecraftVersion minecraftVersion : minecraftVersions) {
			if (minecraftVersion.hasClient())
				minecraftVersionIds.add(minecraftVersion.client().id());
			if (minecraftVersion.hasServer())
				minecraftVersionIds.add(minecraftVersion.server().id());
		}

		ConfigurationContainer configurations = project.getConfigurations();
		TaskContainer tasks = project.getTasks();

		PublishingExtension publishing = (PublishingExtension) project.getExtensions().getByName("publishing");
		PublicationContainer publications = publishing.getPublications();

		DefaultSourceSetContainer sourceSets = (DefaultSourceSetContainer) project.getExtensions().getByName("sourceSets");

		TaskProvider<?> updateNamedMappingsBuilds = tasks.register("updateNamedMappingsBuildsCache", UpdateBuildsCacheTask.NamedMappings.class);
		TaskProvider<?> updateExceptionsBuilds = tasks.register("updateExceptionsBuildsCache", UpdateBuildsCacheTask.Exceptions.class);
		TaskProvider<?> updateSignaturesBuilds = tasks.register("updateSignaturesBuildsCache", UpdateBuildsCacheTask.Signatures.class);
		TaskProvider<?> updateNestsBuilds = tasks.register("updateNestsBuildsCache", UpdateBuildsCacheTask.Nests.class);

		TaskProvider<?> downloadJars = tasks.register("downloadMinecraftJars", DownloadMinecraftJarsTask.class);
		TaskProvider<?> mergeJars = tasks.register("mergeMinecraftJars", MergeMinecraftJarsTask.class, task -> {
			task.dependsOn(downloadJars);
			task.getNamespace().set(Mapper.OFFICIAL);
		});

		TaskProvider<?> downloadExceptions = tasks.register("downloadExceptions", DownloadExceptionsTask.class);
		TaskProvider<?> downloadSignatures = tasks.register("downloadSignatures", DownloadSignaturesTask.class);
		TaskProvider<?> downloadNests = tasks.register("downloadNests", DownloadNestsTask.class);

		if (selection == TaskSelection.INTERMEDIARY) {
			Action<GenerateIntermediaryTask> configureIntermediaryTask = task -> {
				task.dependsOn(mergeJars, downloadNests);
				task.getTargetNamespace().set(Mapper.INTERMEDIARY);
				task.getTargetPackage().set("net/minecraft/unmapped/");
				task.getNameLength().set(7);

				task.configureMinecraftVersion(minecraftVersion -> {
					// very old versions are only partially obfuscated
					// so we provide a very strict obfuscation pattern
					if (minecraftVersion.hasClient() && minecraftVersion.client().releaseTime().compareTo("2009-05-16T11:48:00+00:00") <= 0) { // 'rubydung'
						task.getObfuscationPatterns().add("^(?:(?!com/mojang/rubydung/RubyDung).)*$");
					} else if (minecraftVersion.hasClient() && minecraftVersion.client().releaseTime().compareTo("2009-12-22T00:00:00+00:00") <= 0) { // 'classic'
						task.getObfuscationPatterns().add("^(?:(?!com/mojang/minecraft/MinecraftApplet).)*$");
					}
				});
			};

			TaskProvider<?> generateIntermediary = tasks.register("generateIntermediary", GenerateNewIntermediaryTask.class, configureIntermediaryTask);
			TaskProvider<?> updateIntermediary = tasks.register("updateIntermediary", UpdateIntermediaryTask.class, configureIntermediaryTask);

			IntermediaryDevelopmentFiles files = this.files.getIntermediaryDevelopmentFiles();

			for (String minecraftVersion : minecraftVersionIds) {
				TaskProvider<?> convertMappings = tasks.register("%s_convertMappingsFromTinyV1ToTinyV2".formatted(minecraftVersion), ConvertMappingsFromTinyV1ToTinyV2Task.class, task -> {
					task.getInput().set(files.getTinyV1MappingsFile(minecraftVersion));
					task.getOutput().set(files.getTinyV2MappingsFile(minecraftVersion));
				});

				TaskProvider<BuildMappingsJarTask> tinyV1Jar = tasks.register("%s_tinyV1Jar".formatted(minecraftVersion), BuildMappingsJarTask.class, task -> {
					task.configure(minecraftVersion, files.getTinyV1MappingsFile(minecraftVersion), "%s-tiny-v1.jar");
				});
				TaskProvider<BuildMappingsJarTask> tinyV2Jar = tasks.register("%s_tinyV2Jar".formatted(minecraftVersion), BuildMappingsJarTask.class, task -> {
					task.dependsOn(convertMappings);
					task.configure(minecraftVersion, files.getTinyV2MappingsFile(minecraftVersion), "%s-tiny-v2.jar");
				});

				tasks.getByName("build").dependsOn(tinyV1Jar, tinyV2Jar);

				if (!intermediaryArtifacts.contains(minecraftVersion)) {
					MavenPublication mavenPublication = publications.create("%s_mavenJava".formatted(minecraftVersion), MavenPublication.class, publication -> {
						publication.setGroupId(this.publications.getGroupId().get());
						publication.setArtifactId(this.publications.getArtifactId().get());
						publication.setVersion(minecraftVersion);

						publication.artifact(tinyV1Jar);
						publication.artifact(tinyV2Jar, config -> {
							config.setClassifier("v2");
						});
					});
				}
			}
		}
		if (selection == TaskSelection.MAPPINGS || selection == TaskSelection.EXCEPTIONS_AND_SIGNATURES) {
			TaskProvider<?> downloadIntermediary = tasks.register("downloadIntermediary", DownloadIntermediaryTask.class, task -> {
				task.dependsOn(mergeJars);
			});
			TaskProvider<?> mergeIntermediary = tasks.register("mergeIntermediary", MergeIntermediaryTask.class, task -> {
				task.dependsOn(downloadIntermediary);
			});
			TaskProvider<?> splitIntermediary = tasks.register("splitIntermediary", SplitIntermediaryTask.class, task -> {
				task.dependsOn(mergeIntermediary);
			});
			TaskProvider<?> fillIntermediary = tasks.register("fillIntermediary", FillIntermediaryTask.class, task -> {
				task.dependsOn(splitIntermediary);
			});
			TaskProvider<?> mapMinecraftToIntermediary = tasks.register("mapMinecraftToIntermediary", MapMinecraftTask.class, task -> {
				task.dependsOn(mergeJars, splitIntermediary);
				task.getSourceNamespace().set(Mapper.OFFICIAL);
				task.getTargetNamespace().set(Mapper.INTERMEDIARY);
			});
			TaskProvider<?> mergeIntermediaryJars = tasks.register("mergeIntermediaryMinecraftJars", MergeMinecraftJarsTask.class, task -> {
				task.dependsOn(mapMinecraftToIntermediary);
				task.getNamespace().set(Mapper.INTERMEDIARY);
			});

			TaskProvider<?> mapNestsToIntermediary = tasks.register("mapNestsToIntermediary", MapNestsTask.class, task -> {
				task.dependsOn(downloadNests, fillIntermediary);
				task.getSourceNamespace().set(Mapper.OFFICIAL);
				task.getTargetNamespace().set(Mapper.INTERMEDIARY);
			});
			TaskProvider<?> mergeIntermediaryNests = tasks.register("mergeIntermediaryNests", MergeNestsTask.class, task -> {
				task.dependsOn(mapNestsToIntermediary);
				task.getNamespace().set(Mapper.INTERMEDIARY);
			});

			if (selection == TaskSelection.MAPPINGS) {
				SourceSet constants = sourceSets.getByName(SourceSets.CONSTANTS);

				TaskProvider<Jar> constantsJar = tasks.register("constantsJar", Jar.class, task -> {
					task.from(constants.getOutput());
					task.getArchiveFileName().set("constants.jar");
					task.getDestinationDirectory().set(project.file("build/libs"));
				});
				TaskProvider<?> sourcesJar = tasks.register("sourcesJar", Jar.class, task -> {
					task.from(constants.getAllSource());
					task.getArchiveFileName().set("sources.jar");
					task.getDestinationDirectory().set(project.file("build/libs"));
				});

				TaskProvider<?> prepareBuild = tasks.register("prepareBuild", PrepareBuildTask.class, task -> {
					task.dependsOn(mergeIntermediaryNests);
				});
				TaskProvider<?> checkMappings = tasks.register("checkMappings", CheckMappingsTask.class, task -> {
					task.dependsOn(mergeIntermediaryJars, prepareBuild);
				});
				TaskProvider<?> completeMappings = tasks.register("completeMappings", CompleteMappingsTask.class, task -> {
					task.dependsOn(mergeIntermediaryJars, prepareBuild);
				});
				TaskProvider<?> buildMappings = tasks.register("buildMappings", BuildMappingsTask.class, task -> {
					task.dependsOn(completeMappings);
					task.getClassNamePattern().set("^(net/minecraft/|com/mojang/).*$");
				});

				TaskProvider<?> mapNestsToNamed = tasks.register("mapNestsToNamed", MapNestsTask.class, task -> {
					task.dependsOn(mergeIntermediaryNests, prepareBuild);
					task.getSourceNamespace().set(Mapper.INTERMEDIARY);
					task.getTargetNamespace().set(Mapper.NAMED);
				});

				TaskProvider<?> loadUnpickDefinitions = tasks.register("loadUnpickDefinitions", LoadUnpickDefinitionsTask.class);
				TaskProvider<?> mapUnpickDefinitionsToIntermediary = tasks.register("mapUnpickDefinitionsToIntermediary", MapUnpickDefinitionsToIntermediaryTask.class, task -> {
					task.dependsOn(prepareBuild, loadUnpickDefinitions);
				});
				TaskProvider<?> buildUnpickDefinitions = tasks.register("buildUnpickDefinitions", BuildUnpickDefinitionsTask.class, task -> {
					task.dependsOn(mapNestsToNamed, loadUnpickDefinitions);
				});

				TaskProvider<?> mapExceptionsToIntermediary = tasks.register("mapExceptionsToIntermediary", MapExceptionsTask.class, task -> {
					task.dependsOn(downloadExceptions, fillIntermediary);
					task.getSourceNamespace().set(Mapper.OFFICIAL);
					task.getTargetNamespace().set(Mapper.INTERMEDIARY);
					
				});
				TaskProvider<?> mergeIntermediaryExceptions = tasks.register("mergeIntermediaryExceptions", MergeExceptionsTask.class, task -> {
					task.dependsOn(mapExceptionsToIntermediary);
					task.getNamespace().set(Mapper.INTERMEDIARY);
				});
				TaskProvider<?> mapSignaturesToIntermediary = tasks.register("mapSignaturesToIntermediary", MapSignaturesTask.class, task -> {
					task.dependsOn(downloadSignatures, fillIntermediary);
					task.getSourceNamespace().set(Mapper.OFFICIAL);
					task.getTargetNamespace().set(Mapper.INTERMEDIARY);
					
				});
				TaskProvider<?> mergeIntermediarySignatures = tasks.register("mergeIntermediarySignatures", MergeSignaturesTask.class, task -> {
					task.dependsOn(mapSignaturesToIntermediary);
					task.getNamespace().set(Mapper.INTERMEDIARY);
				});

				TaskProvider<?> processMinecraft = tasks.register("processMinecraft", ProcessMinecraftTask.class, task -> {
					task.dependsOn(mergeIntermediaryJars, mergeIntermediaryExceptions, mergeIntermediarySignatures, mergeIntermediaryNests);
					task.getForDecompile().set(false);
				});
				TaskProvider<?> unpickMinecraft = tasks.register("unpickMinecraft", UnpickMinecraftTask.class, task -> {
					task.dependsOn(constantsJar, mapUnpickDefinitionsToIntermediary, processMinecraft);
					task.getUnpickConstantsJar().set(constantsJar.get().getArchiveFile().get().getAsFile());
					task.getForDecompile().set(false);
				});

				TaskProvider<?> loadMappings = tasks.register("loadMappings", LoadMappingsFromGraphTask.class, task -> {
					task.dependsOn(processMinecraft);
				});
				TaskProvider<?> saveMappings = tasks.register("saveMappings", SaveMappingsIntoGraphTask.class, task -> {
					task.dependsOn(processMinecraft);
					task.getPropagationDirection().set(PropagationDirection.BOTH);
				});
				TaskProvider<?> saveMappingsUp = tasks.register("saveMappingsUp", SaveMappingsIntoGraphTask.class, task -> {
					task.dependsOn(processMinecraft);
					task.getPropagationDirection().set(PropagationDirection.UP);
				});
				TaskProvider<?> saveMappingsDown = tasks.register("saveMappingsDown", SaveMappingsIntoGraphTask.class, task -> {
					task.dependsOn(processMinecraft);
					task.getPropagationDirection().set(PropagationDirection.DOWN);
				});
				TaskProvider<?> insertMappings = tasks.register("insertMappings", SaveMappingsIntoGraphTask.class, task -> {
					task.dependsOn(processMinecraft);
					task.getPropagationDirection().set(PropagationDirection.NONE);
				});

				TaskProvider<?> launchEnigma = tasks.register("enigma", LaunchEnigmaTask.class, task -> {
					task.dependsOn(loadMappings, unpickMinecraft);
					task.getUnpicked().set(true);
				});
				TaskProvider<?> launchEnigmaWithoutUnpick = tasks.register("enigmaWithoutUnpick", LaunchEnigmaTask.class, task -> {
					task.dependsOn(loadMappings);
					task.getUnpicked().set(false);
				});

				String classNamePattern = "^(net/minecraft/|com/mojang/).*$";

				TaskProvider<?> resetGraph = tasks.register("resetGraph", ResetGraphTask.class, task -> {
					task.dependsOn(processMinecraft);
					task.getClassNamePattern().convention(classNamePattern);
					task.getClassNamePattern().finalizeValueOnRead();
				});
				TaskProvider<?> extendGraph = tasks.register("extendGraph", ExtendGraphTask.class, task -> {
					task.dependsOn(processMinecraft);
					task.getClassNamePattern().convention(classNamePattern);
					task.getClassNamePattern().finalizeValueOnRead();
				});

				TaskProvider<?> mapMinecraftToNamed = tasks.register("mapMinecraftToNamed", MapMinecraftTask.class, task -> {
					task.dependsOn(mergeIntermediaryJars, buildMappings);
					task.getSourceNamespace().set(Mapper.INTERMEDIARY);
					task.getTargetNamespace().set(Mapper.NAMED);
				});

				TaskProvider<?> processMinecraftForDecompile = tasks.register("processMinecraftForDecompile", ProcessMinecraftTask.class, task -> {
					task.dependsOn(mergeIntermediaryJars, mergeIntermediaryExceptions, mergeIntermediarySignatures, mergeIntermediaryNests);
					task.getForDecompile().set(true);
				});
				TaskProvider<?> unpickMinecraftForDecompile = tasks.register("unpickMinecraftForDecompile", UnpickMinecraftTask.class, task -> {
					task.dependsOn(constantsJar, mapUnpickDefinitionsToIntermediary, processMinecraftForDecompile);
					task.getUnpickConstantsJar().set(constantsJar.get().getArchiveFile().get().getAsFile());
					task.getForDecompile().set(true);
				});

				TaskProvider<?> buildProcessedMappings = tasks.register("buildProcessedMappings", BuildProcessedMappingsTask.class);

				TaskProvider<?> mapProcessedMinecraftToNamedForDecompile = tasks.register("mapProcessedMinecraftToNamedForDecompile", MapProcessedMinecraftTask.class, task -> {
					task.dependsOn(unpickMinecraftForDecompile, buildProcessedMappings);
					task.getSourceNamespace().set(Mapper.INTERMEDIARY);
					task.getTargetNamespace().set(Mapper.NAMED);
					task.getForDecompile().set(true);
				});

				TaskProvider<?> decompileWithCfr = tasks.register("decompileWithCfr", DecompileMinecraftWithCfrTask.class, task -> {
					task.dependsOn(mapProcessedMinecraftToNamedForDecompile);
				});
				TaskProvider<?> decompileWithVineflower = tasks.register("decompileWithVineflower", DecompileMinecraftWithVineflowerTask.class, task -> {
					task.dependsOn(mapProcessedMinecraftToNamedForDecompile);
				});

				TaskProvider<?> mapMinecraftForJavadoc = tasks.register("mapMinecraftForJavadoc", MapMinecraftForJavadocTask.class, task -> {
					task.dependsOn(mergeIntermediaryJars, buildMappings);
				});
				TaskProvider<?> genFakeSource = tasks.register("generateFakeSource", GenerateFakeSourceTask.class, task -> {
					task.dependsOn(buildMappings, mapMinecraftForJavadoc);
				});

				BuildFiles files = this.files.getMappingsDevelopmentFiles().getBuildFiles();

				for (String minecraftVersion : minecraftVersionIds) {
					TaskProvider<Javadoc> javadoc = tasks.register("%s_javadoc".formatted(minecraftVersion), Javadoc.class, task -> {
						task.dependsOn(genFakeSource);
						task.setFailOnError(false);
						task.setMaxMemory("2G");
						task.source(project.fileTree(files.getFakeSourceDirectory(minecraftVersion)).plus(constants.getAllJava()));
						task.setClasspath(configurations.getByName(Configurations.javadocClasspath(minecraftVersion)).plus(configurations.getByName(Configurations.minecraftLibraries(minecraftVersion))));
						task.setDestinationDir(files.getJavadocDirectory(minecraftVersion));
					});

					TaskProvider<?> mergedTinyV1Jar = tasks.register("%s_mergedTinyV1Jar".formatted(minecraftVersion), BuildMappingsJarTask.class, task -> {
						task.dependsOn(buildMappings, buildUnpickDefinitions);
						task.configure(minecraftVersion, files.getMergedTinyV1MappingsFile(minecraftVersion), files.getUnpickDefinitionsFile(minecraftVersion), "%s-merged-tiny-v1.jar");
					});
					TaskProvider<?> tinyV2Jar = tasks.register("%s_tinyV2Jar".formatted(minecraftVersion), BuildMappingsJarTask.class, task -> {
						task.dependsOn(buildMappings, buildUnpickDefinitions);
						task.configure(minecraftVersion, files.getTinyV2MappingsFile(minecraftVersion), files.getUnpickDefinitionsFile(minecraftVersion), "%s-tiny-v2.jar");
					});
					TaskProvider<?> mergedTinyV2Jar = tasks.register("%s_mergedTinyV2Jar".formatted(minecraftVersion), BuildMappingsJarTask.class, task -> {
						task.dependsOn(buildMappings, buildUnpickDefinitions);
						task.configure(minecraftVersion, files.getMergedTinyV2MappingsFile(minecraftVersion), files.getUnpickDefinitionsFile(minecraftVersion), "%s-merged-tiny-v2.jar");
					});
					TaskProvider<?> compressTinyV1 = tasks.register("%s_compressTinyV1".formatted(minecraftVersion), CompressMappingsTask.class, task -> {
						task.dependsOn(buildMappings);
						task.getMappings().set(files.getMergedTinyV1MappingsFile(minecraftVersion));
						task.getCompressedMappings().set(files.getCompressedMergedTinyV1MappingsFile(minecraftVersion));
					});
					TaskProvider<?> javadocJar = tasks.register("%s_javadocJar".formatted(minecraftVersion), Jar.class, task -> {
						task.dependsOn(javadoc);
						task.from(javadoc.get().getDestinationDir());
						task.getArchiveFileName().set("%s-javadoc.jar".formatted(minecraftVersion));
						task.getDestinationDirectory().set(project.file("build/libs"));
					});

					tasks.getByName("build").dependsOn(mergedTinyV1Jar, tinyV2Jar, mergedTinyV2Jar, compressTinyV1, javadocJar, constantsJar, sourcesJar);

					int latestBuild = namedMappingsArtifacts.getLatestBuild(minecraftVersion);
					int nextBuild = namedMappingsBuilds.getBuild(minecraftVersion) + 1;

					if (nextBuild > latestBuild) {
						MavenPublication mavenPublication = publications.create("%s_mavenJava".formatted(minecraftVersion), MavenPublication.class, publication -> {
							publication.setGroupId(this.publications.getGroupId().get());
							publication.setArtifactId(this.publications.getArtifactId().get());
							publication.setVersion("%s+build.%d".formatted(minecraftVersion, nextBuild));

							publication.artifact(mergedTinyV1Jar);
							publication.artifact(tinyV2Jar, config -> {
								config.setClassifier("v2");
							});
							publication.artifact(mergedTinyV2Jar, config -> {
								config.setClassifier("mergedv2");
							});
							publication.artifact(compressTinyV1, config -> {
								config.setClassifier("tiny"); // for backwards compat with gen1
							});
							publication.artifact(javadocJar, config -> {
								config.setClassifier("javadoc");
							});
							publication.artifact(constantsJar, config -> {
								config.setClassifier("constants");
							});
							publication.artifact(sourcesJar, config -> {
								config.setClassifier("sources");
							});
						});
					}
				}
			}
			if (selection == TaskSelection.EXCEPTIONS_AND_SIGNATURES) {
				TaskProvider<?> downloadNamedMappings = tasks.register("downloadNamedMappings", DownloadNamedMappingsTask.class);

				TaskProvider<?> makeSetupExceptions = tasks.register("makeSetupExceptions", MakeSetupExceptionsTask.class);
				TaskProvider<?> makeSetupSignatures = tasks.register("makeSetupSignatures", MakeSetupSignaturesTask.class);

				TaskProvider<?> makeSetupMappings = tasks.register("makeSetupMappings", MakeSetupMappingsTask.class, task -> {
					task.dependsOn(downloadNamedMappings, mergeIntermediaryJars, mergeIntermediaryNests);
				});
				TaskProvider<?> makeSetupJars = tasks.register("makeSetupJars", MakeSetupJarsTask.class, task -> {
					task.dependsOn(downloadNests, mergeJars);
				});
				TaskProvider<?> mapSetupJars = tasks.register("mapSetupJars", MapSetupJarsTask.class, task -> {
					task.dependsOn(makeSetupMappings, makeSetupJars);
				});

				TaskProvider<?> makeSourceMappings = tasks.register("makeSourceMappings", MakeSourceMappingsTask.class, task -> {
					task.dependsOn(makeSetupMappings, makeSetupJars);
				});
				TaskProvider<?> makeSourceJars = tasks.register("makeSourceJars", MakeSourceJarsTask.class, task -> {
					task.dependsOn(makeSetupExceptions, makeSetupSignatures, downloadNests, mergeJars);
				});
				TaskProvider<?> mapSourceJars = tasks.register("mapSourceJars", MapSourceJarsTask.class, task -> {
					task.dependsOn(makeSourceMappings, makeSourceJars);
				});
				TaskProvider<?> mergeSourceJars = tasks.register("mergeSourceJars", MergeSourceJarsTask.class, task -> {
					task.dependsOn(mapSourceJars);
				});
				TaskProvider<?> processSourceJars = tasks.register("processSourceJars", ProcessSourceJarsTask.class, task -> {
					task.dependsOn(mergeSourceJars);
				});

				TaskProvider<?> makeSource = tasks.register("makeSource", MakeSourceTask.class, task -> {
					task.dependsOn(processSourceJars);
				});
				TaskProvider<?> setUpSource = tasks.register("setUpSource", SetUpSourceTask.class, task -> {
					task.dependsOn(makeSource);
				});

				TaskProvider<?> makeBaseExceptions = tasks.register("makeBaseExceptions", MakeBaseExceptionsTask.class, task -> {
					task.dependsOn(mergeJars);
				});
				TaskProvider<?> makeBaseSignatures = tasks.register("makeBaseSignatures", MakeBaseSignaturesTask.class, task -> {
					task.dependsOn(mergeJars);
				});

				TaskProvider<?> makeGeneratedJars = tasks.register("makeGeneratedJars", MakeGeneratedJarsTask.class, task -> {
					task.dependsOn("build");
				});
				TaskProvider<?> splitGeneratedJar = tasks.register("splitGeneratedJar", SplitGeneratedJarTask.class, task -> {
					task.dependsOn(makeGeneratedJars);
				});
				TaskProvider<?> mapGeneratedJars = tasks.register("mapGeneratedJars", MapGeneratedJarsTask.class, task -> {
					task.dependsOn(makeSourceMappings, splitGeneratedJar);
				});

				TaskProvider<?> makeGeneratedExceptions = tasks.register("makeGeneratedExceptions", MakeGeneratedExceptionsTask.class, task -> {
					task.dependsOn(mapGeneratedJars);
				});
				TaskProvider<?> makeGeneratedSignatures = tasks.register("makeGeneratedSignatures", MakeGeneratedSignaturesTask.class, task -> {
					task.dependsOn(mapGeneratedJars);
				});

				TaskProvider<?> saveExceptions = tasks.register("saveExceptions", SaveExceptionsTask.class, task -> {
					task.dependsOn(makeBaseExceptions, makeGeneratedExceptions);
				});
				TaskProvider<?> saveSignatures = tasks.register("saveSignatures", SaveSignaturesTask.class, task -> {
					task.dependsOn(makeBaseSignatures, makeGeneratedSignatures);
				});
				TaskProvider<?> save = tasks.register("save", task -> {
					task.dependsOn(saveExceptions, saveSignatures);
				});
			}
		}

		tasks.withType(MinecraftTask.class, task -> {
			task.getMinecraftVersions().set(minecraftVersions);
		});
		tasks.getByName("clean", task -> {
			task.doFirst(t -> {
				project.delete(files.getLocalCache().getDirectory());
			});
			task.doLast(t -> {
				try {
					files.getLocalCache().mkdirs();
				} catch (IOException e) {
					throw new RuntimeException("error creating local cache directory", e);
				}
			});
		});
	}

	public void checkAccess(String obj) {
		if (!configured) {
			throw new IllegalStateException("cannot access " + obj + " before the project has been configured!");
		}
	}

	public MinecraftVersion getMinecraftVersion(String id) {
		checkAccess("minecraft versions");
		return minecraftVersionsById.get(id);
	}

	@Override
	public KeratinFiles getFiles() {
		checkAccess("file management");
		return files;
	}

	@Override
	public PublicationsAPI getPublications() {
		checkAccess("publication management");
		return publications;
	}

	@Override
	public void publications(Action<PublicationsAPI> action) {
		action.execute(publications);
	}

	@Override
	public MetaSourcedSingleBuildMavenArtifacts getIntermediaryArtifacts() {
		checkAccess("intermediary artifacts");
		return intermediaryArtifacts;
	}

	@Override
	public void intermediaryArtifacts(Action<MetaSourcedMavenArtifactsAPI> action) {
		action.execute(intermediaryArtifacts);
	}

	@Override
	public MetaSourcedMultipleBuildsMavenArtifacts getNamedMappingsArtifacts() {
		checkAccess("named mappings artifacts");
		return namedMappingsArtifacts;
	}

	@Override
	public void namedMappingsArtifacts(Action<MetaSourcedMavenArtifactsAPI> action) {
		action.execute(namedMappingsArtifacts);
	}

	@Override
	public MetaSourcedMultipleBuildsMavenArtifacts getExceptionsArtifacts() {
		checkAccess("exceptions artifacts");
		return exceptionsArtifacts;
	}

	@Override
	public void exceptionsArtifacts(Action<MetaSourcedMavenArtifactsAPI> action) {
		action.execute(exceptionsArtifacts);
	}

	@Override
	public MetaSourcedMultipleBuildsMavenArtifacts getSignaturesArtifacts() {
		checkAccess("signatures artifacts");
		return signaturesArtifacts;
	}

	@Override
	public void signaturesArtifacts(Action<MetaSourcedMavenArtifactsAPI> action) {
		action.execute(signaturesArtifacts);
	}

	@Override
	public MetaSourcedMultipleBuildsMavenArtifacts getNestsArtifacts() {
		checkAccess("nests artifacts");
		return nestsArtifacts;
	}

	@Override
	public void nestsArtifacts(Action<MetaSourcedMavenArtifactsAPI> action) {
		action.execute(nestsArtifacts);
	}

	@Override
	public void invalidateCache() {
		if (!cacheInvalid) {
			project.getLogger().lifecycle("The cache was either incomplete or invalid. This build will be slower than usual.");
		}

		cacheInvalid = true;
	}

	public boolean isCacheInvalid() {
		return cacheInvalid;
	}

	public static boolean validateOutput(File output, boolean overwrite) throws IOException {
		return validateOutput(output.toPath(), overwrite);
	}

	public static boolean validateOutput(Path output, boolean overwrite) throws IOException {
		if (overwrite || !Files.exists(output)) {
			if (Files.exists(output)) {
				Files.delete(output);
			}

			return false;
		}

		return true;
	}

	@Override
	public VersionsManifest getVersionsManifest() {
		checkAccess("versions manifest");
		return versionsManifest;
	}

	public VersionInfo getVersionInfo(String minecraftVersion) {
		return versionInfos.get(minecraftVersion);
	}

	public VersionDetails getVersionDetails(String minecraftVersion) {
		return versionDetails.get(minecraftVersion);
	}

	public int getNamedMappingsBuild(String minecraftVersion) {
		return namedMappingsBuilds.getBuild(minecraftVersion);
	}

	public BuildNumbers getNamedMappingsBuilds(MinecraftVersion minecraftVersion) {
		return namedMappingsBuilds.getBuildNumbers(minecraftVersion);
	}

	public BuildNumbers getExceptionsBuilds(MinecraftVersion minecraftVersion) {
		return exceptionsBuilds.getBuildNumbers(minecraftVersion);
	}

	public BuildNumbers getSignaturesBuilds(MinecraftVersion minecraftVersion) {
		return signaturesBuilds.getBuildNumbers(minecraftVersion);
	}

	public BuildNumbers getNestsBuilds(MinecraftVersion minecraftVersion) {
		return nestsBuilds.getBuildNumbers(minecraftVersion);
	}

	public ProcessorSettings getProcessorSettings(MinecraftVersion minecraftVersion) {
		return processorSettings.get(minecraftVersion);
	}

	public ProcessorSettings getProcessorSettingsForDecompile(MinecraftVersion minecraftVersion) {
		return processorSettings.get(minecraftVersion).withObfuscateLocalVariableNames(true);
	}

	public Matches findMatches(String sideA, String versionA, String sideB, String versionB) {
		File dir = files.getSharedFiles().getMatchesDirectory();

		File file;
		boolean inverted;

		file = findMatches(dir, sideA, versionA, sideB, versionB);
		inverted = false;

		if (file == null) {
			file = findMatches(dir, sideB, versionB, sideA, versionA);
			inverted = true;
		}

		if (file == null) {
			throw new RuntimeException("no matches from %s %s to %s %s could be found".formatted(sideA, versionA, sideB, versionB));
		}

		return new Matches(file, inverted);
	}

	private File findMatches(File dir, String sideA, String versionA, String sideB, String versionB) {
		String name;

		if (sideA.equals(sideB)) {
			dir = new File(dir, sideA);
			name = "%s#%s.match".formatted(versionA, versionB);
		} else {
			dir = new File(dir, "cross");
			name = "%s-%s#%s-%s.match".formatted(sideA, versionA, sideB, versionB);
		}

		return findMatches(dir, name);
	}

	private static File findMatches(File dir, String name) {
		if (dir.isDirectory()) {
			File file = new File(dir, name);

			if (file.isFile() && file.exists()) {
				return file;
			}

			for (File f : dir.listFiles()) {
				file = findMatches(f, name);

				if (file != null) {
					return file;
				}
			}
		}

		return null;
	}
}
