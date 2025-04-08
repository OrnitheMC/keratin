package net.ornithemc.keratin.maven;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.maven.MavenArtifact;
import net.ornithemc.keratin.api.maven.SingleBuildMavenArtifacts;
import net.ornithemc.keratin.api.maven.MetaSourcedMavenArtifactsAPI;

public class MetaSourcedSingleBuildMavenArtifacts implements MetaSourcedMavenArtifactsAPI, SingleBuildMavenArtifacts {

	private final KeratinGradleExtension keratin;
	private Map<String, String> versions;
	private Map<String, MavenArtifact> artifacts;

	private String metaUrl;
	private String metaEndpoint;
	private String repositoryUrl;
	private String classifier;

	public MetaSourcedSingleBuildMavenArtifacts(KeratinGradleExtension keratin) {
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
	public MavenArtifact get(String minecraftVersion) {
		if (versions == null) {
			findVersions();
		}
		if (!artifacts.containsKey(minecraftVersion)) {
			findArtifact(minecraftVersion);
		}

		return artifacts.get(minecraftVersion);
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
						String version = json.get("version").getAsString();
						String maven = json.get("maven").getAsString();

						versions.put(version, maven);
					}
				}
			}
		} catch (IOException e) {
			keratin.getProject().getLogger().warn("unable to parse maven artifact versions from " + metaEndpointUrl + "!", e);
		}
	}

	private void findArtifact(String minecraftVersion) {
		String maven = versions.get(minecraftVersion);
		MavenArtifact artifact = null;

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

		artifacts.put(minecraftVersion, artifact);
	}
}
