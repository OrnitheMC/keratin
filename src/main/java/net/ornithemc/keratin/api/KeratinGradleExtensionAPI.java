package net.ornithemc.keratin.api;

import java.io.File;

import org.gradle.api.provider.Property;

import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.manifest.VersionInfo;
import net.ornithemc.keratin.api.manifest.VersionsManifest;

public interface KeratinGradleExtensionAPI {

	Property<String> getGlobalCacheDir();

	Property<String> getLocalCacheDir();

	Property<String> getMinecraftVersion();

	Property<Integer> getIntermediaryGen();

	void tasks(TaskSelection selection) throws Exception;

	OrnitheFilesAPI getFiles();

	VersionsManifest getVersionsManifest();

	VersionInfo getVersionInfo();

	VersionInfo getVersionInfo(String minecraftVersion);

	VersionDetails getVersionDetails();

	VersionDetails getVersionDetails(String minecraftVersion);

	int getNestsBuild(String minecraftVersion, GameSide side);

	int getSparrowBuild(String minecraftVersion, GameSide side);

	Property<File> getMatchesDirectory();

}
