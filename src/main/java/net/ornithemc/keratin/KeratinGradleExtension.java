package net.ornithemc.keratin;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

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
import net.ornithemc.keratin.api.task.build.BuildMappingsTask;
import net.ornithemc.keratin.api.task.build.BuildProcessedMappingsTask;
import net.ornithemc.keratin.api.task.build.CheckMappingsTask;
import net.ornithemc.keratin.api.task.build.CompleteMappingsTask;
import net.ornithemc.keratin.api.task.build.PrepareBuildTask;
import net.ornithemc.keratin.api.task.decompiling.DecompileMinecraftWithCfrTask;
import net.ornithemc.keratin.api.task.decompiling.DecompileMinecraftWithVineflowerTask;
import net.ornithemc.keratin.api.task.enigma.LaunchEnigmaTask;
import net.ornithemc.keratin.api.task.mapping.DownloadIntermediaryTask;
import net.ornithemc.keratin.api.task.mapping.GenerateIntermediaryTask;
import net.ornithemc.keratin.api.task.mapping.GenerateNewIntermediaryTask;
import net.ornithemc.keratin.api.task.mapping.MapMinecraftTask;
import net.ornithemc.keratin.api.task.mapping.MapNestsTask;
import net.ornithemc.keratin.api.task.mapping.MapProcessedMinecraftTask;
import net.ornithemc.keratin.api.task.mapping.MapSparrowTask;
import net.ornithemc.keratin.api.task.mapping.UpdateIntermediaryTask;
import net.ornithemc.keratin.api.task.mapping.graph.ExtendGraphTask;
import net.ornithemc.keratin.api.task.mapping.graph.LoadMappingsFromGraphTask;
import net.ornithemc.keratin.api.task.mapping.graph.ResetGraphTask;
import net.ornithemc.keratin.api.task.mapping.graph.SaveMappingsIntoGraphTask;
import net.ornithemc.keratin.api.task.merging.MergeMinecraftJarsTask;
import net.ornithemc.keratin.api.task.merging.MergeNestsTask;
import net.ornithemc.keratin.api.task.merging.MergeSparrowTask;
import net.ornithemc.keratin.api.task.minecraft.DownloadMinecraftJarsTask;
import net.ornithemc.keratin.api.task.processing.DownloadNestsTask;
import net.ornithemc.keratin.api.task.processing.DownloadSparrowTask;
import net.ornithemc.keratin.api.task.processing.ProcessMappingsTask;
import net.ornithemc.keratin.api.task.processing.ProcessMinecraftTask;
import net.ornithemc.keratin.api.task.processing.UpdateBuildsCacheFromMetaTask;
import net.ornithemc.keratin.matching.Matches;
import net.ornithemc.keratin.util.Versioned;
import net.ornithemc.mappingutils.PropagationDirection;

public class KeratinGradleExtension implements KeratinGradleExtensionAPI {

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static KeratinGradleExtension get(Project project) {
		return (KeratinGradleExtension)project.getExtensions().getByName("keratin");
	}

	private final Project project;
	private final OrnitheFiles files;

	private final Property<String> globalCacheDir;
	private final Property<String> localCacheDir;
	private final Property<String> minecraftVersion;
	private final Property<Integer> intermediaryGen;

	private final Property<VersionsManifest> versionsManifest;
	private final Versioned<VersionInfo> versionInfos;
	private final Versioned<VersionDetails> versionDetails;
	private final Versioned<Map<GameSide, Integer>> nestsBuilds;
	private final Versioned<Map<GameSide, Integer>> sparrowBuilds;
	private final Property<File> matchesDir;

