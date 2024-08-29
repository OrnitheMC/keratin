package net.ornithemc.keratin.api;

import org.gradle.api.Action;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.manifest.VersionInfo;
import net.ornithemc.keratin.api.manifest.VersionsManifest;

public interface KeratinGradleExtensionAPI {

	Property<String> getGlobalCacheDirectory();

	Property<String> getLocalCacheDirectory();

	ListProperty<String> getMinecraftVersions();

	void minecraftVersions(String... minecraftVersions);

	Property<Integer> getIntermediaryGen();

	void tasks(TaskSelection selection) throws Exception;

	OrnitheFilesAPI getFiles();

	PublicationsAPI getPublications();

	void publications(Action<PublicationsAPI> action);

	VersionsManifest getVersionsManifest();

	VersionInfo getVersionInfo(String minecraftVersion);

	VersionDetails getVersionDetails(String minecraftVersion);

	int getRavenBuild(String minecraftVersion, GameSide side);

	int getSparrowBuild(String minecraftVersion, GameSide side);

	int getNestsBuild(String minecraftVersion, GameSide side);

	int getNextFeatherBuild(String minecraftVersion);

}
