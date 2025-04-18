package net.ornithemc.keratin.api.task.setup;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles.SetupFiles;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles.SetupJars;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles.SourceMappings;
import net.ornithemc.keratin.files.KeratinFiles;

public abstract class MakeSourceMappingsTask extends MinecraftTask implements MappingsPatcher {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		ExceptionsAndSignaturesDevelopmentFiles excsAndSigs = files.getExceptionsAndSignaturesDevelopmentFiles();
		SetupJars setupJars = excsAndSigs.getSetupJars();
		SetupFiles setupFiles = excsAndSigs.getSetupFiles();
		SourceMappings sourceMappings = excsAndSigs.getSourceMappings();

		if (minecraftVersion.hasSharedObfuscation()) {
			workQueue.submit(PatchMappings.class, parameters -> {
				parameters.getIntermediaryMappings().set(setupFiles.getMergedIntermediaryMappingsFile(minecraftVersion));
				parameters.getNamedMappings().set(setupFiles.getMergedNamedMappingsFile(minecraftVersion));
				parameters.getMappings().set(sourceMappings.getMergedMappingsFile(minecraftVersion));
				parameters.getJar().set(setupJars.getMergedJar(minecraftVersion));
			});
		} else {
			if (minecraftVersion.hasClient()) {
				workQueue.submit(PatchMappings.class, parameters -> {
					parameters.getIntermediaryMappings().set(setupFiles.getClientIntermediaryMappingsFile(minecraftVersion));
					parameters.getNamedMappings().set(setupFiles.getClientNamedMappingsFile(minecraftVersion));
					parameters.getMappings().set(sourceMappings.getClientMappingsFile(minecraftVersion));
					parameters.getJar().set(setupJars.getClientJar(minecraftVersion));
				});
			}
			if (minecraftVersion.hasServer()) {
				workQueue.submit(PatchMappings.class, parameters -> {
					parameters.getIntermediaryMappings().set(setupFiles.getServerIntermediaryMappingsFile(minecraftVersion));
					parameters.getNamedMappings().set(setupFiles.getServerNamedMappingsFile(minecraftVersion));
					parameters.getMappings().set(sourceMappings.getServerMappingsFile(minecraftVersion));
					parameters.getJar().set(setupJars.getServerJar(minecraftVersion));
				});
			}
		}
	}
}