	public KeratinGradleExtension(Project project) {
		this.project = project;
		this.files = new OrnitheFiles(this);

		this.globalCacheDir = this.project.getObjects().property(String.class);
		this.globalCacheDir.convention(Constants.ORNITHE_GLOBAL_CACHE_DIR);
		this.globalCacheDir.finalizeValueOnRead();
		this.localCacheDir = this.project.getObjects().property(String.class);
		this.localCacheDir.convention(Constants.ORNITHE_LOCAL_CACHE_DIR);
		this.localCacheDir.finalizeValueOnRead();

		this.minecraftVersion = this.project.getObjects().property(String.class);
		this.minecraftVersion.convention(this.project.provider(() -> {
			throw new IllegalStateException("main Minecraft version is not set!");
		}));
		this.minecraftVersion.finalizeValueOnRead();
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
		this.nestsBuilds = new Versioned<>(minecraftVersion -> {
			File file = this.files.getNestsBuildsCache();

			if (!file.exists()) {
				return Collections.emptyMap();
			}

			Map<GameSide, Integer> builds = new EnumMap<>(GameSide.class);

			String s = FileUtils.readFileToString(file, Charset.defaultCharset());			
			JsonObject json = GSON.fromJson(s, JsonObject.class);

			for (GameSide side : GameSide.values()) {
				JsonElement buildJson = json.get(minecraftVersion + side.suffix());

				if (buildJson != null && buildJson.isJsonPrimitive()) {
					builds.put(side, buildJson.getAsInt());
				}
			}

			return builds;
		});
		this.sparrowBuilds = new Versioned<>(minecraftVersion -> {
			File file = this.files.getSparrowBuildsCache();

			if (!file.exists()) {
				return Collections.emptyMap();
			}

			Map<GameSide, Integer> builds = new EnumMap<>(GameSide.class);

			String s = FileUtils.readFileToString(file, Charset.defaultCharset());			
			JsonObject json = GSON.fromJson(s, JsonObject.class);

			for (GameSide side : GameSide.values()) {
				JsonElement buildJson = json.get(minecraftVersion + side.suffix());

				if (buildJson != null && buildJson.isJsonPrimitive()) {
					builds.put(side, buildJson.getAsInt());
				}
			}

			return builds;
		});
		this.matchesDir = this.project.getObjects().property(File.class);
		this.matchesDir.convention(this.project.provider(() -> this.project.file("matches/matches")));
		this.matchesDir.finalizeValueOnRead();
	}

	public Project getProject() {
		return project;
	}

	@Override
	public Property<String> getGlobalCacheDir() {
		return globalCacheDir;
	}

	@Override
	public Property<String> getLocalCacheDir() {
		return localCacheDir;
	}

	@Override
	public Property<String> getMinecraftVersion() {
		return minecraftVersion;
	}

	@Override
	public Property<Integer> getIntermediaryGen() {
		return intermediaryGen;
	}

