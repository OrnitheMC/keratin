package net.ornithemc.keratin;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.KeratinGradleExtensionAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.manifest.VersionInfo;
import net.ornithemc.keratin.api.manifest.VersionsManifest;
import net.ornithemc.keratin.api.task.MakeCacheDirectoriesTask;
import net.ornithemc.keratin.api.task.manifest.DownloadVersionDetailsTask;
import net.ornithemc.keratin.api.task.manifest.DownloadVersionInfoTask;
import net.ornithemc.keratin.api.task.manifest.DownloadVersionsManifestTask;
import net.ornithemc.keratin.api.task.merging.MergeMinecraftJarsTask;
import net.ornithemc.keratin.api.task.minecraft.DownloadMinecraftJarsTask;
import net.ornithemc.keratin.api.task.minecraft.DownloadMinecraftLibrariesTask;
import net.ornithemc.keratin.api.task.processing.DownloadNestsTask;
import net.ornithemc.keratin.api.task.processing.DownloadSparrowTask;
import net.ornithemc.keratin.api.task.processing.UpdateBuildsCacheFromMetaTask;
import net.ornithemc.keratin.matching.Matches;
import net.ornithemc.keratin.util.Versioned;

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

				if (buildJson.isJsonPrimitive()) {
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

				if (buildJson.isJsonPrimitive()) {
					builds.put(side, buildJson.getAsInt());
				}
			}

			return builds;
		});
		this.matchesDir = this.project.getObjects().property(File.class);
		this.matchesDir.convention(this.project.provider(() -> this.project.file("matches")));
		this.matchesDir.finalizeValueOnRead();

		this.apply();
	}

	private void apply() {
		TaskContainer tasks = project.getTasks();

		TaskProvider<?> makeDirs = tasks.register("makeCacheDirectories", MakeCacheDirectoriesTask.class);
		TaskProvider<?> downloadManifest = tasks.register("downloadVersionsManifest", DownloadVersionsManifestTask.class, task -> {
			task.dependsOn(makeDirs);
			task.getUrl().convention(Constants.VERSIONS_MANIFEST_URL);
			task.getUrl().finalizeValueOnRead();
			task.getOutput().convention(project.provider(() -> files.getVersionsManifest()));
			task.getOutput().finalizeValueOnRead();
		});
		TaskProvider<?> downloadInfo = tasks.register("downloadVersionInfo", DownloadVersionInfoTask.class, task -> {
			task.dependsOn(downloadManifest);
			task.getMinecraftVersion().convention(this.minecraftVersion);
			task.getMinecraftVersion().finalizeValueOnRead();
		});
		TaskProvider<?> downloadDetails = tasks.register("downloadVersionDetails", DownloadVersionDetailsTask.class, task -> {
			task.dependsOn(downloadManifest);
			task.getMinecraftVersion().convention(this.minecraftVersion);
			task.getMinecraftVersion().finalizeValueOnRead();
		});

		TaskProvider<?> downloadLibraries = tasks.register("downloadMinecraftLibraries", DownloadMinecraftLibrariesTask.class, task -> {
			task.dependsOn(downloadInfo);
			task.getMinecraftVersion().convention(this.minecraftVersion);
			task.getMinecraftVersion().finalizeValueOnRead();
		});
		TaskProvider<?> downloadJars = tasks.register("downloadMinecraftJars", DownloadMinecraftJarsTask.class, task -> {
			task.dependsOn(downloadDetails);
			task.getMinecraftVersion().convention(this.minecraftVersion);
			task.getMinecraftVersion().finalizeValueOnRead();
		});
		TaskProvider<?> mergeJars = tasks.register("mergeMinecraftJars", MergeMinecraftJarsTask.class, task -> {
			task.dependsOn(downloadDetails, downloadJars);
			task.getMinecraftVersion().convention(this.minecraftVersion);
			task.getMinecraftVersion().finalizeValueOnRead();
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

		TaskProvider<?> downloadNests = tasks.register("downloadNests", DownloadNestsTask.class, task -> {
			task.dependsOn(downloadDetails);
			task.getMinecraftVersion().convention(this.minecraftVersion);
			task.getMinecraftVersion().finalizeValueOnRead();
		});
	
		TaskProvider<?> downloadSparrow = tasks.register("downloadSparrow", DownloadSparrowTask.class, task -> {
			task.dependsOn(downloadDetails);
			task.getMinecraftVersion().convention(this.minecraftVersion);
			task.getMinecraftVersion().finalizeValueOnRead();
		});

		tasks.getByName("clean").doFirst(task -> {
			project.delete(files.getLocalBuildCache());
		});
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
	public Map<GameSide, Integer> getNestsBuilds(String minecraftVersion) {
		return nestsBuilds.get(minecraftVersion);
	}

	@Override
	public int getNestsBuild(String minecraftVersion, GameSide side) {
		return nestsBuilds.get(minecraftVersion).get(side);
	}

	@Override
	public Map<GameSide, Integer> getSparrowBuilds(String minecraftVersion) {
		return sparrowBuilds.get(minecraftVersion);
	}

	@Override
	public int getSparrowBuild(String minecraftVersion, GameSide side) {
		return sparrowBuilds.get(minecraftVersion).get(side);
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
			inverted = false;
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

	static File findMatches(File dir, String name) {
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
