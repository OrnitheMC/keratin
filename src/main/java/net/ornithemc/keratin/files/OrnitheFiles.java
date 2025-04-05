package net.ornithemc.keratin.files;

import java.io.IOException;

import org.gradle.api.provider.Property;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.TaskSelection;
import net.ornithemc.keratin.api.files.OrnitheFilesAccess;

public class OrnitheFiles extends FileContainer implements OrnitheFilesAccess {

	private final Property<GlobalCache> globalCache;
	private final Property<LocalCache> localCache;
	private final Property<SharedFiles> sharedFiles;
	private final Property<IntermediaryDevelopmentFiles> intermediaryDevelopmentFiles;
	private final Property<MappingsDevelopmentFiles> mappingsDevelopmentFiles;
	private final Property<ExceptionsAndSignaturesDevelopmentFiles> exceptionsAndSignaturesDevelopmentFiles;

	public OrnitheFiles(KeratinGradleExtension keratin) {
		super(keratin, null);

		this.globalCache = property(GlobalCache.class, () -> new GlobalCache(keratin, this));
		this.localCache = property(LocalCache.class, () -> new LocalCache(keratin, this));
		this.sharedFiles = property(SharedFiles.class, () -> new SharedFiles(keratin, this));
		this.intermediaryDevelopmentFiles = property(IntermediaryDevelopmentFiles.class, () -> new IntermediaryDevelopmentFiles(keratin, this));
		this.mappingsDevelopmentFiles = property(MappingsDevelopmentFiles.class, () -> new MappingsDevelopmentFiles(keratin, this));
		this.exceptionsAndSignaturesDevelopmentFiles = property(ExceptionsAndSignaturesDevelopmentFiles.class, () -> new ExceptionsAndSignaturesDevelopmentFiles(keratin, this));
	}

	public void mkdirs(TaskSelection selection) throws IOException {
		getGlobalCache().mkdirs();
		getLocalCache().mkdirs();
		getSharedFiles().mkdirs();

		if (selection == TaskSelection.INTERMEDIARY) {
			getIntermediaryDevelopmentFiles().mkdirs();
		}
		if (selection == TaskSelection.MAPPINGS) {
			getMappingsDevelopmentFiles().mkdirs();
		}
		if (selection == TaskSelection.EXCEPTIONS_AND_SIGNATURES) {
			getExceptionsAndSignaturesDevelopmentFiles().mkdirs();
		}
	}

	@Override
	public GlobalCache getGlobalCache() {
		return globalCache.get();
	}

	@Override
	public LocalCache getLocalCache() {
		return localCache.get();
	}

	@Override
	public SharedFiles getSharedFiles() {
		return sharedFiles.get();
	}

	@Override
	public IntermediaryDevelopmentFiles getIntermediaryDevelopmentFiles() {
		return intermediaryDevelopmentFiles.get();
	}

	@Override
	public MappingsDevelopmentFiles getMappingsDevelopmentFiles() {
		return mappingsDevelopmentFiles.get();
	}

	@Override
	public ExceptionsAndSignaturesDevelopmentFiles getExceptionsAndSignaturesDevelopmentFiles() {
		return exceptionsAndSignaturesDevelopmentFiles.get();
	}
}
