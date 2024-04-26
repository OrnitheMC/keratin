package net.ornithemc.keratin.api;

import java.io.File;
import java.util.Map;

import org.gradle.api.provider.Property;

import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.manifest.VersionInfo;
import net.ornithemc.keratin.api.manifest.VersionsManifest;

public interface KeratinGradleExtensionAPI {

	Property<String> getGlobalCacheDir();

	Property<String> getLocalCacheDir();

	Property<String> getMinecraftVersion();

	Property<Integer> getIntermediaryGen();

	OrnitheFilesAPI getFiles();

	VersionsManifest getVersionsManifest();

	VersionInfo getVersionInfo(String minecraftVersion);

	VersionDetails getVersionDetails(String minecraftVersion);

	Map<GameSide, Integer> getNestsBuilds(String minecraftVersion);

	int getNestsBuild(String minecraftVersion, GameSide side);

	Map<GameSide, Integer> getSparrowBuilds(String minecraftVersion);

	int getSparrowBuild(String minecraftVersion, GameSide side);

	Property<File> getMatchesDirectory();

}
