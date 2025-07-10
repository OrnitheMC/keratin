package net.ornithemc.keratin.api.maven;

import org.gradle.api.provider.Property;

public interface MavenArtifacts {

	Property<String> getRepositoryUrl();

	Property<String> getClassifier();

}
