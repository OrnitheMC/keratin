package net.ornithemc.keratin.api.task.setup;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.mapping.Mapper;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles.SetupFiles;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles.SetupJars;
import net.ornithemc.keratin.files.GlobalCache.LibrariesCache;
import net.ornithemc.keratin.files.OrnitheFiles;

public abstract class MapSetupJarsTask extends MinecraftTask implements Mapper {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFiles files = keratin.getFiles();

		LibrariesCache libraries = files.getGlobalCache().getLibrariesCache();
		SetupJars setupJars = files.getExceptionsAndSignaturesDevelopmentFiles().getSetupJars();
		SetupFiles setupFiles = files.getExceptionsAndSignaturesDevelopmentFiles().getSetupFiles();

		if (minecraftVersion.hasSharedObfuscation()) {
			workQueue.submit(MapJar.class, parameters -> {
				parameters.getOverwrite().set(true);
				parameters.getInput().set(setupJars.getMergedJar(minecraftVersion));
				parameters.getOutput().set(setupJars.getIntermediaryMergedJar(minecraftVersion));
				parameters.getMappings().set(setupFiles.getMergedIntermediaryMappingsFile(minecraftVersion));
				parameters.getLibraries().set(libraries.getLibraries(minecraftVersion));
				parameters.getSourceNamespace().set("official");
				parameters.getTargetNamespace().set("intermediary");
			});
		} else {
			if (minecraftVersion.hasClient()) {
				workQueue.submit(MapJar.class, parameters -> {
					parameters.getOverwrite().set(true);
					parameters.getInput().set(setupJars.getClientJar(minecraftVersion));
					parameters.getOutput().set(setupJars.getIntermediaryClientJar(minecraftVersion));
					parameters.getMappings().set(setupFiles.getClientIntermediaryMappingsFile(minecraftVersion));
					parameters.getLibraries().set(libraries.getLibraries(minecraftVersion));
					parameters.getSourceNamespace().set("official");
					parameters.getTargetNamespace().set("intermediary");
				});
			}
			if (minecraftVersion.hasServer()) {
				workQueue.submit(MapJar.class, parameters -> {
					parameters.getOverwrite().set(true);
					parameters.getInput().set(setupJars.getServerJar(minecraftVersion));
					parameters.getOutput().set(setupJars.getIntermediaryServerJar(minecraftVersion));
					parameters.getMappings().set(setupFiles.getServerIntermediaryMappingsFile(minecraftVersion));
					parameters.getLibraries().set(libraries.getLibraries(minecraftVersion));
					parameters.getSourceNamespace().set("official");
					parameters.getTargetNamespace().set("intermediary");
				});
			}
		}
	}
}
