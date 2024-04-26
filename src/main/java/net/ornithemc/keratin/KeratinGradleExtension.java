package net.ornithemc.keratin;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.KeratinGradleExtensionAPI;
import net.ornithemc.keratin.api.task.DownloadVersionJsonTask;
import net.ornithemc.keratin.api.task.DownloadVersionsManifestTask;
import net.ornithemc.keratin.api.task.MapMinecraftTask;
import net.ornithemc.keratin.manifest.VersionDetails;
import net.ornithemc.keratin.manifest.VersionInfo;
import net.ornithemc.keratin.manifest.VersionsManifest;
import net.ornithemc.keratin.task.DownloadIntermediaryGen2Task;
import net.ornithemc.keratin.task.DownloadMinecraftJarsTask;
import net.ornithemc.keratin.task.DownloadMinecraftLibrariesTask;
import net.ornithemc.keratin.task.DownloadNestsTask;
import net.ornithemc.keratin.task.DownloadSparrowTask;
import net.ornithemc.keratin.task.MergeMinecraftJarsTask;
import net.ornithemc.keratin.task.ProcessMinecraftTask;
import net.ornithemc.keratin.task.UpdateBuildsCacheFromMetaTask;

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
	private final Property<VersionInfo> versionInfo;
	private final Property<VersionDetails> versionDetails;
	private final MapProperty<GameSide, Integer> nestsBuilds;
	private final MapProperty<GameSide, Integer> sparrowBuilds;

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
			KeratinGradleExtension.this.project.getLogger().warn("no Minecraft version given - defaulting to b1.0");
			return "b1.0";
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
		this.versionInfo = this.project.getObjects().property(VersionInfo.class);
		this.versionInfo.convention(this.project.provider(() -> {
			File file = KeratinGradleExtension.this.files.getVersionInfo();

			if (!file.exists()) {
				throw new RuntimeException("the version info file does not exist!");
			}

			String json = FileUtils.readFileToString(file, Charset.defaultCharset());
			VersionInfo info = KeratinGradleExtension.GSON.fromJson(json, VersionInfo.class);

			return info;
		}));
		this.versionInfo.finalizeValueOnRead();
		this.versionDetails = this.project.getObjects().property(VersionDetails.class);
		this.versionDetails.convention(this.project.provider(() -> {
			File file = KeratinGradleExtension.this.files.getVersionDetails();

			if (!file.exists()) {
				throw new RuntimeException("the version details file does not exist!");
			}

			String s = FileUtils.readFileToString(file, Charset.defaultCharset());
			VersionDetails details = KeratinGradleExtension.GSON.fromJson(s, VersionDetails.class);

			return details;
		}));
		this.versionDetails.finalizeValueOnRead();

		this.nestsBuilds = this.project.getObjects().mapProperty(GameSide.class, Integer.class);
		this.nestsBuilds.convention(this.project.provider(() -> {
			File file = KeratinGradleExtension.this.files.getNestsBuildsCache();

			if (!file.exists()) {
				return Collections.emptyMap();
			}

			Map<GameSide, Integer> builds = new EnumMap<>(GameSide.class);

			String s = FileUtils.readFileToString(file, Charset.defaultCharset());			
			JsonObject json = GSON.fromJson(s, JsonObject.class);

			for (GameSide side : GameSide.values()) {
				JsonElement buildJson = json.get(KeratinGradleExtension.this.minecraftVersion.get() + side.suffix());

				if (buildJson.isJsonPrimitive()) {
					builds.put(side, buildJson.getAsInt());
				}
			}

			return builds;
		}));
		this.nestsBuilds.finalizeValueOnRead();
		this.sparrowBuilds = this.project.getObjects().mapProperty(GameSide.class, Integer.class);
		this.sparrowBuilds.convention(this.project.provider(() -> {
			File file = KeratinGradleExtension.this.files.getSparrowBuildsCache();

			if (!file.exists()) {
				return Collections.emptyMap();
			}

			Map<GameSide, Integer> builds = new EnumMap<>(GameSide.class);

			String s = FileUtils.readFileToString(file, Charset.defaultCharset());			
			JsonObject json = GSON.fromJson(s, JsonObject.class);

			for (GameSide side : GameSide.values()) {
				JsonElement buildJson = json.get(KeratinGradleExtension.this.minecraftVersion.get() + side.suffix());

				if (buildJson.isJsonPrimitive()) {
					builds.put(side, buildJson.getAsInt());
				}
			}

			return builds;
		}));
		this.sparrowBuilds.finalizeValueOnRead();

		this.apply();
	}

	private void apply() {
		TaskContainer tasks = project.getTasks();

		TaskProvider<?> downloadManifest = tasks.register("downloadVersionsManifest", DownloadVersionsManifestTask.class, task -> {
			task.getUrl().convention(Constants.VERSIONS_MANIFEST_URL);
			task.getUrl().finalizeValueOnRead();
			task.getOutput().convention(project.provider(() -> files.getVersionsManifest()));
			task.getOutput().finalizeValueOnRead();
		});
		TaskProvider<?> downloadInfo = tasks.register("downloadVersionInfo", DownloadVersionJsonTask.class, task -> {
			task.dependsOn(downloadManifest);
			task.getUrl().convention(project.provider(() -> task.findManifestEntry(minecraftVersion.get()).url()));
			task.getUrl().finalizeValueOnRead();
			task.getOutput().convention(project.provider(() -> files.getVersionInfo()));
			task.getOutput().finalizeValueOnRead();
		});
		TaskProvider<?> downloadDetails = tasks.register("downloadVersionDetails", DownloadVersionJsonTask.class, task -> {
			task.dependsOn(downloadManifest);
			task.getUrl().convention(project.provider(() -> task.findManifestEntry(minecraftVersion.get()).details()));
			task.getUrl().finalizeValueOnRead();
			task.getOutput().convention(project.provider(() -> files.getVersionDetails()));
			task.getOutput().finalizeValueOnRead();
		});

		TaskProvider<?> downloadLibraries = tasks.register("downloadMinecraftLibraries", DownloadMinecraftLibrariesTask.class, task -> {
			task.dependsOn(downloadInfo);
		});
		TaskProvider<?> downloadJars = tasks.register("downloadMinecraftJars", DownloadMinecraftJarsTask.class, task -> {
			task.dependsOn(downloadDetails);
			task.getMinecraftVersion().convention(minecraftVersion);
			task.getMinecraftVersion().finalizeValueOnRead();
		});
		TaskProvider<?> mergeJars = tasks.register("mergeMinecraftJars", MergeMinecraftJarsTask.class, task -> {
			task.dependsOn(downloadDetails, downloadJars);
		});

		TaskProvider<?> downloadIntermediary = tasks.register("downloadIntermediary", DownloadIntermediaryGen2Task.class, task -> {
			task.dependsOn(downloadDetails);
			task.getUrl().convention(project.provider(() -> {
				String mc = getMinecraftVersion().get();
				int gen = getIntermediaryGen().get();

				if (gen == 1) {
					throw new IllegalStateException("gen1 intermediary is not supported at this time");
				} else {
					return Constants.calamusGen2Url(mc, gen);
				}
			}));
			task.getUrl().finalizeValueOnRead();
			task.getPathInJar().convention("mappings/mappings.tiny");
		});
		TaskProvider<?> mapMcToIntermediary = tasks.register("mapMinecraftToIntermediary", MapMinecraftTask.class, task -> {
			task.dependsOn(downloadLibraries, downloadIntermediary, mergeJars);
			task.getSourceNamespace().set("official");
			task.getTargetNamespace().set("intermediary");
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
			task.getClientUrl().convention(project.provider(() -> {
				return Constants.nestsUrl(getMinecraftVersion().get(), GameSide.CLIENT, getNestsBuilds().getting(GameSide.CLIENT).get());
			}));
			task.getClientUrl().finalizeValueOnRead();
			task.getServerUrl().convention(project.provider(() -> {
				return Constants.nestsUrl(getMinecraftVersion().get(), GameSide.SERVER, getNestsBuilds().getting(GameSide.SERVER).get());
			}));
			task.getServerUrl().finalizeValueOnRead();
			task.getMergedUrl().convention(project.provider(() -> {
				return Constants.nestsUrl(getMinecraftVersion().get(), GameSide.MERGED, getNestsBuilds().getting(GameSide.MERGED).get());
			}));
			task.getMergedUrl().finalizeValueOnRead();
		});
	
		TaskProvider<?> downloadSparrow = tasks.register("downloadSparrow", DownloadSparrowTask.class, task -> {
			task.dependsOn(downloadDetails);
			task.getClientUrl().convention(project.provider(() -> {
				return Constants.sparrowUrl(getMinecraftVersion().get(), GameSide.CLIENT, getSparrowBuilds().getting(GameSide.CLIENT).get());
			}));
			task.getClientUrl().finalizeValueOnRead();
			task.getServerUrl().convention(project.provider(() -> {
				return Constants.sparrowUrl(getMinecraftVersion().get(), GameSide.SERVER, getSparrowBuilds().getting(GameSide.SERVER).get());
			}));
			task.getServerUrl().finalizeValueOnRead();
			task.getMergedUrl().convention(project.provider(() -> {
				return Constants.sparrowUrl(getMinecraftVersion().get(), GameSide.MERGED, getSparrowBuilds().getting(GameSide.MERGED).get());
			}));
			task.getMergedUrl().finalizeValueOnRead();
		});

		TaskProvider<?> processMc = tasks.register("processMinecraft", ProcessMinecraftTask.class, task -> {
			task.dependsOn(mapMcToIntermediary, downloadNests, downloadSparrow);
		});

		tasks.getByName("clean").doFirst(task -> {
			project.delete(files.getLocalCacheDir());
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
	public VersionInfo getVersionInfo() {
		return versionInfo.get();
	}

	@Override
	public VersionDetails getVersionDetails() {
		return versionDetails.get();
	}

	@Override
	public MapProperty<GameSide, Integer> getNestsBuilds() {
		return nestsBuilds;
	}

	@Override
	public MapProperty<GameSide, Integer> getSparrowBuilds() {
		return sparrowBuilds;
	}
}
