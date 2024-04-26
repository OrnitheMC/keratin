package net.ornithemc.keratin.api;

import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

import net.ornithemc.keratin.manifest.VersionDetails;
import net.ornithemc.keratin.manifest.VersionInfo;
import net.ornithemc.keratin.manifest.VersionsManifest;

public interface KeratinGradleExtensionAPI {

	Property<String> getGlobalCacheDir();

	Property<String> getLocalCacheDir();

	Property<String> getMinecraftVersion();

	Property<Integer> getIntermediaryGen();

	OrnitheFilesAPI getFiles();

	VersionsManifest getVersionsManifest();

	VersionInfo getVersionInfo();

	VersionDetails getVersionDetails();

	MapProperty<GameSide, Integer> getNestsBuilds();

	MapProperty<GameSide, Integer> getSparrowBuilds();

}
