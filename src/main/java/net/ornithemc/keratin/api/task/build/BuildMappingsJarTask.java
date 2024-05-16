package net.ornithemc.keratin.api.task.build;

import java.io.File;
import java.util.Map;

import org.gradle.api.Project;
import org.gradle.api.tasks.bundling.Jar;

import net.ornithemc.keratin.KeratinGradleExtension;

public abstract class BuildMappingsJarTask extends Jar {

	public void configure(String minecraftVersion, File mappings, String archiveFileNameFormat) {
		Project project = getProject();
		KeratinGradleExtension keratin = KeratinGradleExtension.get(project);

		from(mappings, copy -> {
			copy.into("mappings");
			copy.rename(mappings.getName(), "mappings.tiny");
		});

		getDestinationDirectory().set(project.file("build/libs"));
		if (archiveFileNameFormat != null) {
			getArchiveFileName().set(archiveFileNameFormat.formatted(minecraftVersion));
		}
		manifest(manifest -> {
			manifest.attributes(Map.of(
				"Minecraft-Version", minecraftVersion,
				"Calamus-Generation", keratin.getIntermediaryGen().get()
			));
		});
	}
}
