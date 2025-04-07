package net.ornithemc.keratin.api.files;

import java.io.File;

import net.ornithemc.keratin.api.MinecraftVersion;

public interface MappingsDevelopmentFilesAccess {

	File getMappingsDirectory();

	File getUnpickDirectory();

	File getUnpickJson();

	File getRunDirectory();

	File getEnigmaProfileJson();

	File getWorkingDirectory(MinecraftVersion minecraftVersion);

	File getEnigmaSessionLock(MinecraftVersion minecraftVersion);

	BuildFilesAccess getBuildFiles();

	interface BuildFilesAccess {

		File getMappingsFile(MinecraftVersion minecraftVersion);

		File getProcessedMappingsFile(MinecraftVersion minecraftVersion);

		File getCompletedMappingsFile(MinecraftVersion minecraftVersion);

		File getNamedNestsFile(MinecraftVersion minecraftVersion);
	
		File getProcessedUnpickDefinitionsFile(MinecraftVersion minecraftVersion);

		File getProcessedIntermediaryUnpickDefinitionsFile(MinecraftVersion minecraftVersion);

		File getUnpickedProcessedIntermediaryJar(MinecraftVersion minecraftVersion);

		File getTinyV1MappingsFile(String minecraftVersion);

		File getTinyV2MappingsFile(String minecraftVersion);

		File getMergedTinyV1MappingsFile(String minecraftVersion);

		File getMergedTinyV2MappingsFile(String minecraftVersion);

		File getCompressedMergedTinyV1MappingsFile(String minecraftVersion);

		File getUnpickDefinitionsFile(String minecraftVersion);

		File getNamedJar(String minecraftVersion);

		File getProcessedNamedJar(String minecraftVersion);

		File getJavadocNamedJar(String minecraftVersion);

		File getFakeSourceDirectory(String minecraftVersion);

		File getJavadocDirectory(String minecraftVersion);

	}
}
