package net.ornithemc.keratin.api.files;

import java.io.File;

public interface IntermediaryDevelopmentFilesAccess {

	File getMappingsDirectory();

	File getTinyV1MappingsFile(String minecraftVersion);

	File getTinyV2MappingsFile(String minecraftVersion);

}
