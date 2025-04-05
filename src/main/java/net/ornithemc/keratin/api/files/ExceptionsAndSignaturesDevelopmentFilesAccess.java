package net.ornithemc.keratin.api.files;

import java.io.File;

import net.ornithemc.keratin.api.MinecraftVersion;

public interface ExceptionsAndSignaturesDevelopmentFilesAccess {

	File getExceptionsDirectory();

	File getSignaturesDirectory();

	File getClientExceptionsFile(MinecraftVersion minecraftVersion);

	File getServerExceptionsFile(MinecraftVersion minecraftVersion);

	File getMergedExceptionsFile(MinecraftVersion minecraftVersion);

	File getClientSignaturesFile(MinecraftVersion minecraftVersion);

	File getServerSignaturesFile(MinecraftVersion minecraftVersion);

	File getMergedSignaturesFile(MinecraftVersion minecraftVersion);

	SetupJarsAccess getSetupJars();

	SetupFilesAccess getSetupFiles();

	SourceJarsAccess getSourceJars();

	SourceMappingsAccess getSourceMappings();

	BuildFilesAccess getBuildFiles();

	interface SetupJarsAccess {

		File getClientJar(MinecraftVersion minecraftVersion);

		File getServerJar(MinecraftVersion minecraftVersion);

		File getMergedJar(MinecraftVersion minecraftVersion);

		File getIntermediaryClientJar(MinecraftVersion minecraftVersion);

		File getIntermediaryServerJar(MinecraftVersion minecraftVersion);

		File getIntermediaryMergedJar(MinecraftVersion minecraftVersion);

	}

	interface SetupFilesAccess {

		File getClientIntermediaryMappingsFile(MinecraftVersion minecraftVersion);

		File getServerIntermediaryMappingsFile(MinecraftVersion minecraftVersion);

		File getMergedIntermediaryMappingsFile(MinecraftVersion minecraftVersion);

		File getClientNamedMappingsFile(MinecraftVersion minecraftVersion);

		File getServerNamedMappingsFile(MinecraftVersion minecraftVersion);

		File getMergedNamedMappingsFile(MinecraftVersion minecraftVersion);

		File getClientExceptionsFile(MinecraftVersion minecraftVersion);

		File getServerExceptionsFile(MinecraftVersion minecraftVersion);

		File getMergedExceptionsFile(MinecraftVersion minecraftVersion);

		File getClientSignaturesFile(MinecraftVersion minecraftVersion);

		File getServerSignaturesFile(MinecraftVersion minecraftVersion);

		File getMergedSignaturesFile(MinecraftVersion minecraftVersion);

	}

	interface SourceJarsAccess {

		File getClientJar(MinecraftVersion minecraftVersion);

		File getServerJar(MinecraftVersion minecraftVersion);

		File getMergedJar(MinecraftVersion minecraftVersion);

		File getNamedClientJar(MinecraftVersion minecraftVersion);

		File getNamedServerJar(MinecraftVersion minecraftVersion);

		File getNamedMergedJar(MinecraftVersion minecraftVersion);

		File getProcessedNamedJar(MinecraftVersion minecraftVersion);

	}

	interface SourceMappingsAccess {

		File getClientMappingsFile(MinecraftVersion minecraftVersion);

		File getServerMappingsFile(MinecraftVersion minecraftVersion);

		File getMergedMappingsFile(MinecraftVersion minecraftVersion);

	}

	interface BuildFilesAccess {

		File getBaseClientExceptionsFile(MinecraftVersion minecraftVersion);

		File getBaseServerExceptionsFile(MinecraftVersion minecraftVersion);

		File getBaseMergedExceptionsFile(MinecraftVersion minecraftVersion);

		File getBaseClientSignaturesFile(MinecraftVersion minecraftVersion);

		File getBaseServerSignaturesFile(MinecraftVersion minecraftVersion);

		File getBaseMergedSignaturesFile(MinecraftVersion minecraftVersion);

		File getGeneratedClientJar(MinecraftVersion minecraftVersion);

		File getGeneratedServerJar(MinecraftVersion minecraftVersion);

		File getGeneratedMergedJar(MinecraftVersion minecraftVersion);

		File getNamedGeneratedClientJar(MinecraftVersion minecraftVersion);

		File getNamedGeneratedServerJar(MinecraftVersion minecraftVersion);

		File getNamedGeneratedMergedJar(MinecraftVersion minecraftVersion);

		File getGeneratedClientExceptionsFile(MinecraftVersion minecraftVersion);

		File getGeneratedServerExceptionsFile(MinecraftVersion minecraftVersion);

		File getGeneratedMergedExceptionsFile(MinecraftVersion minecraftVersion);

		File getGeneratedClientSignaturesFile(MinecraftVersion minecraftVersion);

		File getGeneratedServerSignaturesFile(MinecraftVersion minecraftVersion);

		File getGeneratedMergedSignaturesFile(MinecraftVersion minecraftVersion);

	}
}
