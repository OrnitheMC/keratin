package net.ornithemc.keratin.api.maven;

public interface SingleBuildMavenArtifacts {

	boolean contains(String minecraftVersion);

	MavenArtifact get(String minecraftVersion);

}