	private void configure(TaskSelection selection) throws Exception {
		boolean refreshDeps = project.getGradle().getStartParameter().isRefreshDependencies();

		String minecraftVersion = getMinecraftVersion().get();

		ConfigurationContainer configurations = project.getConfigurations();
		DependencyHandler dependencies = project.getDependencies();
		
		Configuration minecraftLibraries = configurations.register(Configurations.MINECRAFT_LIBRARIES).get();

		for (File cacheDir : files.getCacheDirectories()) {
			if (!cacheDir.exists()) {
				cacheDir.mkdirs();
			}
		}

		File versionsManifest = files.getVersionsManifest();

		if (refreshDeps || !versionsManifest.exists()) {
			project.getLogger().lifecycle(":downloading versions manifest");
			FileUtils.copyURLToFile(new URL(Constants.VERSIONS_MANIFEST_URL), versionsManifest);
		}

		File versionInfo = files.getVersionInfo(minecraftVersion);

		if (refreshDeps || !versionInfo.exists()) {
			project.getLogger().lifecycle(":downloading version info for Minecraft " + minecraftVersion);

			VersionsManifest manifest = getVersionsManifest();
			VersionsManifest.Entry entry = manifest.findOrThrow(minecraftVersion);

			FileUtils.copyURLToFile(new URL(entry.url()), versionInfo);
		}

		File versionDetails = files.getVersionDetails(minecraftVersion);

		if (refreshDeps || !versionDetails.exists()) {
			project.getLogger().lifecycle(":downloading version details for Minecraft " + minecraftVersion);

			VersionsManifest manifest = getVersionsManifest();
			VersionsManifest.Entry entry = manifest.findOrThrow(minecraftVersion);

			FileUtils.copyURLToFile(new URL(entry.details()), versionDetails);
		}

		VersionInfo info = getVersionInfo(minecraftVersion);

		for (Library library : info.libraries()) {
			Downloads downloads = library.downloads();
			Artifact artifact = downloads.artifact();

			if (artifact != null) {
				dependencies.add(minecraftLibraries.getName(), library.name());
			}
		}

		if (selection == TaskSelection.FEATHER) {
			Configuration decompileClasspath = configurations.register(Configurations.DECOMPILE_CLASSPATH).get();
			Configuration enigmaRuntime = configurations.register(Configurations.ENIGMA_RUNTIME).get();

			dependencies.add(decompileClasspath.getName(), "net.fabricmc:cfr:0.0.9");
			dependencies.add(decompileClasspath.getName(), "org.vineflower:vineflower:1.10.1");
			dependencies.add(enigmaRuntime.getName(), "net.ornithemc:enigma-swing:1.9.0");
			dependencies.add(enigmaRuntime.getName(), "org.quiltmc:quilt-enigma-plugin:1.3.0");
		}
	}

