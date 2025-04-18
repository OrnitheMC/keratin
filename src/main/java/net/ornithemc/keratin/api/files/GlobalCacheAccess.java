package net.ornithemc.keratin.api.files;

import java.io.File;
import java.util.Collection;

import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.settings.BuildNumbers;
import net.ornithemc.keratin.api.settings.ProcessorSettings;

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

		File getVersionDetailJson(String minecraftVersion);

	}

	interface GameJarsCacheAccess {

		File getDirectory();

		File getClientJar(MinecraftVersion minecraftVersion);

		File getServerJar(MinecraftVersion minecraftVersion);

		File getServerZip(MinecraftVersion minecraftVersion);

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

		File getProcessedIntermediaryJar(MinecraftVersion minecraftVersion, ProcessorSettings processorSettings);

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

		File getNamedMappingsJar(String minecraftVersion, int build);

		File getNamedMappingsFile(String minecraftVersion, int build);

	}

	interface ExceptionsCacheAccess {

		File getDirectory();

		File getClientExceptionsJar(MinecraftVersion minecraftVersion, BuildNumbers builds);

		File getServerExceptionsJar(MinecraftVersion minecraftVersion, BuildNumbers builds);

		File getMergedExceptionsJar(MinecraftVersion minecraftVersion, BuildNumbers builds);

		File getClientExceptionsFile(MinecraftVersion minecraftVersion, BuildNumbers builds);

		File getServerExceptionsFile(MinecraftVersion minecraftVersion, BuildNumbers builds);

		File getMergedExceptionsFile(MinecraftVersion minecraftVersion, BuildNumbers builds);

		File getIntermediaryClientExceptionsFile(MinecraftVersion minecraftVersion, BuildNumbers builds);

		File getIntermediaryServerExceptionsFile(MinecraftVersion minecraftVersion, BuildNumbers builds);

		File getIntermediaryMergedExceptionsFile(MinecraftVersion minecraftVersion, BuildNumbers builds);

		File getMainIntermediaryExceptionsFile(MinecraftVersion minecraftVersion, BuildNumbers builds);

	}

	interface SignaturesCacheAccess {

		File getDirectory();

		File getClientSignaturesJar(MinecraftVersion minecraftVersion, BuildNumbers builds);

		File getServerSignaturesJar(MinecraftVersion minecraftVersion, BuildNumbers builds);

		File getMergedSignaturesJar(MinecraftVersion minecraftVersion, BuildNumbers builds);

		File getClientSignaturesFile(MinecraftVersion minecraftVersion, BuildNumbers builds);

		File getServerSignaturesFile(MinecraftVersion minecraftVersion, BuildNumbers builds);

		File getMergedSignaturesFile(MinecraftVersion minecraftVersion, BuildNumbers builds);

		File getIntermediaryClientSignaturesFile(MinecraftVersion minecraftVersion, BuildNumbers builds);

		File getIntermediaryServerSignaturesFile(MinecraftVersion minecraftVersion, BuildNumbers builds);

		File getIntermediaryMergedSignaturesFile(MinecraftVersion minecraftVersion, BuildNumbers builds);

		File getMainIntermediarySignaturesFile(MinecraftVersion minecraftVersion, BuildNumbers builds);

	}

	interface NestsCacheAccess {

		File getDirectory();

		File getClientNestsJar(MinecraftVersion minecraftVersion, BuildNumbers builds);

		File getServerNestsJar(MinecraftVersion minecraftVersion, BuildNumbers builds);

		File getMergedNestsJar(MinecraftVersion minecraftVersion, BuildNumbers builds);

		File getClientNestsFile(MinecraftVersion minecraftVersion, BuildNumbers builds);

		File getServerNestsFile(MinecraftVersion minecraftVersion, BuildNumbers builds);

		File getMergedNestsFile(MinecraftVersion minecraftVersion, BuildNumbers builds);

		File getIntermediaryClientNestsFile(MinecraftVersion minecraftVersion, BuildNumbers builds);

		File getIntermediaryServerNestsFile(MinecraftVersion minecraftVersion, BuildNumbers builds);

		File getIntermediaryMergedNestsFile(MinecraftVersion minecraftVersion, BuildNumbers builds);

		File getMainIntermediaryNestsFile(MinecraftVersion minecraftVersion, BuildNumbers builds);

	}

	interface LibrariesCacheAccess {

		Collection<File> getLibraries(String minecraftVersion);

		Collection<File> getLibraries(MinecraftVersion minecraftVersion);

	}
}
