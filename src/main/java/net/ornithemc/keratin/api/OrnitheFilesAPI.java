package net.ornithemc.keratin.api;

import java.io.File;
import java.util.List;

public interface OrnitheFilesAPI {

	File getGlobalCacheDir();

	File getLocalCacheDir();

	File getVersionJsonsDir();

	File getGameJarsDir();

	File getMappedJarsDir();

	File getProcessedJarsDir();

	File getLibrariesDir();

	File getMappingsDir();

	File getProcessedMappingsDir();

	File getNestsDir();

	File getSparrowDir();

	File getNestsBuildsCache();

	File getSparrowBuildsCache();

	File getVersionsManifest();

	File getVersionInfo(String minecraftVersion);

	File getVersionDetails(String minecraftVersion);

	List<File> getLibraries(String minecraftVersion);

	File getClientJar(String minecraftVersion);

	File getServerJar(String minecraftVersion);

	File getMergedJar(String minecraftVersion);

	File getMainJar(String minecraftVersion);

	File getIntermediaryClientJar(String minecraftVersion);

	File getIntermediaryServerJar(String minecraftVersion);

	File getIntermediaryMergedJar(String minecraftVersion);

	File getMainIntermediaryJar(String minecraftVersion);

	File getNamedClientJar(String minecraftVersion);

	File getNamedServerJar(String minecraftVersion);

	File getNamedMergedJar(String minecraftVersion);

	File getMainNamedJar(String minecraftVersion);

	File getNestedIntermediaryClientJar(String minecraftVersion);

	File getNestedIntermediaryServerJar(String minecraftVersion);

	File getNestedIntermediaryMergedJar(String minecraftVersion);

	File getMainNestedIntermediaryJar(String minecraftVersion);

	File getSignaturePatchedIntermediaryClientJar(String minecraftVersion);

	File getSignaturePatchedIntermediaryServerJar(String minecraftVersion);

	File getSignaturePatchedIntermediaryMergedJar(String minecraftVersion);

	File getMainSignaturePatchedIntermediaryJar(String minecraftVersion);

	File getProcessedIntermediaryClientJar(String minecraftVersion);

	File getProcessedIntermediaryServerJar(String minecraftVersion);

	File getProcessedIntermediaryMergedJar(String minecraftVersion);

	File getMainProcessedIntermediaryJar(String minecraftVersion);

	File getProcessedNamedClientJar(String minecraftVersion);

	File getProcessedNamedServerJar(String minecraftVersion);

	File getProcessedNamedMergedJar(String minecraftVersion);

	File getMainProcessedNamedJar(String minecraftVersion);

	File getClientJar(String minecraftVersion, String namespace);

	File getServerJar(String minecraftVersion, String namespace);

	File getMergedJar(String minecraftVersion, String namespace);

	File getMainJar(String minecraftVersion, String namespace);

	File getProcessedClientJar(String minecraftVersion, String namespace);

	File getProcessedServerJar(String minecraftVersion, String namespace);

	File getProcessedMergedJar(String minecraftVersion, String namespace);

	File getMainProcessedJar(String minecraftVersion, String namespace);

	File getClientIntermediaryMappings(String minecraftVersion);

	File getServerIntermediaryMappings(String minecraftVersion);

	File getMergedIntermediaryMappings(String minecraftVersion);

	File getClientNamedMappings(String minecraftVersion);

	File getServerNamedMappings(String minecraftVersion);

	File getMergedNamedMappings(String minecraftVersion);

	File getClientMappings(String minecraftVersion, String targetNamespace);

	File getServerMappings(String minecraftVersion, String targetNamespace);

	File getMergedMappings(String minecraftVersion, String targetNamespace);

	File getClientNests(String minecraftVersion);

	File getServerNests(String minecraftVersion);

	File getMergedNests(String minecraftVersion);

	File getIntermediaryClientNests(String minecraftVersion);

	File getIntermediaryServerNests(String minecraftVersion);

	File getIntermediaryMergedNests(String minecraftVersion);

	File getMainIntermediaryNests(String minecraftVersion);

	File getClientSparrowFile(String minecraftVersion);

	File getServerSparrowFile(String minecraftVersion);

	File getMergedSparrowFile(String minecraftVersion);

	File getIntermediaryClientSparrowFile(String minecraftVersion);

	File getIntermediaryServerSparrowFile(String minecraftVersion);

	File getIntermediaryMergedSparrowFile(String minecraftVersion);

	File getMainIntermediarySparrowFile(String minecraftVersion);

}
