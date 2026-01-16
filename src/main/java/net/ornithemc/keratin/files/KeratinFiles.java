package net.ornithemc.keratin.files;

import java.io.IOException;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.TaskSelection;
import net.ornithemc.keratin.api.files.KeratinFilesAccess;

public class KeratinFiles extends FileContainer implements KeratinFilesAccess {

	private final GlobalCache globalCache;
	private final LocalCache localCache;
	private final SharedFiles sharedFiles;
	private final BuildFiles buildFiles;
	private final IntermediaryDevelopmentFiles intermediaryDevelopmentFiles;
	private final MappingsDevelopmentFiles mappingsDevelopmentFiles;
	private final ExceptionsAndSignaturesDevelopmentFiles exceptionsAndSignaturesDevelopmentFiles;

	public KeratinFiles(KeratinGradleExtension keratin) {
		super(keratin, null);

		this.globalCache = new GlobalCache(keratin, this);
		this.localCache = new LocalCache(keratin, this);
		this.sharedFiles = new SharedFiles(keratin, this);
		this.buildFiles = new BuildFiles(keratin, this);
		this.intermediaryDevelopmentFiles = new IntermediaryDevelopmentFiles(keratin, this);
		this.mappingsDevelopmentFiles = new MappingsDevelopmentFiles(keratin, this);
		this.exceptionsAndSignaturesDevelopmentFiles = new ExceptionsAndSignaturesDevelopmentFiles(keratin, this);
	}

	public void mkdirs(TaskSelection selection) throws IOException {
		globalCache.mkdirs();
		localCache.mkdirs();
		sharedFiles.mkdirs();
		buildFiles.mkdirs();

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
	public BuildFiles getBuildFiles() {
		return buildFiles;
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
