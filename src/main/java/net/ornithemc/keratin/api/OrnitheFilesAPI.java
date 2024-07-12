package net.ornithemc.keratin.api;

import java.io.File;
import java.util.Collection;

public interface OrnitheFilesAPI {

	File getGlobalBuildCache();

	File getLocalBuildCache();

	File getVersionJsonsCache();

	File getGameJarsCache();

	File getMappedJarsCache();

	File getProcessedJarsCache();

	File getLibrariesCache();

	File getMappingsCache();

	File getProcessedMappingsCache();

	File getRavenCache();

	File getSparrowCache();

	File getNestsCache();

	File getRavenBuildsCache();

	File getSparrowBuildsCache();

	File getNestsBuildsCache();

	File getEnigmaProfile();

	File getMappingsDirectory();

	File getMatchesDirectory();

	File getRunDirectory();

	File getWorkingDirectory(String minecraftVersion);

	File getEnigmaSessionLock(String minecraftVersion);

	File getDecompiledSourceDirectory(String minecraftVersion);

	File getFakeSourceDirectory(String minecraftVersion);

	File getJavadocDirectory(String minecraftVersion);

	File getVersionsManifest();

	File getVersionInfo(String minecraftVersion);

	File getVersionDetails(String minecraftVersion);

	Collection<File> getLibraries(String minecraftVersion);

	File getClientJar(String minecraftVersion);

	File getServerJar(String minecraftVersion);

	File getMergedJar(String minecraftVersion);

	File getIntermediaryClientJar(String minecraftVersion);

	File getIntermediaryServerJar(String minecraftVersion);

	File getIntermediaryMergedJar(String minecraftVersion);

	File getMainIntermediaryJar(String minecraftVersion);

	File getExceptionsPatchedIntermediaryClientJar(String minecraftVersion);

	File getExceptionsPatchedIntermediaryServerJar(String minecraftVersion);

	File getExceptionsPatchedIntermediaryMergedJar(String minecraftVersion);

	File getMainExceptionsPatchedIntermediaryJar(String minecraftVersion);

	File getSignaturePatchedIntermediaryClientJar(String minecraftVersion);

	File getSignaturePatchedIntermediaryServerJar(String minecraftVersion);

	File getSignaturePatchedIntermediaryMergedJar(String minecraftVersion);

	File getMainSignaturePatchedIntermediaryJar(String minecraftVersion);

	File getNestedIntermediaryClientJar(String minecraftVersion);

	File getNestedIntermediaryServerJar(String minecraftVersion);

	File getNestedIntermediaryMergedJar(String minecraftVersion);

	File getMainNestedIntermediaryJar(String minecraftVersion);

	File getProcessedIntermediaryClientJar(String minecraftVersion);

	File getProcessedIntermediaryServerJar(String minecraftVersion);

	File getProcessedIntermediaryMergedJar(String minecraftVersion);

	File getMainProcessedIntermediaryJar(String minecraftVersion);

	File getNamedJar(String minecraftVersion);

	File getProcessedNamedJar(String minecraftVersion);

	File getClientIntermediaryMappings(String minecraftVersion);

	File getServerIntermediaryMappings(String minecraftVersion);

	File getMergedIntermediaryMappings(String minecraftVersion);

	File getNamedMappings(String minecraftVersion);

	File getProcessedNamedMappings(String minecraftVersion);

	File getCompletedNamedMappings(String minecraftVersion);

	File getTinyV1NamedMappings(String minecraftVersion);

	File getTinyV2NamedMappings(String minecraftVersion);

	File getMergedTinyV1NamedMappings(String minecraftVersion);

	File getMergedTinyV2NamedMappings(String minecraftVersion);

	File getClientRavenFile(String minecraftVersion);

	File getServerRavenFile(String minecraftVersion);

	File getMergedRavenFile(String minecraftVersion);

	File getIntermediaryClientRavenFile(String minecraftVersion);

	File getIntermediaryServerRavenFile(String minecraftVersion);

	File getIntermediaryMergedRavenFile(String minecraftVersion);

	File getMainIntermediaryRavenFile(String minecraftVersion);

	File getNamedRavenFile(String minecraftVersion);

	File getClientSparrowFile(String minecraftVersion);

	File getServerSparrowFile(String minecraftVersion);

	File getMergedSparrowFile(String minecraftVersion);

	File getIntermediaryClientSparrowFile(String minecraftVersion);

	File getIntermediaryServerSparrowFile(String minecraftVersion);

	File getIntermediaryMergedSparrowFile(String minecraftVersion);

	File getMainIntermediarySparrowFile(String minecraftVersion);

	File getNamedSparrowFile(String minecraftVersion);

	File getClientNests(String minecraftVersion);

	File getServerNests(String minecraftVersion);

	File getMergedNests(String minecraftVersion);

	File getIntermediaryClientNests(String minecraftVersion);

	File getIntermediaryServerNests(String minecraftVersion);

	File getIntermediaryMergedNests(String minecraftVersion);

	File getMainIntermediaryNests(String minecraftVersion);

	File getNamedNests(String minecraftVersion);

}
