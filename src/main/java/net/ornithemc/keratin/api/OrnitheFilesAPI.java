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

	File getRavenCache();

	File getSparrowCache();

	File getNestsCache();

	File getFeatherBuildsCache();

	File getRavenBuildsCache();

	File getSparrowBuildsCache();

	File getNestsBuildsCache();

	File getEnigmaProfile();

	File getMappingsDirectory();

	File getExceptionsDirectory();

	File getSignaturesDirectory();

	File getMatchesDirectory();

	File getRunDirectory();

	File getWorkingDirectory(MinecraftVersion minecraftVersion);

	File getEnigmaSessionLock(MinecraftVersion minecraftVersion);

	File getDecompiledSourceDirectory(MinecraftVersion minecraftVersion);

	File getFakeSourceDirectory(String minecraftVersion);

	File getJavadocDirectory(String minecraftVersion);

	File getVersionsManifest();

	File getVersionInfo(String minecraftVersion);

	File getVersionDetails(String minecraftVersion);

	Collection<File> getLibraries(String minecraftVersion);

	Collection<File> getLibraries(MinecraftVersion minecraftVersion);

	File getClientJar(MinecraftVersion minecraftVersion);

	File getServerJar(MinecraftVersion minecraftVersion);

	File getMergedJar(MinecraftVersion minecraftVersion);

	File getIntermediaryClientJar(MinecraftVersion minecraftVersion);

	File getIntermediaryServerJar(MinecraftVersion minecraftVersion);

	File getIntermediaryMergedJar(MinecraftVersion minecraftVersion);

	File getMainIntermediaryJar(MinecraftVersion minecraftVersion);

	File getLvtPatchedIntermediaryClientJar(MinecraftVersion minecraftVersion);

	File getLvtPatchedIntermediaryServerJar(MinecraftVersion minecraftVersion);

	File getLvtPatchedIntermediaryMergedJar(MinecraftVersion minecraftVersion);

	File getMainLvtPatchedIntermediaryJar(MinecraftVersion minecraftVersion);

	File getExceptionsPatchedIntermediaryClientJar(MinecraftVersion minecraftVersion);

	File getExceptionsPatchedIntermediaryServerJar(MinecraftVersion minecraftVersion);

	File getExceptionsPatchedIntermediaryMergedJar(MinecraftVersion minecraftVersion);

	File getMainExceptionsPatchedIntermediaryJar(MinecraftVersion minecraftVersion);

	File getSignaturePatchedIntermediaryClientJar(MinecraftVersion minecraftVersion);

	File getSignaturePatchedIntermediaryServerJar(MinecraftVersion minecraftVersion);

	File getSignaturePatchedIntermediaryMergedJar(MinecraftVersion minecraftVersion);

	File getMainSignaturePatchedIntermediaryJar(MinecraftVersion minecraftVersion);

	File getPreenedIntermediaryClientJar(MinecraftVersion minecraftVersion);

	File getPreenedIntermediaryServerJar(MinecraftVersion minecraftVersion);

	File getPreenedIntermediaryMergedJar(MinecraftVersion minecraftVersion);

	File getMainPreenedIntermediaryJar(MinecraftVersion minecraftVersion);

	File getNestedIntermediaryClientJar(MinecraftVersion minecraftVersion);

	File getNestedIntermediaryServerJar(MinecraftVersion minecraftVersion);

	File getNestedIntermediaryMergedJar(MinecraftVersion minecraftVersion);

	File getMainNestedIntermediaryJar(MinecraftVersion minecraftVersion);

	File getProcessedIntermediaryClientJar(MinecraftVersion minecraftVersion);

	File getProcessedIntermediaryServerJar(MinecraftVersion minecraftVersion);

	File getProcessedIntermediaryMergedJar(MinecraftVersion minecraftVersion);

	File getMainProcessedIntermediaryJar(MinecraftVersion minecraftVersion);

	File getNamedJar(String minecraftVersion);

	File getProcessedNamedJar(String minecraftVersion);

	File getJavadocNamedJar(String minecraftVersion);

	File getClientIntermediaryMappings(MinecraftVersion minecraftVersion);

	File getServerIntermediaryMappings(MinecraftVersion minecraftVersion);

	File getMergedIntermediaryMappings(MinecraftVersion minecraftVersion);

	File getMainIntermediaryMappings(MinecraftVersion minecraftVersion);

	File getFilledClientIntermediaryMappings(MinecraftVersion minecraftVersion);

	File getFilledServerIntermediaryMappings(MinecraftVersion minecraftVersion);

	File getFilledMergedIntermediaryMappings(MinecraftVersion minecraftVersion);

	File getMainFilledIntermediaryMappings(MinecraftVersion minecraftVersion);

	File getNamedMappings(MinecraftVersion minecraftVersion);

	File getProcessedNamedMappings(MinecraftVersion minecraftVersion);

	File getCompletedNamedMappings(MinecraftVersion minecraftVersion);

	File getTinyV1NamedMappings(String minecraftVersion);

	File getTinyV2NamedMappings(String minecraftVersion);

	File getMergedTinyV1NamedMappings(String minecraftVersion);

	File getMergedTinyV2NamedMappings(String minecraftVersion);

	File getCompressedMergedTinyV1NamedMappings(String minecraftVersion);

	File getClientRavenFile(MinecraftVersion minecraftVersion);

	File getServerRavenFile(MinecraftVersion minecraftVersion);

	File getMergedRavenFile(MinecraftVersion minecraftVersion);

	File getIntermediaryClientRavenFile(MinecraftVersion minecraftVersion);

	File getIntermediaryServerRavenFile(MinecraftVersion minecraftVersion);

	File getIntermediaryMergedRavenFile(MinecraftVersion minecraftVersion);

	File getMainIntermediaryRavenFile(MinecraftVersion minecraftVersion);

	File getNamedRavenFile(MinecraftVersion minecraftVersion);

	File getClientSparrowFile(MinecraftVersion minecraftVersion);

	File getServerSparrowFile(MinecraftVersion minecraftVersion);

	File getMergedSparrowFile(MinecraftVersion minecraftVersion);

	File getIntermediaryClientSparrowFile(MinecraftVersion minecraftVersion);

	File getIntermediaryServerSparrowFile(MinecraftVersion minecraftVersion);

	File getIntermediaryMergedSparrowFile(MinecraftVersion minecraftVersion);

	File getMainIntermediarySparrowFile(MinecraftVersion minecraftVersion);

	File getNamedSparrowFile(MinecraftVersion minecraftVersion);

	File getClientNests(MinecraftVersion minecraftVersion);

	File getServerNests(MinecraftVersion minecraftVersion);

	File getMergedNests(MinecraftVersion minecraftVersion);

	File getIntermediaryClientNests(MinecraftVersion minecraftVersion);

	File getIntermediaryServerNests(MinecraftVersion minecraftVersion);

	File getIntermediaryMergedNests(MinecraftVersion minecraftVersion);

	File getMainIntermediaryNests(MinecraftVersion minecraftVersion);

	File getNamedNests(MinecraftVersion minecraftVersion);

	File getIntermediaryFile(String minecraftVersion);

	File getIntermediaryV2File(String minecraftVersion);

	File getFeatherMappings(String minecraftVersion);

	File getClientExceptions(MinecraftVersion minecraftVersion);

	File getServerExceptions(MinecraftVersion minecraftVersion);

	File getMergedExceptions(MinecraftVersion minecraftVersion);

	File getClientSignatures(MinecraftVersion minecraftVersion);

	File getServerSignatures(MinecraftVersion minecraftVersion);

	File getMergedSignatures(MinecraftVersion minecraftVersion);

	File getSetupClientJar(MinecraftVersion minecraftVersion);

	File getSetupServerJar(MinecraftVersion minecraftVersion);

	File getSetupMergedJar(MinecraftVersion minecraftVersion);

	File getIntermediarySetupClientJar(MinecraftVersion minecraftVersion);

	File getIntermediarySetupServerJar(MinecraftVersion minecraftVersion);

	File getIntermediarySetupMergedJar(MinecraftVersion minecraftVersion);

	File getSourceClientJar(MinecraftVersion minecraftVersion);

	File getSourceServerJar(MinecraftVersion minecraftVersion);

	File getSourceMergedJar(MinecraftVersion minecraftVersion);

	File getNamedSourceClientJar(MinecraftVersion minecraftVersion);

	File getNamedSourceServerJar(MinecraftVersion minecraftVersion);

	File getNamedSourceMergedJar(MinecraftVersion minecraftVersion);

	File getProcessedNamedSourceJar(MinecraftVersion minecraftVersion);

	File getSetupClientIntermediaryMappings(MinecraftVersion minecraftVersion);

	File getSetupServerIntermediaryMappings(MinecraftVersion minecraftVersion);

	File getSetupMergedIntermediaryMappings(MinecraftVersion minecraftVersion);

	File getSetupClientNamedMappings(MinecraftVersion minecraftVersion);

	File getSetupServerNamedMappings(MinecraftVersion minecraftVersion);

	File getSetupMergedNamedMappings(MinecraftVersion minecraftVersion);

	File getSourceClientMappings(MinecraftVersion minecraftVersion);

	File getSourceServerMappings(MinecraftVersion minecraftVersion);

	File getSourceMergedMappings(MinecraftVersion minecraftVersion);

	File getSetupClientExceptions(MinecraftVersion minecraftVersion);

	File getSetupServerExceptions(MinecraftVersion minecraftVersion);

	File getSetupMergedExceptions(MinecraftVersion minecraftVersion);

	File getSetupClientSignatures(MinecraftVersion minecraftVersion);

	File getSetupServerSignatures(MinecraftVersion minecraftVersion);

	File getSetupMergedSignatures(MinecraftVersion minecraftVersion);

	File getBaseClientExceptions(MinecraftVersion minecraftVersion);

	File getBaseServerExceptions(MinecraftVersion minecraftVersion);

	File getBaseMergedExceptions(MinecraftVersion minecraftVersion);

	File getBaseClientSignatures(MinecraftVersion minecraftVersion);

	File getBaseServerSignatures(MinecraftVersion minecraftVersion);

	File getBaseMergedSignatures(MinecraftVersion minecraftVersion);

	File getGeneratedClientJar(MinecraftVersion minecraftVersion);

	File getGeneratedServerJar(MinecraftVersion minecraftVersion);

	File getGeneratedMergedJar(MinecraftVersion minecraftVersion);

	File getNamedGeneratedClientJar(MinecraftVersion minecraftVersion);

	File getNamedGeneratedServerJar(MinecraftVersion minecraftVersion);

	File getNamedGeneratedMergedJar(MinecraftVersion minecraftVersion);

	File getGeneratedClientExceptions(MinecraftVersion minecraftVersion);

	File getGeneratedServerExceptions(MinecraftVersion minecraftVersion);

	File getGeneratedMergedExceptions(MinecraftVersion minecraftVersion);

	File getGeneratedClientSignatures(MinecraftVersion minecraftVersion);

	File getGeneratedServerSignatures(MinecraftVersion minecraftVersion);

	File getGeneratedMergedSignatures(MinecraftVersion minecraftVersion);

}
