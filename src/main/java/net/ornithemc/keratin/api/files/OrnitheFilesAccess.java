package net.ornithemc.keratin.api.files;

public interface OrnitheFilesAccess {

	GlobalCacheAccess getGlobalCache();

	LocalCacheAccess getLocalCache();

	SharedFilesAccess getSharedFiles();

	IntermediaryDevelopmentFilesAccess getIntermediaryDevelopmentFiles();

	MappingsDevelopmentFilesAccess getMappingsDevelopmentFiles();

	ExceptionsAndSignaturesDevelopmentFilesAccess getExceptionsAndSignaturesDevelopmentFiles();

}
