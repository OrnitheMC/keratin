package net.ornithemc.keratin.maven;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.maven.MavenArtifact;
import net.ornithemc.keratin.api.maven.MetaSourcedMavenArtifactsAPI;
import net.ornithemc.keratin.api.maven.MultipleBuildsMavenArtifacts;

public class MetaSourcedMultipleBuildsMavenArtifacts implements MetaSourcedMavenArtifactsAPI, MultipleBuildsMavenArtifacts {

	private final KeratinGradleExtension keratin;
	private Map<String, Map<Integer, String>> versions;
	private Map<String, Map<Integer, MavenArtifact>> artifacts;

	private String metaUrl;
	private String metaEndpoint;
	private String repositoryUrl;
	private String classifier;

	public MetaSourcedMultipleBuildsMavenArtifacts(KeratinGradleExtension keratin) {
		this.keratin = keratin;
	}

	@Override
	public void setMetaUrl(String metaUrl) {
		this.metaUrl = metaUrl;
	}

	@Override
	public void setMetaEndpoint(String metaEndpoint) {
		this.metaEndpoint = metaEndpoint;
	}

	@Override
	public void setRepositoryUrl(String repositoryUrl) {
		this.repositoryUrl = repositoryUrl;
	}

	@Override
	public void setClassifier(String classifier) {
		this.classifier = classifier;
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

		return versions.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().keySet().stream().max(Comparator.naturalOrder()).get()));
	}

	@Override
	public int getLatestBuild(String minecraftVersion) {
		if (versions == null) {
			findVersions();
		}

		return versions.get(minecraftVersion).keySet().stream().max(Comparator.naturalOrder()).get();
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

		String metaEndpointUrl = metaUrl + metaEndpoint.formatted(keratin.getIntermediaryGen().get());

		try {
			try (InputStreamReader ir = new InputStreamReader(new URL(metaEndpointUrl).openStream())) {
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
		} catch (IOException e) {
			keratin.getProject().getLogger().warn("unable to parse maven artifact versions from " + metaEndpointUrl + "!", e);
		}
	}

	private void findArtifact(String minecraftVersion, int build) {
		Map<Integer, String> builds = versions.get(minecraftVersion);
		MavenArtifact artifact = null;

		if (builds != null) {
			String maven = builds.get(build);

			if (maven != null) {
				artifact = MavenArtifact.of(maven).withRepositoryUrl(repositoryUrl).withClassifier(classifier);

				try {
					String sha1Path = artifact.url() + ".sha1";
					URL sha1Url = new URL(sha1Path);

					try (BufferedReader br = new BufferedReader(new InputStreamReader(sha1Url.openStream()))) {
						artifact = artifact.withSha1(br.readLine());
					}
				} catch (IOException e) {
					keratin.getProject().getLogger().warn("unable to find maven artifact sha1 hash for " + artifact.artifactId() + " version " + artifact.version() + "!", e);
				}
			}
		}

		artifacts.computeIfAbsent(minecraftVersion, key -> new HashMap<>()).put(build, artifact);
	}
}
