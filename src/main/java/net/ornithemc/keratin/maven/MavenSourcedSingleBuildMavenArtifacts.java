package net.ornithemc.keratin.maven;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import groovy.util.Node;
import groovy.util.NodeList;
import groovy.xml.XmlParser;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.maven.MavenArtifact;
import net.ornithemc.keratin.api.maven.MavenSourcedMavenArtifacts;
import net.ornithemc.keratin.api.maven.SingleBuildMavenArtifacts;

public abstract class MavenSourcedSingleBuildMavenArtifacts implements MavenSourcedMavenArtifacts, SingleBuildMavenArtifacts {

	private final KeratinGradleExtension keratin;
	private Set<String> versions;
	private Map<String, MavenArtifact> artifacts;

	@Inject
	public MavenSourcedSingleBuildMavenArtifacts(KeratinGradleExtension keratin) {
		this.keratin = keratin;
	}

	@Override
	public boolean contains(String minecraftVersion) {
		if (versions == null) {
			findVersions();
		}

		return versions.contains(minecraftVersion);
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
		versions = new HashSet<>();
		artifacts = new HashMap<>();

		String mavenPomUrl = String.format("%s%s/%s/maven-metadata.xml",
			getRepositoryUrl().get(),
			getGroupId().get().replace('.', '/'),
			getArtifactId().get().formatted(keratin.getIntermediaryGen().get())
		);

		try {
			try (InputStream is = new URI(mavenPomUrl).toURL().openStream()) {
				XmlParser parser = new XmlParser();

				Node root = parser.parse(is);
				Node versioning = (Node) ((NodeList) root.get("versioning")).getFirst();
				Node versionsNode = (Node) ((NodeList) versioning.get("versions")).getFirst();

				NodeList versionNode = (NodeList) versionsNode.get("version");

				for (Object o : versionNode) {
					Node node = (Node) o;
					String version = node.text();

					versions.add(version);
				}
			}
		} catch (Exception e) {
			keratin.getProject().getLogger().warn("unable to parse maven artifact versions from " + mavenPomUrl + "!", e);
		}
	}

	private void findArtifact(String minecraftVersion) {
		MavenArtifact artifact = null;

		if (versions.contains(minecraftVersion)) {
			artifact = MavenArtifact.of(
					getGroupId().get(),
					getArtifactId().get().formatted(keratin.getIntermediaryGen().get()),
					minecraftVersion
				)
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
