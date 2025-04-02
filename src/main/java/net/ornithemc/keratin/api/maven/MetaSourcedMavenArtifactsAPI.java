package net.ornithemc.keratin.api.maven;

public interface MetaSourcedMavenArtifactsAPI extends MavenArtifactsAPI {

	void setMetaUrl(String metaUrl);

	void setMetaEndpoint(String endpoint);

}
