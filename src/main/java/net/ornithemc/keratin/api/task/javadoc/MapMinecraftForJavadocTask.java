package net.ornithemc.keratin.api.task.javadoc;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.mapping.Mapper;
import net.ornithemc.keratin.files.GlobalCache.LibrariesCache;
import net.ornithemc.keratin.files.GlobalCache.MappedJarsCache;
import net.ornithemc.keratin.files.KeratinFiles;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles.BuildFiles;

public abstract class MapMinecraftForJavadocTask extends MinecraftTask implements Mapper {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		MappedJarsCache mappedJars = files.getGlobalCache().getMappedJarsCache();
		LibrariesCache libraries = files.getGlobalCache().getLibrariesCache();
		BuildFiles buildFiles = files.getMappingsDevelopmentFiles().getBuildFiles();

		if (minecraftVersion.hasSharedVersioning() && minecraftVersion.canBeMerged()) {
			workQueue.submit(MapJar.class, parameters -> {
				parameters.getOverwrite().set(true);
				parameters.getInput().set(mappedJars.getIntermediaryMergedJar(minecraftVersion));
				parameters.getOutput().set(buildFiles.getJavadocNamedJar(minecraftVersion.id()));
				parameters.getMappings().set(buildFiles.getMappingsFile(minecraftVersion));
				parameters.getLibraries().addAll(libraries.getLibraries(minecraftVersion));
				parameters.getSourceNamespace().set(INTERMEDIARY);
				parameters.getTargetNamespace().set(NAMED);
			});
		} else {
			if (minecraftVersion.hasClient()) {
				workQueue.submit(MapJar.class, parameters -> {
					parameters.getOverwrite().set(true);
					parameters.getInput().set(mappedJars.getIntermediaryClientJar(minecraftVersion));
					parameters.getOutput().set(buildFiles.getJavadocNamedJar(minecraftVersion.client().id()));
					parameters.getMappings().set(buildFiles.getMappingsFile(minecraftVersion));
					parameters.getLibraries().set(libraries.getLibraries(minecraftVersion.client().id()));
					parameters.getSourceNamespace().set(INTERMEDIARY);
					parameters.getTargetNamespace().set(NAMED);
				});
			}
			if (minecraftVersion.hasServer()) {
				workQueue.submit(MapJar.class, parameters -> {
					parameters.getOverwrite().set(true);
					parameters.getInput().set(mappedJars.getIntermediaryServerJar(minecraftVersion));
					parameters.getOutput().set(buildFiles.getJavadocNamedJar(minecraftVersion.server().id()));
					parameters.getMappings().set(buildFiles.getMappingsFile(minecraftVersion));
					parameters.getLibraries().set(libraries.getLibraries(minecraftVersion.server().id()));
					parameters.getSourceNamespace().set(INTERMEDIARY);
					parameters.getTargetNamespace().set(NAMED);
				});
			}
		}
	}
}
