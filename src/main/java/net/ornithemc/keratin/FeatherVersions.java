package net.ornithemc.keratin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class FeatherVersions {

	private final KeratinGradleExtension keratin;
	private Map<String, Integer> builds;

	public FeatherVersions(KeratinGradleExtension keratin) {
		this.keratin = keratin;
	}

	public String getNext(String minecraftVersion) {
		if (builds == null) {
			findBuilds();
		}

		return "%s+build.%d".formatted(minecraftVersion, builds.getOrDefault(minecraftVersion, 0) + 1);
	}

	private void findBuilds() {
		builds = new HashMap<>();

		try {
			String metaUrl = Constants.META_URL + "/v3/versions/gen%d/feather".formatted(keratin.getIntermediaryGen().get());

			try (InputStreamReader ir = new InputStreamReader(new URL(metaUrl).openStream())) {
				JsonArray jsonArray = KeratinGradleExtension.GSON.fromJson(ir, JsonArray.class);

				for (JsonElement jsonEntry : jsonArray) {
					if (jsonEntry.isJsonObject()) {
						JsonObject json = jsonEntry.getAsJsonObject();

						String gameVersion = json.get("gameVersion").getAsString();
						int build = json.get("build").getAsInt();

						builds.compute(gameVersion, (key, value) -> value == null ? build : Math.max(value, build));
					}
				}
			}
		} catch (IOException e) {
			keratin.getProject().getLogger().warn("unable to find Feather builds!", e);
		}
	}
}
