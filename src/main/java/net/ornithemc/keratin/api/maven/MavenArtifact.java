package net.ornithemc.keratin.api.maven;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
		StringBuilder sb = new StringBuilder();

		sb.append(repositoryUrl);
		if (sb.charAt(sb.length() - 1) != '/') {
			sb.append('/');
		}
		sb.append(escape(groupId).replace('.', '/'));
		sb.append('/');
		sb.append(escape(artifactId));
		sb.append('/');
		sb.append(escape(version));
		sb.append('/');
		sb.append(escape(artifactId));
		sb.append('-');
		sb.append(escape(version));
		if (classifier != null) {
			sb.append('-');
			sb.append(escape(classifier));
		}
		sb.append(".jar");

		return sb.toString();
	}

	private static String escape(String s) {
		return URLEncoder.encode(s, StandardCharsets.UTF_8);
	}
}
