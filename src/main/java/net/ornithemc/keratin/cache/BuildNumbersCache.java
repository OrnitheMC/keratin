package net.ornithemc.keratin.cache;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.settings.BuildNumbers;

public class BuildNumbersCache {

	private final File cacheFile;
	private final boolean respectSplitObfuscation;

	private final BuildNumbersCache oldCache;

	private Map<String, Integer> builds;
	private Map<MinecraftVersion, BuildNumbers> buildNumbers;

	public BuildNumbersCache(KeratinGradleExtension keratin, File cacheFile, File oldCacheFile, boolean respectSplitObfuscation) {
		this.cacheFile = cacheFile;
		this.respectSplitObfuscation = respectSplitObfuscation;

		if (oldCacheFile != null) {
			this.oldCache = new BuildNumbersCache(keratin, oldCacheFile, null, respectSplitObfuscation);
		} else {
			this.oldCache = null;
		}

		this.builds = new HashMap<>();
		this.buildNumbers = new HashMap<>();

		try {
			loadFromCache();

			if (this.oldCache != null) {
				if (this.oldCache.cacheFile.exists()) {
					this.oldCache.cacheFile.delete();
				}
				if (this.cacheFile.exists()) {
					Files.copy(this.cacheFile, this.oldCache.cacheFile);
				}
			}
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

	public BuildNumbersCache getOldCache() {
		return oldCache;
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
