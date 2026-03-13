package net.ornithemc.keratin.api.maven;

import java.net.URI;
import java.net.URISyntaxException;

public record MavenArtifact(String repositoryUrl, String groupId, String artifactId, String version, String classifier, String sha1) {

	public static MavenArtifact of(String maven) {
		String[] s = maven.split("[:]");
		return of(s[0], s[1], s[2]);
	}

	public static MavenArtifact of(String groupId, String artifactId, String version) {
		return new MavenArtifact(null, groupId, artifactId, version, null, null);
	}

	public MavenArtifact withRepositoryUrl(String repositoryUrl) {
		return new MavenArtifact(repositoryUrl, groupId, artifactId, version, classifier, sha1);
	}

	public MavenArtifact withClassifier(String classifier) {
		return new MavenArtifact(repositoryUrl, groupId, artifactId, version, classifier, sha1);
	}

	public MavenArtifact withSha1(String sha1) {
		return new MavenArtifact(repositoryUrl, groupId, artifactId, version, classifier, sha1);
	}

	public String url() {
		URI base = URI.create(repositoryUrl);

		String scheme = base.getScheme();
		String host = base.getHost();
		String path = base.getPath();

		StringBuilder sb = new StringBuilder();

		if (path != null) {
			sb.append(path);
		}
		sb.append('/');
		sb.append(groupId.replace('.', '/'));
		sb.append('/');
		sb.append(artifactId);
		sb.append('/');
		sb.append(version);
		sb.append('/');
		sb.append(artifactId);
		sb.append('-');
		sb.append(version);
		if (classifier != null) {
			sb.append('-');
			sb.append(classifier);
		}
		sb.append(".jar");

		path = sb.toString();

		try {
			// this feels super hacky and dumb but seemingly there's
			// no utility in the JDK for encoding url path segments???
			return new URI(scheme, host, path, null).toString();
		} catch (URISyntaxException e) {
			throw new IllegalStateException("unable to construct valid url", e);
		}
	}
}
