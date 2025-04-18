package net.ornithemc.keratin.api;

import org.gradle.api.Action;
import org.gradle.api.provider.Property;

import net.ornithemc.keratin.api.files.KeratinFilesAccess;
import net.ornithemc.keratin.api.manifest.VersionsManifest;
import net.ornithemc.keratin.api.maven.SingleBuildMavenArtifacts;
import net.ornithemc.keratin.api.maven.MetaSourcedMavenArtifactsAPI;
import net.ornithemc.keratin.api.maven.MultipleBuildsMavenArtifacts;

public interface KeratinGradleExtensionAPI {

	Property<String> getGlobalCacheDirectory();

	Property<String> getLocalCacheDirectory();

	Property<String> getVersionsManifestUrl();

	void minecraftVersion(String minecraftVersion);

	void minecraftVersions(String... minecraftVersions);

	Property<Integer> getIntermediaryGen();

	void tasks(TaskSelection selection) throws Exception;

	KeratinFilesAccess getFiles();

	PublicationsAPI getPublications();

	void publications(Action<PublicationsAPI> action);

	SingleBuildMavenArtifacts getIntermediaryArtifacts();

	void intermediaryArtifacts(Action<MetaSourcedMavenArtifactsAPI> action);

	MultipleBuildsMavenArtifacts getNamedMappingsArtifacts();

	void namedMappingsArtifacts(Action<MetaSourcedMavenArtifactsAPI> action);

	MultipleBuildsMavenArtifacts getExceptionsArtifacts();

	void exceptionsArtifacts(Action<MetaSourcedMavenArtifactsAPI> action);

	MultipleBuildsMavenArtifacts getSignaturesArtifacts();

	void signaturesArtifacts(Action<MetaSourcedMavenArtifactsAPI> action);

	MultipleBuildsMavenArtifacts getNestsArtifacts();

	void nestsArtifacts(Action<MetaSourcedMavenArtifactsAPI> action);

	void invalidateCache();

	VersionsManifest getVersionsManifest();

}
