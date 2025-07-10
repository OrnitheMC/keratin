package net.ornithemc.keratin.api;

import org.gradle.api.Action;
import org.gradle.api.provider.Property;

import net.ornithemc.keratin.api.files.KeratinFilesAccess;
import net.ornithemc.keratin.api.manifest.VersionsManifest;
import net.ornithemc.keratin.api.maven.MavenSourcedMavenArtifacts;
import net.ornithemc.keratin.api.maven.MetaSourcedMavenArtifacts;
import net.ornithemc.keratin.api.maven.MultipleBuildsMavenArtifacts;
import net.ornithemc.keratin.api.maven.SingleBuildMavenArtifacts;

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

	void mavenSourcedIntermediaryArtifacts(Action<MavenSourcedMavenArtifacts> action);

	void metaSourcedIntermediaryArtifacts(Action<MetaSourcedMavenArtifacts> action);

	<T extends SingleBuildMavenArtifacts> void intermediaryArtifacts(Class<T> type, Action<T> action);

	void mavenSourcedNamedMappingsArtifacts(Action<MavenSourcedMavenArtifacts> action);

	void metaSourcedNamedMappingsArtifacts(Action<MetaSourcedMavenArtifacts> action);

	<T extends MultipleBuildsMavenArtifacts> void namedMappingsArtifacts(Class<T> type, Action<T> action);

	void mavenSourcedExceptionsArtifacts(Action<MavenSourcedMavenArtifacts> action);

	void metaSourcedExceptionsArtifacts(Action<MetaSourcedMavenArtifacts> action);

	<T extends MultipleBuildsMavenArtifacts> void exceptionsArtifacts(Class<T> type, Action<T> action);

	void mavenSourcedSignaturesArtifacts(Action<MavenSourcedMavenArtifacts> action);

	void metaSourcedSignaturesArtifacts(Action<MetaSourcedMavenArtifacts> action);

	<T extends MultipleBuildsMavenArtifacts> void signaturesArtifacts(Class<T> type, Action<T> action);

	void mavenSourcedNestsArtifacts(Action<MavenSourcedMavenArtifacts> action);

	void metaSourcedNestsArtifacts(Action<MetaSourcedMavenArtifacts> action);

	<T extends MultipleBuildsMavenArtifacts> void nestsArtifacts(Class<T> type, Action<T> action);

	void invalidateCache();

	VersionsManifest getVersionsManifest();

}
