package net.ornithemc.keratin.api.maven;

import org.gradle.api.provider.Property;

public interface MavenSourcedMavenArtifacts extends MavenArtifacts {

	Property<String> getGroupId();

	Property<String> getArtifactId();

}
