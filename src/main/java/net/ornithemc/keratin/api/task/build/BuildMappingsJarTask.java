package net.ornithemc.keratin.api.task.build;

import java.io.File;
import java.util.Map;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.tasks.bundling.Jar;

import net.ornithemc.keratin.KeratinGradleExtension;

public abstract class BuildMappingsJarTask extends Jar {

	@Inject
	public BuildMappingsJarTask() {
		Project project = getProject();
		KeratinGradleExtension keratin = KeratinGradleExtension.get(project);

		getDestinationDirectory().set(project.file("build/libs"));

		manifest(manifest -> {
			manifest.attributes(Map.of(
				"Minecraft-Version", keratin.getMinecraftVersion().get(),
				"Calamus-Generation", keratin.getIntermediaryGen().get()
			));
		});
	}

	public void mappings(File mappings) {
		from(mappings, copy -> {
			copy.into("mappings");
			copy.rename(mappings.getName(), "mappings.tiny");
		});
	}

}
