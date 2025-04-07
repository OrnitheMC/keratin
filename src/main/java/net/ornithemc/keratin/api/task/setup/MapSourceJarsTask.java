package net.ornithemc.keratin.api.task.setup;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.mapping.Mapper;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles.SourceJars;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles.SourceMappings;
import net.ornithemc.keratin.files.GlobalCache.LibrariesCache;
import net.ornithemc.keratin.files.OrnitheFiles;

public abstract class MapSourceJarsTask extends MinecraftTask implements Mapper {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFiles files = keratin.getFiles();

		LibrariesCache libraries = files.getGlobalCache().getLibrariesCache();
		SourceJars sourceJars = files.getExceptionsAndSignaturesDevelopmentFiles().getSourceJars();
		SourceMappings mappings = files.getExceptionsAndSignaturesDevelopmentFiles().getSourceMappings();

		if (minecraftVersion.hasSharedObfuscation()) {
			workQueue.submit(MapJar.class, parameters -> {
				parameters.getOverwrite().set(true);
				parameters.getInput().set(sourceJars.getMergedJar(minecraftVersion));
				parameters.getOutput().set(sourceJars.getNamedMergedJar(minecraftVersion));
				parameters.getMappings().set(mappings.getMergedMappingsFile(minecraftVersion));
				parameters.getLibraries().set(libraries.getLibraries(minecraftVersion));
				parameters.getSourceNamespace().set("official");
				parameters.getTargetNamespace().set("named");
			});
		} else {
			if (minecraftVersion.hasClient()) {
				workQueue.submit(MapJar.class, parameters -> {
					parameters.getOverwrite().set(true);
					parameters.getInput().set(sourceJars.getClientJar(minecraftVersion));
					parameters.getOutput().set(sourceJars.getNamedClientJar(minecraftVersion));
					parameters.getMappings().set(mappings.getClientMappingsFile(minecraftVersion));
					parameters.getLibraries().set(libraries.getLibraries(minecraftVersion));
					parameters.getSourceNamespace().set("official");
					parameters.getTargetNamespace().set("named");
				});
			}
			if (minecraftVersion.hasServer()) {
				workQueue.submit(MapJar.class, parameters -> {
					parameters.getOverwrite().set(true);
					parameters.getInput().set(sourceJars.getServerJar(minecraftVersion));
					parameters.getOutput().set(sourceJars.getNamedServerJar(minecraftVersion));
					parameters.getMappings().set(mappings.getServerMappingsFile(minecraftVersion));
					parameters.getLibraries().set(libraries.getLibraries(minecraftVersion));
					parameters.getSourceNamespace().set("official");
					parameters.getTargetNamespace().set("named");
				});
			}
		}
	}
}