	@SuppressWarnings("unused")
	@Override
	public void tasks(TaskSelection selection) throws Exception {
		configure(selection);

		ConfigurationContainer configurations = project.getConfigurations();
		TaskContainer tasks = project.getTasks();

		TaskProvider<?> syncLibraries = tasks.register("syncMinecraftLibraries", Sync.class, task -> {
			task.from(configurations.getByName(Configurations.MINECRAFT_LIBRARIES));
			task.into(files.getLibrariesCache());
		});
		TaskProvider<?> downloadJars = tasks.register("downloadMinecraftJars", DownloadMinecraftJarsTask.class);
		TaskProvider<?> mergeJars = tasks.register("mergeMinecraftJars", MergeMinecraftJarsTask.class, task -> {
			task.dependsOn(downloadJars);
			task.getNamespace().set("official");
		});

		TaskProvider<?> updateNestsBuilds = tasks.register("updateNestsBuildsCache", UpdateBuildsCacheFromMetaTask.class, task -> {
			task.getMetaUrl().convention(Constants.META_URL);
			task.getMetaUrl().finalizeValueOnRead();
			task.getMetaEndpoint().convention("/v3/versions/nests");
			task.getMetaEndpoint().finalizeValueOnRead();
			task.getCacheFile().convention(project.provider(() -> files.getNestsBuildsCache()));
			task.getCacheFile().finalizeValueOnRead();
		});
		TaskProvider<?> updateSparrowBuilds = tasks.register("updateSparrowBuildsCache", UpdateBuildsCacheFromMetaTask.class, task -> {
			task.getMetaUrl().convention(Constants.META_URL);
			task.getMetaUrl().finalizeValueOnRead();
			task.getMetaEndpoint().convention("/v3/versions/sparrow");
			task.getMetaEndpoint().finalizeValueOnRead();
			task.getCacheFile().convention(project.provider(() -> files.getSparrowBuildsCache()));
			task.getCacheFile().finalizeValueOnRead();
		});

		TaskProvider<?> downloadNests = tasks.register("downloadNests", DownloadNestsTask.class);
		TaskProvider<?> downloadSparrow = tasks.register("downloadSparrow", DownloadSparrowTask.class);

		if (selection == TaskSelection.CALAMUS) {
			Action<GenerateIntermediaryTask> configureIntermediaryTask = task -> {
				task.dependsOn(mergeJars);
				task.getTargetNamespace().set("intermediary");
				task.getTargetPackage().set("net/minecraft/unmapped/");
				task.getNameLength().set(7);

				task.doFirst(_task -> {
					String minecraftVersion = task.getMinecraftVersion().get();
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
		}
		if (selection == TaskSelection.FEATHER) {
			TaskProvider<?> downloadIntermediary = tasks.register("downloadIntermediary", DownloadIntermediaryTask.class, task -> {
				task.dependsOn(mergeJars);
			});
			TaskProvider<?> mapMinecraftToIntermediary = tasks.register("mapMinecraftToIntermediary", MapMinecraftTask.class, task -> {
				task.dependsOn(syncLibraries, downloadIntermediary);
				task.getSourceNamespace().set("official");
				task.getTargetNamespace().set("intermediary");
			});
			TaskProvider<?> mergeIntermediaryJars = tasks.register("mergeIntermediaryMinecraftJars", MergeMinecraftJarsTask.class, task -> {
				task.dependsOn(mapMinecraftToIntermediary);
				task.getNamespace().set("intermediary");
			});

			TaskProvider<?> mapNestsToIntermediary = tasks.register("mapNestsToIntermediary", MapNestsTask.class, task -> {
				task.dependsOn(downloadNests, downloadIntermediary);
				task.getSourceNamespace().set("official");
				task.getTargetNamespace().set("intermediary");
				
			});
			TaskProvider<?> mergeIntermediaryNests = tasks.register("mergeIntermediaryNests", MergeNestsTask.class, task -> {
				task.dependsOn(mapNestsToIntermediary);
				task.getNamespace().set("intermediary");
			});

			TaskProvider<?> mapSparrowToIntermediary = tasks.register("mapSparrowToIntermediary", MapSparrowTask.class, task -> {
				task.dependsOn(downloadSparrow, downloadIntermediary);
				task.getSourceNamespace().set("official");
				task.getTargetNamespace().set("intermediary");
				
			});
			TaskProvider<?> mergeIntermediarySparrow = tasks.register("mergeIntermediarySparrow", MergeSparrowTask.class, task -> {
				task.dependsOn(mapSparrowToIntermediary);
				task.getNamespace().set("intermediary");
			});

			TaskProvider<?> processMappings = tasks.register("processMappings", ProcessMappingsTask.class, task -> {
				task.dependsOn(downloadIntermediary, mergeIntermediaryNests);
			});
			TaskProvider<?> processMinecraft = tasks.register("processMinecraft", ProcessMinecraftTask.class, task -> {
				task.dependsOn(mergeIntermediaryJars, mergeIntermediaryNests, mergeIntermediarySparrow, processMappings);
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
		}

		tasks.getByName("clean").doFirst(task -> {
			project.delete(files.getLocalBuildCache());
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
	public VersionInfo getVersionInfo() {
		return getVersionInfo(minecraftVersion.get());
	}

	@Override
	public VersionInfo getVersionInfo(String minecraftVersion) {
		return versionInfos.get(minecraftVersion);
	}

	@Override
	public VersionDetails getVersionDetails() {
		return getVersionDetails(minecraftVersion.get());
	}

	@Override
	public VersionDetails getVersionDetails(String minecraftVersion) {
		return versionDetails.get(minecraftVersion);
	}

	@Override
	public int getNestsBuild(String minecraftVersion, GameSide side) {
		return nestsBuilds.get(minecraftVersion).getOrDefault(side, -1);
	}

	@Override
	public int getSparrowBuild(String minecraftVersion, GameSide side) {
		return sparrowBuilds.get(minecraftVersion).getOrDefault(side, -1);
	}

	@Override
	public Property<File> getMatchesDirectory() {
		return matchesDir;
	}

	public Matches findMatches(String sideA, String versionA, String sideB, String versionB) {
		File dir = matchesDir.get();

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
