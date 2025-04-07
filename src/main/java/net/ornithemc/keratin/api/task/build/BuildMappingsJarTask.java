package net.ornithemc.keratin.api.task.build;

import java.io.File;
import java.util.Map;

import org.gradle.api.Project;
import org.gradle.api.tasks.bundling.Jar;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles;
import net.ornithemc.keratin.files.OrnitheFiles;

public abstract class BuildMappingsJarTask extends Jar {

	public void configure(String minecraftVersion, File mappings, String archiveFileNameFormat) {
		configure(minecraftVersion, mappings, null, archiveFileNameFormat);
	}

	public void configure(String minecraftVersion, File mappings, File unpickDefinitions, String archiveFileNameFormat) {
		Project project = getProject();
		KeratinGradleExtension keratin = KeratinGradleExtension.get(project);

		from(mappings, copy -> {
			copy.into("mappings");
			copy.rename(mappings.getName(), "mappings.tiny");
		});
		if (unpickDefinitions != null) {
			OrnitheFiles files = keratin.getFiles();
			MappingsDevelopmentFiles mappingsFiles = files.getMappingsDevelopmentFiles();

			File unpickJson = mappingsFiles.getUnpickJson();

			from(unpickJson, copy ->{
				copy.into("extras");
				copy.rename(unpickJson.getName(), "unpick.json");
			});
			from(unpickDefinitions, copy ->{
				copy.into("extras");
				copy.rename(unpickDefinitions.getName(), "definitions.unpick");
			});
		}

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
