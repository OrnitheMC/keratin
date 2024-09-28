package net.ornithemc.keratin.api;

import org.gradle.api.Action;
import org.gradle.api.provider.Property;

import net.ornithemc.keratin.api.manifest.VersionsManifest;

public interface KeratinGradleExtensionAPI {

	Property<String> getGlobalCacheDirectory();

	Property<String> getLocalCacheDirectory();

	void minecraftVersions(String... minecraftVersions);

	Property<Integer> getIntermediaryGen();

	void tasks(TaskSelection selection) throws Exception;

	OrnitheFilesAPI getFiles();

	PublicationsAPI getPublications();

	void publications(Action<PublicationsAPI> action);

	VersionsManifest getVersionsManifest();

	int getFeatherBuild(String minecraftVersion);

	int getRavenBuild(MinecraftVersion minecraftVersion, GameSide side);

	int getSparrowBuild(MinecraftVersion minecraftVersion, GameSide side);

	int getNestsBuild(MinecraftVersion minecraftVersion, GameSide side);

}
