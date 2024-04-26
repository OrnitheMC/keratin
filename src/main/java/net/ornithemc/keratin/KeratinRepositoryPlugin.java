package net.ornithemc.keratin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.plugins.PluginAware;

public class KeratinRepositoryPlugin implements Plugin<PluginAware> {

	@Override
	public void apply(PluginAware pluginAware) {
		if (pluginAware instanceof Settings settings) {
			declareRepositories(settings.getDependencyResolutionManagement().getRepositories());

			settings.getGradle().getPluginManager().apply(KeratinRepositoryPlugin.class);
		} else if (pluginAware instanceof Project project) {
			if (project.getGradle().getPlugins().hasPlugin(KeratinRepositoryPlugin.class)) {
				return;
			}

			declareRepositories(project.getRepositories());
		} else if (pluginAware instanceof Gradle) {
			return;
		} else {
			throw new IllegalArgumentException("Expected target to be a Project or Settings, but was a " + pluginAware.getClass());
		}
	}

	private void declareRepositories(RepositoryHandler repositories) {
		repositories.maven(repo -> {
			repo.setName(Constants.MAVEN_NAME);
			repo.setUrl(Constants.MAVEN_URL);
		});
		repositories.maven(repo -> {
			repo.setName(Constants.FABRIC_MAVEN_NAME);
			repo.setUrl(Constants.FABRIC_MAVEN_URL);
		});
		repositories.maven(repo -> {
			repo.setName(Constants.VINEFLOWER_SNAPSHOTS_MAVEN_NAME);
			repo.setUrl(Constants.VINEFLOWER_SNAPSHOTS_MAVEN_URL);
		});
	}
}
