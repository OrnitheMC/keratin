package net.ornithemc.keratin.api.files;

import java.io.File;
import java.util.Collection;

import net.ornithemc.keratin.api.MinecraftVersion;

public interface GlobalCacheAccess {

	File getDirectory();

	File getVersionsManifestJson();

	MetadataCacheAccess getMetadataCache();

	GameJarsCacheAccess getGameJarsCache();

	MappedJarsCacheAccess getMappedJarsCache();

	ProcessedJarsCacheAccess getProcessedJarsCache();

	MappingsCacheAccess getMappingsCache();

	ExceptionsCacheAccess getExceptionsCache();

	SignaturesCacheAccess getSignaturesCache();

	NestsCacheAccess getNestsCache();

	LibrariesCacheAccess getLibrariesCache();

	interface MetadataCacheAccess {

		File getDirectory();

		File getVersionInfoJson(String minecraftVersion);

		File getVersionDetailJsons(String minecraftVersion);

	}

	interface GameJarsCacheAccess {

		File getDirectory();

		File getClientJar(MinecraftVersion minecraftVersion);

		File getServerJar(MinecraftVersion minecraftVersion);

		File getMergedJar(MinecraftVersion minecraftVersion);

	}

	interface MappedJarsCacheAccess {

		File getDirectory();

		File getIntermediaryClientJar(MinecraftVersion minecraftVersion);

		File getIntermediaryServerJar(MinecraftVersion minecraftVersion);

		File getIntermediaryMergedJar(MinecraftVersion minecraftVersion);

		File getMainIntermediaryJar(MinecraftVersion minecraftVersion);

	}

	interface ProcessedJarsCacheAccess {

		File getDirectory();

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

	}

	interface MappingsCacheAccess {

		File getDirectory();

		File getClientIntermediaryMappingsJar(MinecraftVersion minecraftVersion);

		File getServerIntermediaryMappingsJar(MinecraftVersion minecraftVersion);

		File getMergedIntermediaryMappingsJar(MinecraftVersion minecraftVersion);

		File getClientIntermediaryMappingsFile(MinecraftVersion minecraftVersion);

		File getServerIntermediaryMappingsFile(MinecraftVersion minecraftVersion);

		File getMergedIntermediaryMappingsFile(MinecraftVersion minecraftVersion);

		File getMainIntermediaryMappingsFile(MinecraftVersion minecraftVersion);

		File getFilledClientIntermediaryMappingsFile(MinecraftVersion minecraftVersion);

		File getFilledServerIntermediaryMappingsFile(MinecraftVersion minecraftVersion);

		File getFilledMergedIntermediaryMappingsFile(MinecraftVersion minecraftVersion);

		File getMainFilledIntermediaryMappingsFile(MinecraftVersion minecraftVersion);

		File getNamedMappingsJar(String minecraftVersion);

		File getNamedMappingsFile(String minecraftVersion);

	}

	interface ExceptionsCacheAccess {

		File getDirectory();

		File getClientExceptionsJar(MinecraftVersion minecraftVersion);

		File getServerExceptionsJar(MinecraftVersion minecraftVersion);

		File getMergedExceptionsJar(MinecraftVersion minecraftVersion);

		File getClientExceptionsFile(MinecraftVersion minecraftVersion);

		File getServerExceptionsFile(MinecraftVersion minecraftVersion);

		File getMergedExceptionsFile(MinecraftVersion minecraftVersion);

		File getIntermediaryClientExceptionsFile(MinecraftVersion minecraftVersion);

		File getIntermediaryServerExceptionsFile(MinecraftVersion minecraftVersion);

		File getIntermediaryMergedExceptionsFile(MinecraftVersion minecraftVersion);

		File getMainIntermediaryExceptionsFile(MinecraftVersion minecraftVersion);

	}

	interface SignaturesCacheAccess {

		File getDirectory();

		File getClientSignaturesJar(MinecraftVersion minecraftVersion);

		File getServerSignaturesJar(MinecraftVersion minecraftVersion);

		File getMergedSignaturesJar(MinecraftVersion minecraftVersion);

		File getClientSignaturesFile(MinecraftVersion minecraftVersion);

		File getServerSignaturesFile(MinecraftVersion minecraftVersion);

		File getMergedSignaturesFile(MinecraftVersion minecraftVersion);

		File getIntermediaryClientSignaturesFile(MinecraftVersion minecraftVersion);

		File getIntermediaryServerSignaturesFile(MinecraftVersion minecraftVersion);

		File getIntermediaryMergedSignaturesFile(MinecraftVersion minecraftVersion);

		File getMainIntermediarySignaturesFile(MinecraftVersion minecraftVersion);

	}

	interface NestsCacheAccess {

		File getDirectory();

		File getClientNestsJar(MinecraftVersion minecraftVersion);

		File getServerNestsJar(MinecraftVersion minecraftVersion);

		File getMergedNestsJar(MinecraftVersion minecraftVersion);

		File getClientNestsFile(MinecraftVersion minecraftVersion);

		File getServerNestsFile(MinecraftVersion minecraftVersion);

		File getMergedNestsFile(MinecraftVersion minecraftVersion);

		File getIntermediaryClientNestsFile(MinecraftVersion minecraftVersion);

		File getIntermediaryServerNestsFile(MinecraftVersion minecraftVersion);

		File getIntermediaryMergedNestsFile(MinecraftVersion minecraftVersion);

		File getMainIntermediaryNestsFile(MinecraftVersion minecraftVersion);

	}

	interface LibrariesCacheAccess {

		File getDirectory();

		Collection<File> getLibraries(String minecraftVersion);

		Collection<File> getLibraries(MinecraftVersion minecraftVersion);

	}
}
