package net.ornithemc.keratin.maven;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.maven.MavenArtifact;
import net.ornithemc.keratin.api.maven.MetaSourcedMavenArtifacts;
import net.ornithemc.keratin.api.maven.MultipleBuildsMavenArtifacts;

public abstract class MetaSourcedMultipleBuildsMavenArtifacts implements MetaSourcedMavenArtifacts, MultipleBuildsMavenArtifacts {

	private final KeratinGradleExtension keratin;
	private Map<String, Map<Integer, String>> versions;
	private Map<String, Map<Integer, MavenArtifact>> artifacts;

	@Inject
	public MetaSourcedMultipleBuildsMavenArtifacts(KeratinGradleExtension keratin) {
		this.keratin = keratin;
	}

	@Override
	public boolean contains(String minecraftVersion) {
		if (versions == null) {
			findVersions();
		}

		return versions.containsKey(minecraftVersion);
	}

	@Override
	public boolean contains(String minecraftVersion, int build) {
		if (versions == null) {
			findVersions();
		}

		return versions.containsKey(minecraftVersion) && versions.get(minecraftVersion).containsKey(build);
	}

	@Override
	public Map<String, Integer> getLatestBuilds() {
		if (versions == null) {
			findVersions();
		}

		return versions.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().keySet().stream().max(Comparator.naturalOrder()).orElse(0)));
	}

	@Override
	public int getLatestBuild(String minecraftVersion) {
		if (versions == null) {
			findVersions();
		}

		return versions.getOrDefault(minecraftVersion, Collections.emptyMap()).keySet().stream().max(Comparator.naturalOrder()).orElse(0);
	}

	@Override
	public MavenArtifact get(String minecraftVersion, int build) {
		if (versions == null) {
			findVersions();
		}
		if (!artifacts.containsKey(minecraftVersion) || !artifacts.get(minecraftVersion).containsKey(build)) {
			findArtifact(minecraftVersion, build);
		}

		return artifacts.get(minecraftVersion).get(build);
	}

	private void findVersions() {
		versions = new HashMap<>();
		artifacts = new HashMap<>();

		String metaEndpointUrl = String.format("%s%s",
			getMetaUrl().get(),
			getMetaEndpoint().get().formatted(keratin.getIntermediaryGen().get())
		);

		try {
			try (InputStreamReader ir = new InputStreamReader(new URI(metaEndpointUrl).toURL().openStream())) {
				JsonArray jsonArray = KeratinGradleExtension.GSON.fromJson(ir, JsonArray.class);

				for (JsonElement jsonEntry : jsonArray) {
					if (jsonEntry.isJsonObject()) {
						JsonObject json = jsonEntry.getAsJsonObject();
						String version = json.get("gameVersion").getAsString();
						int build = json.get("build").getAsInt();
						String maven = json.get("maven").getAsString();

						versions.computeIfAbsent(version, key -> new HashMap<>()).put(build, maven);
					}
				}
			}
		} catch (Exception e) {
			keratin.getProject().getLogger().warn("unable to parse maven artifact versions from " + metaEndpointUrl + "!", e);
		}
	}

	private void findArtifact(String minecraftVersion, int build) {
		Map<Integer, String> builds = versions.get(minecraftVersion);
		MavenArtifact artifact = null;

		if (builds != null) {
			String maven = builds.get(build);

			if (maven != null) {
				artifact = MavenArtifact.of(maven)
					.withRepositoryUrl(getRepositoryUrl().get())
					.withClassifier(getClassifier().getOrNull());

				try {
					String sha1Path = artifact.url() + ".sha1";
					URL sha1Url = new URI(sha1Path).toURL();

					try (BufferedReader br = new BufferedReader(new InputStreamReader(sha1Url.openStream()))) {
						artifact = artifact.withSha1(br.readLine());
					}
				} catch (Exception e) {
					keratin.getProject().getLogger().warn("unable to find maven artifact sha1 hash for " + artifact.artifactId() + " version " + artifact.version() + "!", e);
				}
			}
		}

		artifacts.computeIfAbsent(minecraftVersion, key -> new HashMap<>()).put(build, artifact);
	}
}
