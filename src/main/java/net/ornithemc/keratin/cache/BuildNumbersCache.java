package net.ornithemc.keratin.cache;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.settings.BuildNumbers;

public class BuildNumbersCache {

	private final File cacheFile;
	private final boolean respectSplitObfuscation;

	private final BuildNumbersCache backup;

	private Map<String, Integer> builds;
	private Map<MinecraftVersion, BuildNumbers> buildNumbers;

	public BuildNumbersCache(KeratinGradleExtension keratin, File cacheFile, File backupCacheFile, boolean respectSplitObfuscation) {
		this.cacheFile = cacheFile;
		this.respectSplitObfuscation = respectSplitObfuscation;

		if (backupCacheFile != null) {
			this.backup = new BuildNumbersCache(keratin, backupCacheFile, null, respectSplitObfuscation);
		} else {
			this.backup = null;
		}

		this.builds = new HashMap<>();
		this.buildNumbers = new HashMap<>();

		try {
			loadFromCache();
		} catch (IOException e) {
			keratin.getProject().getLogger().lifecycle("error while load build numbers from cache " + cacheFile.getName(), e);
		}
	}

	private void loadFromCache() throws IOException {
		builds.clear();
		buildNumbers.clear();

		if (cacheFile.exists()) {
			String s = FileUtils.readFileToString(cacheFile, Charset.defaultCharset());			
			JsonObject json = KeratinGradleExtension.GSON.fromJson(s, JsonObject.class);

			for (Map.Entry<String, JsonElement> e : json.entrySet()) {
				String minecraftVersionId = e.getKey();
				JsonElement element = e.getValue();

				if (element.isJsonPrimitive()) {
					builds.put(minecraftVersionId, element.getAsInt());
				}
			}
		}
	}

	public void backUp() throws IOException {
		if (backup != null) {
			backup.update(builds);
		}
	}

	public void update(Map<String, Integer> latestBuilds) throws IOException {
		builds.clear();
		buildNumbers.clear();

		builds.putAll(latestBuilds);

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(cacheFile))) {
			KeratinGradleExtension.GSON.toJson(builds, bw);
		}
	}

	public File getFile() {
		return cacheFile;
	}

	public BuildNumbersCache getBackup() {
		return backup;
	}

	public int getBuild(String minecraftVersion) {
		return builds.getOrDefault(minecraftVersion, 0);
	}

	public BuildNumbers getBuildNumbers(MinecraftVersion minecraftVersion) {
		if (!buildNumbers.containsKey(minecraftVersion)) {
			findBuildNumbers(minecraftVersion);
		}

		return buildNumbers.get(minecraftVersion);
	}

	private void findBuildNumbers(MinecraftVersion minecraftVersion) {
		BuildNumbers buildNumbers = BuildNumbers.none();

		if (respectSplitObfuscation ? minecraftVersion.hasSharedObfuscation() : minecraftVersion.hasSharedVersioning()) {
			int build = getBuild(minecraftVersion.id());

			if (build > 0) {
				buildNumbers = buildNumbers.withBuild(build);
			}
		} else if (respectSplitObfuscation && minecraftVersion.hasSharedVersioning()) {
			int clientBuild = minecraftVersion.hasClient() ? getBuild(minecraftVersion.clientKey()) : -1;
			int serverBuild = minecraftVersion.hasServer() ? getBuild(minecraftVersion.serverKey()) : -1;

			if (clientBuild > 0 || serverBuild > 0) {
				buildNumbers = buildNumbers.withBuilds(clientBuild, serverBuild);
			}
		} else {
			int clientBuild = minecraftVersion.hasClient() ? getBuild(minecraftVersion.client().id()) : -1;
			int serverBuild = minecraftVersion.hasServer() ? getBuild(minecraftVersion.server().id()) : -1;

			if (clientBuild > 0 || serverBuild > 0) {
				buildNumbers = buildNumbers.withBuilds(clientBuild, serverBuild);
			}
		}

		this.buildNumbers.put(minecraftVersion, buildNumbers);
	}
}
