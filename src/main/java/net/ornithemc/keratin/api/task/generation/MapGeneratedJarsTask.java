package net.ornithemc.keratin.api.task.generation;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.mapping.Mapper;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles.BuildFiles;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles.SourceMappings;
import net.ornithemc.keratin.files.GlobalCache.LibrariesCache;
import net.ornithemc.keratin.files.KeratinFiles;

public abstract class MapGeneratedJarsTask extends MinecraftTask implements Mapper {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		LibrariesCache libraries = files.getGlobalCache().getLibrariesCache();
		SourceMappings mappings = files.getExceptionsAndSignaturesDevelopmentFiles().getSourceMappings();
		BuildFiles buildFiles = files.getExceptionsAndSignaturesDevelopmentFiles().getBuildFiles();

		if (minecraftVersion.hasSharedObfuscation()) {
			workQueue.submit(MapJar.class, parameters -> {
				parameters.getOverwrite().set(true);
				parameters.getInput().set(buildFiles.getNamedGeneratedMergedJar(minecraftVersion));
				parameters.getOutput().set(buildFiles.getGeneratedMergedJar(minecraftVersion));
				parameters.getMappings().set(mappings.getMergedMappingsFile(minecraftVersion));
				parameters.getLibraries().set(libraries.getLibraries(minecraftVersion));
				parameters.getSourceNamespace().set(Mapper.NAMED);
				parameters.getTargetNamespace().set(Mapper.OFFICIAL);
			});
		} else {
			if (minecraftVersion.hasClient()) {
				workQueue.submit(MapJar.class, parameters -> {
					parameters.getOverwrite().set(true);
					parameters.getInput().set(buildFiles.getNamedGeneratedClientJar(minecraftVersion));
					parameters.getOutput().set(buildFiles.getGeneratedClientJar(minecraftVersion));
					parameters.getMappings().set(mappings.getClientMappingsFile(minecraftVersion));
					parameters.getLibraries().set(libraries.getLibraries(minecraftVersion));
					parameters.getSourceNamespace().set(Mapper.NAMED);
					parameters.getTargetNamespace().set(Mapper.OFFICIAL);
				});
			}
			if (minecraftVersion.hasServer()) {
				workQueue.submit(MapJar.class, parameters -> {
					parameters.getOverwrite().set(true);
					parameters.getInput().set(buildFiles.getNamedGeneratedServerJar(minecraftVersion));
					parameters.getOutput().set(buildFiles.getGeneratedServerJar(minecraftVersion));
					parameters.getMappings().set(mappings.getServerMappingsFile(minecraftVersion));
					parameters.getLibraries().set(libraries.getLibraries(minecraftVersion));
					parameters.getSourceNamespace().set(Mapper.NAMED);
					parameters.getTargetNamespace().set(Mapper.OFFICIAL);
				});
			}
		}
	}
}
