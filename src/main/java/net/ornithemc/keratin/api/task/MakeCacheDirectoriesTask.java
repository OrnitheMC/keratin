package net.ornithemc.keratin.api.task;

import org.gradle.api.tasks.TaskAction;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;

public class MakeCacheDirectoriesTask extends KeratinTask {

	@TaskAction
	public void run() {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		if (!files.getGlobalBuildCache().exists())
			files.getGlobalBuildCache().mkdirs();
		if (!files.getLocalBuildCache().exists())
			files.getLocalBuildCache().mkdirs();
		if (!files.getVersionJsonsCache().exists())
			files.getVersionJsonsCache().mkdirs();
		if (!files.getGameJarsCache().exists())
			files.getGameJarsCache().mkdirs();
		if (!files.getMappedJarsCache().exists())
			files.getMappedJarsCache().mkdirs();
		if (!files.getProcessedJarsCache().exists())
			files.getProcessedJarsCache().mkdirs();
		if (!files.getLibrariesCache().exists())
			files.getLibrariesCache().mkdirs();
		if (!files.getMappingsCache().exists())
			files.getMappingsCache().mkdirs();
		if (!files.getProcessedMappingsCache().exists())
			files.getProcessedMappingsCache().mkdirs();
		if (!files.getNestsCache().exists())
			files.getNestsCache().mkdirs();
		if (!files.getSparrowCache().exists())
			files.getSparrowCache().mkdirs();

		if (!files.getMappingsDirectory().exists())
			files.getMappingsDirectory().mkdirs();
	}
}
