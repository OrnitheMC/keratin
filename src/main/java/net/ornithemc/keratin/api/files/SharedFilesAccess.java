package net.ornithemc.keratin.api.files;

import java.io.File;

import net.ornithemc.keratin.api.MinecraftVersion;

/**
 * Local files pertaining to shared functionality across projects.
 */
public interface SharedFilesAccess {

	File getMatchesDirectory();

	File getNamedMappingsBuildsJson();

	File getExceptionsBuildsJson();

	File getSignaturesBuildsJson();

	File getNestsBuildsJson();

	File getDecompiledSourceDirectory(MinecraftVersion minecraftVersion);

}
