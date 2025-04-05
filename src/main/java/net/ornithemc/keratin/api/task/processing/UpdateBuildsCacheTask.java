package net.ornithemc.keratin.api.task.processing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.gradle.api.tasks.TaskAction;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.maven.MultipleBuildsMavenArtifacts;
import net.ornithemc.keratin.api.task.KeratinTask;
import net.ornithemc.keratin.files.OrnitheFiles;
import net.ornithemc.keratin.files.SharedFiles;

public abstract class UpdateBuildsCacheTask extends KeratinTask {

	protected void updateCacheFile(File cacheFile, Map<String, Integer> latestBuilds) throws IOException {
		getProject().getLogger().lifecycle(":updating builds cache " + cacheFile.getName());

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(cacheFile))) {
			KeratinGradleExtension.GSON.toJson(latestBuilds, bw);
		}
	}

	public static abstract class NamedMappings extends UpdateBuildsCacheTask {

		@TaskAction
		public void run() throws IOException {
			KeratinGradleExtension keratin = getExtension();
			OrnitheFiles files = keratin.getFiles();
			MultipleBuildsMavenArtifacts namedMappings = keratin.getNamedMappingsArtifacts();

			SharedFiles sharedFiles = files.getSharedFiles();

			updateCacheFile(
				sharedFiles.getNamedMappingsBuildsJson(),
				namedMappings.getLatestBuilds()
			);
		}
	}

	public static abstract class Exceptions extends UpdateBuildsCacheTask {

		@TaskAction
		public void run() throws IOException {
			KeratinGradleExtension keratin = getExtension();
			OrnitheFiles files = keratin.getFiles();
			MultipleBuildsMavenArtifacts exceptions = keratin.getExceptionsArtifacts();

			SharedFiles sharedFiles = files.getSharedFiles();

			updateCacheFile(
				sharedFiles.getExceptionsBuildsJson(),
				exceptions.getLatestBuilds()
			);
		}
	}

	public static abstract class Signatures extends UpdateBuildsCacheTask {

		@TaskAction
		public void run() throws IOException {
			KeratinGradleExtension keratin = getExtension();
			OrnitheFiles files = keratin.getFiles();
			MultipleBuildsMavenArtifacts signatures = keratin.getSignaturesArtifacts();

			SharedFiles sharedFiles = files.getSharedFiles();

			updateCacheFile(
				sharedFiles.getSignaturesBuildsJson(),
				signatures.getLatestBuilds()
			);
		}
	}

	public static abstract class Nests extends UpdateBuildsCacheTask {

		@TaskAction
		public void run() throws IOException {
			KeratinGradleExtension keratin = getExtension();
			OrnitheFiles files = keratin.getFiles();
			MultipleBuildsMavenArtifacts nests = keratin.getNestsArtifacts();

			SharedFiles sharedFiles = files.getSharedFiles();

			updateCacheFile(
				sharedFiles.getNestsBuildsJson(),
				nests.getLatestBuilds()
			);
		}
	}
}
