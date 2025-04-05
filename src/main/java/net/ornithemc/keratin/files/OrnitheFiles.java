package net.ornithemc.keratin.files;

import java.io.IOException;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.TaskSelection;
import net.ornithemc.keratin.api.files.OrnitheFilesAccess;

public class OrnitheFiles extends FileContainer implements OrnitheFilesAccess {

	private final GlobalCache globalCache;
	private final LocalCache localCache;
	private final SharedFiles sharedFiles;
	private final IntermediaryDevelopmentFiles intermediaryDevelopmentFiles;
	private final MappingsDevelopmentFiles mappingsDevelopmentFiles;
	private final ExceptionsAndSignaturesDevelopmentFiles exceptionsAndSignaturesDevelopmentFiles;

	public OrnitheFiles(KeratinGradleExtension keratin) {
		super(keratin);

		this.globalCache = new GlobalCache(keratin);
		this.localCache = new LocalCache(keratin);
		this.sharedFiles = new SharedFiles(keratin);
		this.intermediaryDevelopmentFiles = new IntermediaryDevelopmentFiles(keratin, this.localCache);
		this.mappingsDevelopmentFiles = new MappingsDevelopmentFiles(keratin, this.localCache);
		this.exceptionsAndSignaturesDevelopmentFiles = new ExceptionsAndSignaturesDevelopmentFiles(keratin, this.localCache);
	}

	public void mkdirs(TaskSelection selection) throws IOException {
		globalCache.mkdirs();
		localCache.mkdirs();
		sharedFiles.mkdirs();

		if (selection == TaskSelection.INTERMEDIARY) {
			intermediaryDevelopmentFiles.mkdirs();
		}
		if (selection == TaskSelection.MAPPINGS) {
			mappingsDevelopmentFiles.mkdirs();
		}
		if (selection == TaskSelection.EXCEPTIONS_AND_SIGNATURES) {
			exceptionsAndSignaturesDevelopmentFiles.mkdirs();
		}
	}

	@Override
	public GlobalCache getGlobalCache() {
		return globalCache;
	}

	@Override
	public LocalCache getLocalCache() {
		return localCache;
	}

	@Override
	public SharedFiles getSharedFiles() {
		return sharedFiles;
	}

	@Override
	public IntermediaryDevelopmentFiles getIntermediaryDevelopmentFiles() {
		return intermediaryDevelopmentFiles;
	}

	@Override
	public MappingsDevelopmentFiles getMappingsDevelopmentFiles() {
		return mappingsDevelopmentFiles;
	}

	@Override
	public ExceptionsAndSignaturesDevelopmentFiles getExceptionsAndSignaturesDevelopmentFiles() {
		return exceptionsAndSignaturesDevelopmentFiles;
	}
}
