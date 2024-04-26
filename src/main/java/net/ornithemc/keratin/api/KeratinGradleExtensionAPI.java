package net.ornithemc.keratin.api;

import java.util.Map;

import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;

import net.ornithemc.keratin.manifest.VersionDetails;
import net.ornithemc.keratin.manifest.VersionInfo;
import net.ornithemc.keratin.manifest.VersionsManifest;

public interface KeratinGradleExtensionAPI {

	Property<String> getGlobalCacheDir();

	Property<String> getLocalCacheDir();

	Property<String> getMainMinecraftVersion();

	SetProperty<String> getMinecraftVersions();

	Property<Integer> getIntermediaryGen();

	OrnitheFilesAPI getFiles();

	VersionsManifest getVersionsManifest();

	VersionInfo getMainVersionInfo();

	VersionInfo getVersionInfo(String minecraftVersion);

	VersionDetails getMainVersionDetails();

	VersionDetails getVersionDetails(String minecraftVersion);

	Map<GameSide, Integer> getNestsBuilds(String minecraftVersion);

	int getNestsBuild(String minecraftVersion, GameSide side);

	Map<GameSide, Integer> getSparrowBuilds(String minecraftVersion);

	int getSparrowBuild(String minecraftVersion, GameSide side);

}
