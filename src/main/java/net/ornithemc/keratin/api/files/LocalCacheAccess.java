package net.ornithemc.keratin.api.files;

import java.io.File;

public interface LocalCacheAccess {

	File getDirectory();

	File getOldNamedMappingsBuildsJson();

	File getOldExceptionsBuildsJson();

	File getOldSignaturesBuildsJson();

	File getOldNestsBuildsJson();

}
