package net.ornithemc.keratin.api.files;

import java.io.File;

public interface LocalCacheAccess {

	File getDirectory();

	File getNamedMappingsBuildsJsonBackup();

	File getExceptionsBuildsJsonBackup();

	File getSignaturesBuildsJsonBackup();

	File getNestsBuildsJsonBackup();

}
