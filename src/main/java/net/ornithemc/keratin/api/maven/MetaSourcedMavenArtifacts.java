package net.ornithemc.keratin.api.maven;

import org.gradle.api.provider.Property;

public interface MetaSourcedMavenArtifacts extends MavenArtifacts {

	Property<String> getMetaUrl();

	Property<String> getMetaEndpoint();

}
