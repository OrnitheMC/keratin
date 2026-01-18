package net.ornithemc.keratin.maven;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.maven.MavenArtifact;
import net.ornithemc.keratin.api.maven.MetaSourcedMavenArtifacts;
import net.ornithemc.keratin.api.maven.SingleBuildMavenArtifacts;

public abstract class MetaSourcedSingleBuildMavenArtifacts implements MetaSourcedMavenArtifacts, SingleBuildMavenArtifacts {

	private final KeratinGradleExtension keratin;
	private Map<String, String> versions;
	private Map<String, MavenArtifact> artifacts;

	@Inject
	public MetaSourcedSingleBuildMavenArtifacts(KeratinGradleExtension keratin) {
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

		if (!getRepositoryUrl().isPresent()) {
			return;
		}

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
						String version = json.get("version").getAsString();
						String maven = json.get("maven").getAsString();

						versions.put(version, maven);
					}
				}
			}
		} catch (Exception e) {
			keratin.getProject().getLogger().warn("unable to parse maven artifact versions from " + metaEndpointUrl + "!", e);
		}
	}

	private void findArtifact(String minecraftVersion) {
		String maven = versions.get(minecraftVersion);
		MavenArtifact artifact = null;

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

		artifacts.put(minecraftVersion, artifact);
	}
}
