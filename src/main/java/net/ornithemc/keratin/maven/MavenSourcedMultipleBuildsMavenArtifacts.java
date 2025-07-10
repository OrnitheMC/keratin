package net.ornithemc.keratin.maven;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import groovy.util.Node;
import groovy.util.NodeList;
import groovy.xml.XmlParser;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.maven.MavenArtifact;
import net.ornithemc.keratin.api.maven.MavenSourcedMavenArtifacts;
import net.ornithemc.keratin.api.maven.MultipleBuildsMavenArtifacts;

public abstract class MavenSourcedMultipleBuildsMavenArtifacts implements MavenSourcedMavenArtifacts, MultipleBuildsMavenArtifacts {

	private static final Pattern VERSION_WITH_BUILD_PATTERN = Pattern.compile("(.+)\\+build\\.(\\d)");

	private final KeratinGradleExtension keratin;
	private Map<String, Set<Integer>> versions;
	private Map<String, Map<Integer, MavenArtifact>> artifacts;

	@Inject
	public MavenSourcedMultipleBuildsMavenArtifacts(KeratinGradleExtension keratin) {
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

		return versions.containsKey(minecraftVersion) && versions.get(minecraftVersion).contains(build);
	}

	@Override
	public Map<String, Integer> getLatestBuilds() {
		if (versions == null) {
			findVersions();
		}

		return versions.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().max(Comparator.naturalOrder()).orElse(0)));
	}

	@Override
	public int getLatestBuild(String minecraftVersion) {
		if (versions == null) {
			findVersions();
		}

		return versions.getOrDefault(minecraftVersion, Collections.emptySet()).stream().max(Comparator.naturalOrder()).orElse(0);
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
					String artifactVersion = node.text();

					Matcher matcher = VERSION_WITH_BUILD_PATTERN.matcher(artifactVersion);

					if (matcher.matches()) {
						String version = matcher.group(1);
						int build = Integer.parseInt(matcher.group(2));

						versions.computeIfAbsent(version, key -> new HashSet<>()).add(build);
					}
				}
			}
		} catch (Exception e) {
			keratin.getProject().getLogger().warn("unable to parse maven artifact versions from " + mavenPomUrl + "!", e);
		}
	}

	private void findArtifact(String minecraftVersion, int build) {
		Set<Integer> builds = versions.get(minecraftVersion);
		MavenArtifact artifact = null;

		if (builds != null && builds.contains(build)) {
			artifact = MavenArtifact.of(
					getGroupId().get(),
					getArtifactId().get().formatted(keratin.getIntermediaryGen().get()),
					String.format("%s+build.%d", minecraftVersion, build)
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

		artifacts.computeIfAbsent(minecraftVersion, key -> new HashMap<>()).put(build, artifact);
	}
}
