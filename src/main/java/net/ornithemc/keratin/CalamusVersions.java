package net.ornithemc.keratin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CalamusVersions {

	private final KeratinGradleExtension keratin;
	private Set<String> versions;

	public CalamusVersions(KeratinGradleExtension keratin) {
		this.keratin = keratin;
	}

	public boolean contains(String minecraftVersion) {
		if (versions == null) {
			findVersions();
		}

		return versions.contains(minecraftVersion);
	}

	private void findVersions() {
		versions = new HashSet<>();

		try {
			String metaUrl = Constants.META_URL + "/v3/versions/intermediary-gen%d".formatted(keratin.getIntermediaryGen().get());

			try (InputStreamReader ir = new InputStreamReader(new URL(metaUrl).openStream())) {
				JsonArray jsonArray = KeratinGradleExtension.GSON.fromJson(ir, JsonArray.class);

				for (JsonElement jsonEntry : jsonArray) {
					if (jsonEntry.isJsonObject()) {
						JsonObject json = jsonEntry.getAsJsonObject();
						versions.add(json.get("version").getAsString());
					}
				}
			}
		} catch (IOException e) {
			keratin.getProject().getLogger().warn("unable to find Calamus builds!", e);
		}
	}
}
