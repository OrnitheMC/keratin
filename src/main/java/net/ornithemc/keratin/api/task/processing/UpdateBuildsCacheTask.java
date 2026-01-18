package net.ornithemc.keratin.api.task.processing;

import java.io.IOException;

import org.gradle.api.tasks.TaskAction;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.maven.MultipleBuildsMavenArtifacts;
import net.ornithemc.keratin.api.task.KeratinTask;
import net.ornithemc.keratin.cache.BuildNumbersCache;

public abstract class UpdateBuildsCacheTask extends KeratinTask {

	void updateCache(BuildNumbersCache cache, MultipleBuildsMavenArtifacts artifacts) throws IOException {
		getProject().getLogger().lifecycle(":updating builds cache " + cache.getFile().getName());

		cache.backUp();
		cache.update(artifacts.getLatestBuilds());
	}

	public static abstract class NamedMappings extends UpdateBuildsCacheTask {

		@TaskAction
		public void run() throws IOException {
			KeratinGradleExtension keratin = getExtension();

			updateCache(
				keratin.getNamedMappingsBuilds(),
				keratin.getNamedMappingsArtifacts()
			);
		}
	}

	public static abstract class Exceptions extends UpdateBuildsCacheTask {

		@TaskAction
		public void run() throws IOException {
			KeratinGradleExtension keratin = getExtension();

			updateCache(
				keratin.getExceptionsBuilds(),
				keratin.getExceptionsArtifacts()
			);
		}
	}

	public static abstract class Signatures extends UpdateBuildsCacheTask {

		@TaskAction
		public void run() throws IOException {
			KeratinGradleExtension keratin = getExtension();

			updateCache(
				keratin.getSignaturesBuilds(),
				keratin.getSignaturesArtifacts()
			);
		}
	}

	public static abstract class Nests extends UpdateBuildsCacheTask {

		@TaskAction
		public void run() throws IOException {
			KeratinGradleExtension keratin = getExtension();

			updateCache(
				keratin.getNestsBuilds(),
				keratin.getNestsArtifacts()
			);
		}
	}
}
