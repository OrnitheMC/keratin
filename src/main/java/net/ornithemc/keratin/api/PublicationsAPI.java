package net.ornithemc.keratin.api;

import org.gradle.api.provider.Property;

public interface PublicationsAPI {

	Property<String> getGroupId();

	Property<String> getArtifactId();

}
