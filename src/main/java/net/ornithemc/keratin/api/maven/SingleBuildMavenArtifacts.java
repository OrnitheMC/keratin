package net.ornithemc.keratin.api.maven;

public interface SingleBuildMavenArtifacts extends MavenArtifacts {

	boolean contains(String minecraftVersion);

	MavenArtifact get(String minecraftVersion);

}
