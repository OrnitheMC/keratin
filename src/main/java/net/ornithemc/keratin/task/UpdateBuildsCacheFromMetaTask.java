package net.ornithemc.keratin.task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskAction;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.task.KeratinTask;

public abstract class UpdateBuildsCacheFromMetaTask extends KeratinTask {

	public abstract Property<String> getMetaUrl();

	public abstract Property<String> getMetaEndpoint();

	public abstract Property<File> getCacheFile();

	@TaskAction
	public void run() throws IOException {
		getProject().getLogger().lifecycle(":updating builds cache: " + getCacheFile().get().getName());

		String metaUrl = getMetaUrl().get() + getMetaEndpoint().get();
		File cacheFile = getCacheFile().get();

		Map<String, Integer> builds = new LinkedHashMap<>();

		try (InputStreamReader ir = new InputStreamReader(new URL(metaUrl).openStream())) {
			JsonArray jsonArray = KeratinGradleExtension.GSON.fromJson(ir, JsonArray.class);

			for (JsonElement jsonEntry : jsonArray) {
				if (jsonEntry.isJsonObject()) {
					JsonObject json = jsonEntry.getAsJsonObject();

					String gameVersion = json.get("gameVersion").getAsString();
					int build = json.get("build").getAsInt();

					builds.compute(gameVersion, (key, value) -> Math.max(value, build));
				}
			}
		}
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(cacheFile))) {
			KeratinGradleExtension.GSON.toJson(builds, bw);
		}
	}
}
