package net.ornithemc.keratin.api.maven;

public record MavenArtifact(String repositoryUrl, String groupId, String artifactId, String version, String classifier, String sha1) {

	public static MavenArtifact of(String maven) {
		String[] s = maven.split("[:]");
		return new MavenArtifact(null, s[0], s[1], s[2], null, null);
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

		return sb.toString();
	}
}
