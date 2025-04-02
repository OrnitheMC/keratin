package net.ornithemc.keratin.api.maven;

import java.util.Map;

public interface MultipleBuildsMavenArtifacts {

	boolean contains(String minecraftVersion);

	boolean contains(String minecraftVersion, int build);

	Map<String, Integer> getLatestBuilds();

	int getLatestBuild(String minecraftVersion);

	MavenArtifact get(String minecraftVersion, int build);

}
