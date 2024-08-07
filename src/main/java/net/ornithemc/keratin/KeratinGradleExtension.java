package net.ornithemc.keratin;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.publish.PublicationContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.javadoc.Javadoc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.KeratinGradleExtensionAPI;
import net.ornithemc.keratin.api.TaskSelection;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.manifest.VersionInfo;
import net.ornithemc.keratin.api.manifest.VersionInfo.Library;
import net.ornithemc.keratin.api.manifest.VersionInfo.Library.Downloads;
import net.ornithemc.keratin.api.manifest.VersionInfo.Library.Downloads.Artifact;
import net.ornithemc.keratin.api.manifest.VersionsManifest;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.build.BuildMappingsJarTask;
import net.ornithemc.keratin.api.task.build.BuildMappingsTask;
import net.ornithemc.keratin.api.task.build.BuildProcessedMappingsTask;
import net.ornithemc.keratin.api.task.build.CheckMappingsTask;
import net.ornithemc.keratin.api.task.build.CompleteMappingsTask;
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
import net.ornithemc.keratin.api.task.mapping.ConvertMappingsFromTinyV1ToTinyV2Task;
import net.ornithemc.keratin.api.task.mapping.DownloadIntermediaryTask;
import net.ornithemc.keratin.api.task.mapping.GenerateIntermediaryTask;
import net.ornithemc.keratin.api.task.mapping.GenerateNewIntermediaryTask;
import net.ornithemc.keratin.api.task.mapping.MapRavenTask;
import net.ornithemc.keratin.api.task.mapping.MapMinecraftTask;
import net.ornithemc.keratin.api.task.mapping.MapNestsTask;
import net.ornithemc.keratin.api.task.mapping.MapProcessedMinecraftTask;
import net.ornithemc.keratin.api.task.mapping.MapSparrowTask;
import net.ornithemc.keratin.api.task.mapping.SplitIntermediaryTask;
import net.ornithemc.keratin.api.task.mapping.UpdateIntermediaryTask;
import net.ornithemc.keratin.api.task.mapping.graph.ExtendGraphTask;
import net.ornithemc.keratin.api.task.mapping.graph.LoadMappingsFromGraphTask;
import net.ornithemc.keratin.api.task.mapping.graph.ResetGraphTask;
import net.ornithemc.keratin.api.task.mapping.graph.SaveMappingsIntoGraphTask;
import net.ornithemc.keratin.api.task.merging.MergeRavenTask;
import net.ornithemc.keratin.api.task.merging.MergeMinecraftJarsTask;
import net.ornithemc.keratin.api.task.merging.MergeNestsTask;
import net.ornithemc.keratin.api.task.merging.MergeSparrowTask;
import net.ornithemc.keratin.api.task.minecraft.DownloadMinecraftJarsTask;
import net.ornithemc.keratin.api.task.processing.DownloadRavenTask;
import net.ornithemc.keratin.api.task.processing.DownloadNestsTask;
import net.ornithemc.keratin.api.task.processing.DownloadSparrowTask;
import net.ornithemc.keratin.api.task.processing.ProcessMinecraftTask;
import net.ornithemc.keratin.api.task.processing.UpdateBuildsCacheFromMetaTask;
import net.ornithemc.keratin.api.task.setup.DownloadMappingsTask;
import net.ornithemc.keratin.api.task.setup.MakeSetupExceptionsTask;
import net.ornithemc.keratin.api.task.setup.MakeSetupJarsTask;
import net.ornithemc.keratin.api.task.setup.MakeSetupMappingsTask;
import net.ornithemc.keratin.api.task.setup.MakeSetupSignaturesTask;
import net.ornithemc.keratin.api.task.setup.MakeSourceJarsTask;
import net.ornithemc.keratin.api.task.setup.MakeSourceTask;
import net.ornithemc.keratin.api.task.setup.MakeSourceMappingsTask;
import net.ornithemc.keratin.api.task.setup.MapSetupJarsTask;
import net.ornithemc.keratin.api.task.setup.MapSourceJarsTask;
import net.ornithemc.keratin.api.task.setup.MergeSourceJarsTask;
import net.ornithemc.keratin.api.task.setup.SetUpSourceTask;
import net.ornithemc.keratin.matching.Matches;
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
	private final OrnitheFiles files;
	private final CalamusVersions calamusVersions;
	private final FeatherVersions featherVersions;

	private final Property<String> globalCacheDir;
	private final Property<String> localCacheDir;
	private final ListProperty<String> minecraftVersions;
	private final Property<Integer> intermediaryGen;

	private final Property<VersionsManifest> versionsManifest;
	private final Versioned<VersionInfo> versionInfos;
	private final Versioned<VersionDetails> versionDetails;
	private final Versioned<Map<GameSide, Integer>> ravenBuilds;
	private final Versioned<Map<GameSide, Integer>> sparrowBuilds;
	private final Versioned<Map<GameSide, Integer>> nestsBuilds;

	public KeratinGradleExtension(Project project) {
		this.project = project;
		this.files = new OrnitheFiles(this);
		this.calamusVersions = new CalamusVersions(this);
		this.featherVersions = new FeatherVersions(this);

		this.globalCacheDir = this.project.getObjects().property(String.class);
		this.globalCacheDir.convention(Constants.ORNITHE_GLOBAL_CACHE_DIR);
		this.globalCacheDir.finalizeValueOnRead();
		this.localCacheDir = this.project.getObjects().property(String.class);
		this.localCacheDir.convention(Constants.ORNITHE_LOCAL_CACHE_DIR);
		this.localCacheDir.finalizeValueOnRead();

		this.minecraftVersions = this.project.getObjects().listProperty(String.class);
		this.minecraftVersions.convention(this.project.provider(() -> {
			return Collections.emptyList();
		}));
		this.minecraftVersions.finalizeValueOnRead();
		this.intermediaryGen = this.project.getObjects().property(Integer.class);
		this.intermediaryGen.convention(1);
		this.intermediaryGen.finalizeValueOnRead();

		this.versionsManifest = this.project.getObjects().property(VersionsManifest.class);
		this.versionsManifest.convention(this.project.provider(() -> {
			File file = KeratinGradleExtension.this.files.getVersionsManifest();

			if (!file.exists()) {
				throw new RuntimeException("versions manifest file does not exist!");
			}

			String json = FileUtils.readFileToString(file, Charset.defaultCharset());
			VersionsManifest manifest = KeratinGradleExtension.GSON.fromJson(json, VersionsManifest.class);

			return manifest;
		}));
		this.versionsManifest.finalizeValueOnRead();
		this.versionInfos = new Versioned<>(minecraftVersion -> {
			File file = this.files.getVersionInfo(minecraftVersion);

			if (!file.exists()) {
				throw new RuntimeException("no version info file for Minecraft version " + minecraftVersion + " exists!");
			}

			String json = FileUtils.readFileToString(file, Charset.defaultCharset());
			VersionInfo info = KeratinGradleExtension.GSON.fromJson(json, VersionInfo.class);

			return info;
		});
		this.versionDetails = new Versioned<>(minecraftVersion -> {
			File file = this.files.getVersionDetails(minecraftVersion);

			if (!file.exists()) {
				throw new RuntimeException("no version details file for Minecraft version " + minecraftVersion + " exists!");
			}

			String json = FileUtils.readFileToString(file, Charset.defaultCharset());
			VersionDetails details = KeratinGradleExtension.GSON.fromJson(json, VersionDetails.class);

			return details;
		});
		this.ravenBuilds = new Versioned<>(minecraftVersion -> parseBuildsFromCache(minecraftVersion, this.files.getRavenBuildsCache()));
		this.sparrowBuilds = new Versioned<>(minecraftVersion -> parseBuildsFromCache(minecraftVersion, this.files.getSparrowBuildsCache()));
		this.nestsBuilds = new Versioned<>(minecraftVersion -> parseBuildsFromCache(minecraftVersion, this.files.getNestsBuildsCache()));
	}

	private static Map<GameSide, Integer> parseBuildsFromCache(String minecraftVersion, File cacheFile) throws IOException {
		if (!cacheFile.exists()) {
			return Collections.emptyMap();
		}

		Map<GameSide, Integer> builds = new EnumMap<>(GameSide.class);

		String s = FileUtils.readFileToString(cacheFile, Charset.defaultCharset());			
		JsonObject json = GSON.fromJson(s, JsonObject.class);

		for (GameSide side : GameSide.values()) {
			JsonElement buildJson = json.get(minecraftVersion + side.suffix());

			if (buildJson != null && buildJson.isJsonPrimitive()) {
				builds.put(side, buildJson.getAsInt());
			}
		}

		return builds;
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
	public ListProperty<String> getMinecraftVersions() {
		return minecraftVersions;
	}

	@Override
	public void minecraftVersions(String... minecraftVersions) {
		this.minecraftVersions.set(Arrays.asList(minecraftVersions));
	}

	@Override
	public Property<Integer> getIntermediaryGen() {
		return intermediaryGen;
	}

	private void findMinecraftVersions(TaskSelection selection, Consumer<String> action) throws IOException {
		if (selection == TaskSelection.CALAMUS) {
			File dir = files.getMappingsDirectory();

			for (File file : dir.listFiles()) {
				if (!file.isFile() || !file.canRead()) {
					continue;
				}

				String fileName = file.getName();

				if (!fileName.endsWith(".tiny")) {
					continue;
				}

				action.accept(fileName.substring(0, fileName.length() - ".tiny".length()));
			}
		}
		if (selection == TaskSelection.FEATHER) {
			File dir = files.getMappingsDirectory();
			VersionGraph graph = VersionGraph.of(Format.TINY_V2, dir.toPath());

			graph.walk(version -> action.accept(version.toString()), path -> { });
		}
	}

	@SuppressWarnings("unused")
	private Set<String> configure(TaskSelection selection) throws Exception {
		Set<String> minecraftVersions = new LinkedHashSet<>();

		if (!this.minecraftVersions.get().isEmpty()) {
			minecraftVersions.addAll(this.minecraftVersions.get());
		} else {
			findMinecraftVersions(selection, minecraftVersions::add);

			if (selection == TaskSelection.CALAMUS) {
				minecraftVersions.removeIf(calamusVersions::contains);
			}
		}

		boolean refreshDeps = project.getGradle().getStartParameter().isRefreshDependencies();

		for (File cacheDir : files.getCacheDirectories()) {
			if (!cacheDir.exists()) {
				cacheDir.mkdirs();
			}
		}

		File versionsManifest = files.getVersionsManifest();

		if (refreshDeps || !versionsManifest.exists()) {
			FileUtils.copyURLToFile(new URL(Constants.VERSIONS_MANIFEST_URL), versionsManifest);
		}

		for (String minecraftVersion : minecraftVersions) {
			File versionInfo = files.getVersionInfo(minecraftVersion);

			if (refreshDeps || !versionInfo.exists()) {
				VersionsManifest manifest = getVersionsManifest();
				VersionsManifest.Entry entry = manifest.findOrThrow(minecraftVersion);

				FileUtils.copyURLToFile(new URL(entry.url()), versionInfo);
			}

			File versionDetails = files.getVersionDetails(minecraftVersion);

			if (refreshDeps || !versionDetails.exists()) {
				VersionsManifest manifest = getVersionsManifest();
				VersionsManifest.Entry entry = manifest.findOrThrow(minecraftVersion);

				FileUtils.copyURLToFile(new URL(entry.details()), versionDetails);
			}
		}

		ConfigurationContainer configurations = project.getConfigurations();
		DependencyHandler dependencies = project.getDependencies();

		for (String minecraftVersion : minecraftVersions) {
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

					if (selection == TaskSelection.SPARROW_AND_RAVEN) {
						dependencies.add(Configurations.IMPLEMENTATION, library.name());
					}
				}
			}
		}

		if (selection == TaskSelection.FEATHER) {
			Configuration decompileClasspath = configurations.register(Configurations.DECOMPILE_CLASSPATH).get();
			Configuration enigmaRuntime = configurations.register(Configurations.ENIGMA_RUNTIME).get();
			Configuration mappingPoetJar = configurations.register(Configurations.MAPPING_POET_JAR, configuration -> {
				configuration.setTransitive(false);
			}).get();
			Configuration mappingPoet = configurations.register(Configurations.MAPPING_POET, configuration -> {
				configuration.extendsFrom(mappingPoetJar);
				configuration.setTransitive(true);
			}).get();

			dependencies.add(decompileClasspath.getName(), "org.vineflower:vineflower:1.10.1");
			dependencies.add(decompileClasspath.getName(), "net.fabricmc:cfr:0.0.9");
			dependencies.add(enigmaRuntime.getName(), "net.ornithemc:enigma-swing:1.9.0");
			dependencies.add(enigmaRuntime.getName(), "org.quiltmc:quilt-enigma-plugin:1.3.0");
			dependencies.add(mappingPoetJar.getName(), "net.fabricmc:mappingpoet:0.3.0");
			
		}
		if (selection == TaskSelection.SPARROW_AND_RAVEN) {
			Configuration decompileClasspath = configurations.register(Configurations.DECOMPILE_CLASSPATH).get();

			dependencies.add(Configurations.IMPLEMENTATION, "net.fabricmc:fabric-loader:0.15.11");
			dependencies.add(decompileClasspath.getName(), "org.vineflower:vineflower:1.10.1");
		}

		return minecraftVersions;
	}

	@SuppressWarnings("unused")
	@Override
	public void tasks(TaskSelection selection) throws Exception {
		Set<String> minecraftVersions = configure(selection);

		ConfigurationContainer configurations = project.getConfigurations();
		TaskContainer tasks = project.getTasks();

		PublishingExtension publishing = (PublishingExtension) project.getExtensions().getByName("publishing");
		PublicationContainer publications = publishing.getPublications();

		TaskProvider<?> updateRavenBuilds = tasks.register("updateRavenBuildsCache", UpdateBuildsCacheFromMetaTask.class, task -> {
			task.getMetaUrl().convention(Constants.META_URL);
			task.getMetaUrl().finalizeValueOnRead();
			task.getMetaEndpoint().convention(Constants.RAVEN_ENDPOINT);
			task.getMetaEndpoint().finalizeValueOnRead();
			task.getCacheFile().convention(project.provider(() -> files.getRavenBuildsCache()));
			task.getCacheFile().finalizeValueOnRead();
		});
		TaskProvider<?> updateSparrowBuilds = tasks.register("updateSparrowBuildsCache", UpdateBuildsCacheFromMetaTask.class, task -> {
			task.getMetaUrl().convention(Constants.META_URL);
			task.getMetaUrl().finalizeValueOnRead();
			task.getMetaEndpoint().convention(Constants.SPARROW_ENDPOINT);
			task.getMetaEndpoint().finalizeValueOnRead();
			task.getCacheFile().convention(project.provider(() -> files.getSparrowBuildsCache()));
			task.getCacheFile().finalizeValueOnRead();
		});
		TaskProvider<?> updateNestsBuilds = tasks.register("updateNestsBuildsCache", UpdateBuildsCacheFromMetaTask.class, task -> {
			task.getMetaUrl().convention(Constants.META_URL);
			task.getMetaUrl().finalizeValueOnRead();
			task.getMetaEndpoint().convention(Constants.NESTS_ENDPOINT);
			task.getMetaEndpoint().finalizeValueOnRead();
			task.getCacheFile().convention(project.provider(() -> files.getNestsBuildsCache()));
			task.getCacheFile().finalizeValueOnRead();
		});

		TaskProvider<Sync> syncLibraries = tasks.register("syncMinecraftLibraries", Sync.class, task -> {
			for (String minecraftVersion : minecraftVersions) {
				task.from(configurations.getByName(Configurations.minecraftLibraries(minecraftVersion)));
			}
			task.into(files.getLibrariesCache());
		});
		TaskProvider<?> downloadJars = tasks.register("downloadMinecraftJars", DownloadMinecraftJarsTask.class);
		TaskProvider<?> mergeJars = tasks.register("mergeMinecraftJars", MergeMinecraftJarsTask.class, task -> {
			task.dependsOn(downloadJars);
			task.getNamespace().set("official");
		});

		TaskProvider<?> downloadRaven = tasks.register("downloadRaven", DownloadRavenTask.class);
		TaskProvider<?> downloadSparrow = tasks.register("downloadSparrow", DownloadSparrowTask.class);
		TaskProvider<?> downloadNests = tasks.register("downloadNests", DownloadNestsTask.class);

		if (selection == TaskSelection.CALAMUS) {
			Action<GenerateIntermediaryTask> configureIntermediaryTask = task -> {
				task.dependsOn(mergeJars, downloadNests);
				task.getTargetNamespace().set("intermediary");
				task.getTargetPackage().set("net/minecraft/unmapped/");
				task.getNameLength().set(7);

				task.configureMinecraftVersion(minecraftVersion -> {
					VersionDetails details = getVersionDetails(minecraftVersion);

					// very old versions are only partially obfuscated
					// so we provide a very strict obfuscation pattern
					if (details.releaseTime().compareTo("2009-05-17T00:00:00+00:00") < 0) { // 'rubydung'
						task.getObfuscationPatterns().add("^(?:(?!com/mojang/rubydung/RubyDung).)*$");
					} else if (details.releaseTime().compareTo("2009-12-24T00:00:00+00:00") < 0) { // 'classic'
						task.getObfuscationPatterns().add("^(?:(?!com/mojang/minecraft/MinecraftApplet).)*$");
					}
				});
			};

			TaskProvider<?> generateIntermediary = tasks.register("generateIntermediary", GenerateNewIntermediaryTask.class, configureIntermediaryTask);
			TaskProvider<?> updateIntermediary = tasks.register("updateIntermediary", UpdateIntermediaryTask.class, configureIntermediaryTask);

			File mappingsDir = files.getMappingsDirectory();
			File buildDir = files.getLocalBuildCache();

			for (String minecraftVersion : minecraftVersions) {
				File tinyV1File = new File(mappingsDir, "%s.tiny".formatted(minecraftVersion));
				File tinyV2File = new File(buildDir, "%s.tiny".formatted(minecraftVersion));

				TaskProvider<?> convertMappings = tasks.register("%s_convertMappingsFromTinyV1ToTinyV2".formatted(minecraftVersion), ConvertMappingsFromTinyV1ToTinyV2Task.class, task -> {
					task.getInput().set(tinyV1File);
					task.getOutput().set(tinyV2File);
				});

				TaskProvider<BuildMappingsJarTask> tinyV1Jar = tasks.register("%s_tinyV1Jar".formatted(minecraftVersion), BuildMappingsJarTask.class, task -> {
					task.configure(minecraftVersion, tinyV1File, "%s-tiny-v1.jar");
				});
				TaskProvider<BuildMappingsJarTask> tinyV2Jar = tasks.register("%s_tinyV2Jar".formatted(minecraftVersion), BuildMappingsJarTask.class, task -> {
					task.dependsOn(convertMappings);
					task.configure(minecraftVersion, tinyV2File, "%s-tiny-v2.jar");
				});

				tasks.getByName("build").dependsOn(tinyV1Jar, tinyV2Jar);

				MavenPublication mavenPublication = publications.create("%s_mavenJava".formatted(minecraftVersion), MavenPublication.class, publication -> {
					publication.setGroupId("net.ornithemc");
					publication.setArtifactId("calamus-intermediary-gen%d".formatted(intermediaryGen.get()));
					publication.setVersion(minecraftVersion);

					publication.artifact(tinyV1Jar);
					publication.artifact(tinyV2Jar, config -> {
						config.setClassifier("v2");
					});
				});
			}
		}
		if (selection == TaskSelection.FEATHER || selection == TaskSelection.SPARROW_AND_RAVEN) {
			TaskProvider<?> downloadIntermediary = tasks.register("downloadIntermediary", DownloadIntermediaryTask.class, task -> {
				task.dependsOn(mergeJars);
			});
			TaskProvider<?> splitIntermediary = tasks.register("splitIntermediary", SplitIntermediaryTask.class, task -> {
				task.dependsOn(downloadIntermediary);
			});
			TaskProvider<?> mapMinecraftToIntermediary = tasks.register("mapMinecraftToIntermediary", MapMinecraftTask.class, task -> {
				task.dependsOn(syncLibraries, splitIntermediary);
				task.getSourceNamespace().set("official");
				task.getTargetNamespace().set("intermediary");
			});
			TaskProvider<?> mergeIntermediaryJars = tasks.register("mergeIntermediaryMinecraftJars", MergeMinecraftJarsTask.class, task -> {
				task.dependsOn(mapMinecraftToIntermediary);
				task.getNamespace().set("intermediary");
			});

			TaskProvider<?> mapNestsToIntermediary = tasks.register("mapNestsToIntermediary", MapNestsTask.class, task -> {
				task.dependsOn(downloadNests, splitIntermediary);
				task.getSourceNamespace().set("official");
				task.getTargetNamespace().set("intermediary");
				
			});
			TaskProvider<?> mergeIntermediaryNests = tasks.register("mergeIntermediaryNests", MergeNestsTask.class, task -> {
				task.dependsOn(mapNestsToIntermediary);
				task.getNamespace().set("intermediary");
			});

			if (selection == TaskSelection.FEATHER) {
				TaskProvider<?> mapRavenToIntermediary = tasks.register("mapRavenToIntermediary", MapRavenTask.class, task -> {
					task.dependsOn(downloadRaven, splitIntermediary);
					task.getSourceNamespace().set("official");
					task.getTargetNamespace().set("intermediary");
					
				});
				TaskProvider<?> mergeIntermediaryRaven = tasks.register("mergeIntermediaryRaven", MergeRavenTask.class, task -> {
					task.dependsOn(mapRavenToIntermediary);
					task.getNamespace().set("intermediary");
				});
				TaskProvider<?> mapSparrowToIntermediary = tasks.register("mapSparrowToIntermediary", MapSparrowTask.class, task -> {
					task.dependsOn(downloadSparrow, splitIntermediary);
					task.getSourceNamespace().set("official");
					task.getTargetNamespace().set("intermediary");
					
				});
				TaskProvider<?> mergeIntermediarySparrow = tasks.register("mergeIntermediarySparrow", MergeSparrowTask.class, task -> {
					task.dependsOn(mapSparrowToIntermediary);
					task.getNamespace().set("intermediary");
				});

				TaskProvider<?> processMinecraft = tasks.register("processMinecraft", ProcessMinecraftTask.class, task -> {
					task.dependsOn(mergeIntermediaryJars, mergeIntermediaryRaven, mergeIntermediarySparrow, mergeIntermediaryNests);
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
					task.dependsOn(loadMappings);
				});

				if (minecraftVersions.size() == 1) {
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
				}

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

				TaskProvider<?> mapMinecraftToNamed = tasks.register("mapMinecraftToNamed", MapMinecraftTask.class, task -> {
					task.dependsOn(mergeIntermediaryJars, buildMappings);
					task.getSourceNamespace().set("intermediary");
					task.getTargetNamespace().set("named");
				});

				TaskProvider<?> buildProcessedMappings = tasks.register("buildProcessedMappings", BuildProcessedMappingsTask.class, task -> {
					task.dependsOn(processMinecraft);
				});

				TaskProvider<?> mapProcessedMinecraftToNamed = tasks.register("mapProcessedMinecraftToNamed", MapProcessedMinecraftTask.class, task -> {
					task.dependsOn(buildProcessedMappings);
					task.getSourceNamespace().set("intermediary");
					task.getTargetNamespace().set("named");
				});

				TaskProvider<?> decompileWithCfr = tasks.register("decompileWithCfr", DecompileMinecraftWithCfrTask.class, task -> {
					task.dependsOn(mapProcessedMinecraftToNamed);
				});
				TaskProvider<?> decompileWithVineflower = tasks.register("decompileWithVineflower", DecompileMinecraftWithVineflowerTask.class, task -> {
					task.dependsOn(mapProcessedMinecraftToNamed);
				});

				TaskProvider<?> genFakeSource = tasks.register("generakeFakeSource", GenerateFakeSourceTask.class, task -> {
					task.dependsOn(buildMappings, mapMinecraftToNamed);
				});

				for (String minecraftVersion : minecraftVersions) {
					TaskProvider<Javadoc> javadoc = tasks.register("%s_javadoc".formatted(minecraftVersion), Javadoc.class, task -> {
						task.dependsOn(genFakeSource);
						task.setFailOnError(false);
						task.setMaxMemory("2G");
						task.source(files.getFakeSourceDirectory(minecraftVersion));
						task.setClasspath(configurations.getByName(Configurations.javadocClasspath(minecraftVersion)).plus(configurations.getByName(Configurations.minecraftLibraries(minecraftVersion))));
						task.setDestinationDir(files.getJavadocDirectory(minecraftVersion));
					});

					TaskProvider<?> mergedTinyV1Jar = tasks.register("%s_mergedTinyV1Jar".formatted(minecraftVersion), BuildMappingsJarTask.class, task -> {
						task.dependsOn(buildMappings);
						task.configure(minecraftVersion, files.getMergedTinyV1NamedMappings(minecraftVersion), "%s-merged-tiny-v1.jar");
					});
					TaskProvider<?> tinyV2Jar = tasks.register("%s_tinyV2Jar".formatted(minecraftVersion), BuildMappingsJarTask.class, task -> {
						task.dependsOn(buildMappings);
						task.configure(minecraftVersion, files.getTinyV2NamedMappings(minecraftVersion), "%s-tiny-v2.jar");
					});
					TaskProvider<?> mergedTinyV2Jar = tasks.register("%s_mergedTinyV2Jar".formatted(minecraftVersion), BuildMappingsJarTask.class, task -> {
						task.dependsOn(buildMappings);
						task.configure(minecraftVersion, files.getMergedTinyV2NamedMappings(minecraftVersion), "%s-merged-tiny-v2.jar");
					});
					TaskProvider<?> javadocJar = tasks.register("%s_javadocJar".formatted(minecraftVersion), Jar.class, task -> {
						task.dependsOn(javadoc);
						task.from(javadoc.get().getDestinationDir());
						task.getArchiveFileName().set("%s-javadoc.jar".formatted(minecraftVersion));
						task.getDestinationDirectory().set(project.file("build/libs"));
					});

					tasks.getByName("build").dependsOn(mergedTinyV1Jar, tinyV2Jar, mergedTinyV2Jar, javadocJar);

					MavenPublication mavenPublication = publications.create("%s_mavenJava".formatted(minecraftVersion), MavenPublication.class, publication -> {
						publication.setGroupId("net.ornithemc");
						publication.setArtifactId("feather-gen%d".formatted(intermediaryGen.get()));
						publication.setVersion("%s+build.%d".formatted(minecraftVersion, getNextFeatherBuild(minecraftVersion)));

						publication.artifact(mergedTinyV1Jar);
						publication.artifact(tinyV2Jar, config -> {
							config.setClassifier("v2");
						});
						publication.artifact(mergedTinyV2Jar, config -> {
							config.setClassifier("mergedv2");
						});
						publication.artifact(javadocJar, config -> {
							config.setClassifier("javadoc");
						});
					});
				}
			}
			if (selection == TaskSelection.SPARROW_AND_RAVEN) {
				TaskProvider<?> downloadMappings = tasks.register("downloadMappings", DownloadMappingsTask.class);

				TaskProvider<?> makeSetupExceptions = tasks.register("makeSetupExceptions", MakeSetupExceptionsTask.class);
				TaskProvider<?> makeSetupSignatures = tasks.register("makeSetupSignatures", MakeSetupSignaturesTask.class);

				TaskProvider<?> makeSetupMappings = tasks.register("makeSetupMappings", MakeSetupMappingsTask.class, task -> {
					task.dependsOn(downloadMappings, mergeIntermediaryJars, mergeIntermediaryNests);
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

				TaskProvider<?> makeSource = tasks.register("makeSource", MakeSourceTask.class, task -> {
					task.dependsOn(mergeSourceJars);
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
				project.delete(files.getLocalBuildCache());
			});
		});
	}

	@Override
	public OrnitheFiles getFiles() {
		return files;
	}

	@Override
	public VersionsManifest getVersionsManifest() {
		return versionsManifest.get();
	}

	@Override
	public VersionInfo getVersionInfo(String minecraftVersion) {
		return versionInfos.get(minecraftVersion);
	}

	@Override
	public VersionDetails getVersionDetails(String minecraftVersion) {
		return versionDetails.get(minecraftVersion);
	}

	@Override
	public int getRavenBuild(String minecraftVersion, GameSide side) {
		return ravenBuilds.get(minecraftVersion).getOrDefault(side, -1);
	}

	@Override
	public int getSparrowBuild(String minecraftVersion, GameSide side) {
		return sparrowBuilds.get(minecraftVersion).getOrDefault(side, -1);
	}

	@Override
	public int getNestsBuild(String minecraftVersion, GameSide side) {
		return nestsBuilds.get(minecraftVersion).getOrDefault(side, -1);
	}

	@Override
	public int getNextFeatherBuild(String minecraftVersion) {
		return featherVersions.getNext(minecraftVersion);
	}

	public Matches findMatches(String sideA, String versionA, String sideB, String versionB) {
		File dir = files.getMatchesDirectory();

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
