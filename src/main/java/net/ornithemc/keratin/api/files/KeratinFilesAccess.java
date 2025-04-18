package net.ornithemc.keratin.api.files;

public interface KeratinFilesAccess {

	GlobalCacheAccess getGlobalCache();

	LocalCacheAccess getLocalCache();

	SharedFilesAccess getSharedFiles();

	IntermediaryDevelopmentFilesAccess getIntermediaryDevelopmentFiles();

	MappingsDevelopmentFilesAccess getMappingsDevelopmentFiles();

	ExceptionsAndSignaturesDevelopmentFilesAccess getExceptionsAndSignaturesDevelopmentFiles();

}
