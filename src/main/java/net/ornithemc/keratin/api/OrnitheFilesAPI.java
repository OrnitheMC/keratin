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

	File getVersionsManifest();

	File getVersionInfo();

	File getVersionDetails();

	List<File> getLibraries();

	File getNestsBuildsCache();

	File getSparrowBuildsCache();

	File getClientJar();

	File getServerJar();

	File getMergedJar();

	File getMainJar();

	File getIntermediaryClientJar();

	File getIntermediaryServerJar();

	File getIntermediaryMergedJar();

	File getMainIntermediaryJar();

	File getNamedClientJar();

	File getNamedServerJar();

	File getNamedMergedJar();

	File getMainNamedJar();

	File getNestedIntermediaryClientJar();

	File getNestedIntermediaryServerJar();

	File getNestedIntermediaryMergedJar();

	File getMainNestedIntermediaryJar();

	File getSignaturePatchedIntermediaryClientJar();

	File getSignaturePatchedIntermediaryServerJar();

	File getSignaturePatchedIntermediaryMergedJar();

	File getMainSignaturePatchedIntermediaryJar();

	File getProcessedIntermediaryClientJar();

	File getProcessedIntermediaryServerJar();

	File getProcessedIntermediaryMergedJar();

	File getMainProcessedIntermediaryJar();

	File getProcessedNamedClientJar();

	File getProcessedNamedServerJar();

	File getProcessedNamedMergedJar();

	File getMainProcessedNamedJar();

	File getClientJar(String namespace);

	File getServerJar(String namespace);

	File getMergedJar(String namespace);

	File getMainJar(String namespace);

	File getProcessedClientJar(String namespace);

	File getProcessedServerJar(String namespace);

	File getProcessedMergedJar(String namespace);

	File getMainProcessedJar(String namespace);

	File getClientIntermediaryMappings();

	File getServerIntermediaryMappings();

	File getMergedIntermediaryMappings();

	File getClientNamedMappings();

	File getServerNamedMappings();

	File getMergedNamedMappings();

	File getClientMappings(String targetNamespace);

	File getServerMappings(String targetNamespace);

	File getMergedMappings(String targetNamespace);

	File getClientNests();

	File getServerNests();

	File getMergedNests();

	File getIntermediaryClientNests();

	File getIntermediaryServerNests();

	File getIntermediaryMergedNests();

	File getMainIntermediaryNests();

	File getClientSparrowFile();

	File getServerSparrowFile();

	File getMergedSparrowFile();

	File getIntermediaryClientSparrowFile();

	File getIntermediaryServerSparrowFile();

	File getIntermediaryMergedSparrowFile();

	File getMainIntermediarySparrowFile();

}
