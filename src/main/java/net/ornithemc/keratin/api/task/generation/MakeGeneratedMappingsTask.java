package net.ornithemc.keratin.api.task.generation;

import java.io.File;
import java.io.IOException;

import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.mapping.Mapper;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles.BuildFiles;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles.SetupJars;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles.SourceMappings;
import net.ornithemc.keratin.files.KeratinFiles;

public abstract class MakeGeneratedMappingsTask extends MinecraftTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		ExceptionsAndSignaturesDevelopmentFiles excsAndSigs = files.getExceptionsAndSignaturesDevelopmentFiles();
		SetupJars setupJars = excsAndSigs.getSetupJars();
		SourceMappings sourceMappings = excsAndSigs.getSourceMappings();
		BuildFiles buildFiles = excsAndSigs.getBuildFiles();

		if (minecraftVersion.canBeMergedAsObfuscated()) {
			workQueue.submit(PatchMappings.class, parameters -> {
				parameters.getInput().set(sourceMappings.getMergedMappingsFile(minecraftVersion));
				parameters.getOutput().set(buildFiles.getMergedMappingsFile(minecraftVersion));
				parameters.getJar().set(setupJars.getIntermediaryMergedJar(minecraftVersion));
			});
		} else {
			if (minecraftVersion.hasClient()) {
				workQueue.submit(PatchMappings.class, parameters -> {
					parameters.getInput().set(sourceMappings.getClientMappingsFile(minecraftVersion));
					parameters.getOutput().set(buildFiles.getClientMappingsFile(minecraftVersion));
					parameters.getJar().set(minecraftVersion.canBeMergedAsMapped()
						? setupJars.getIntermediaryMergedJar(minecraftVersion)
						: setupJars.getIntermediaryClientJar(minecraftVersion));
				});
			}
			if (minecraftVersion.hasServer()) {
				workQueue.submit(PatchMappings.class, parameters -> {
					parameters.getInput().set(sourceMappings.getServerMappingsFile(minecraftVersion));
					parameters.getOutput().set(buildFiles.getServerMappingsFile(minecraftVersion));
					parameters.getJar().set(minecraftVersion.canBeMergedAsMapped()
						? setupJars.getIntermediaryMergedJar(minecraftVersion)
						: setupJars.getIntermediaryServerJar(minecraftVersion));
				});
			}
		}
	}

	public interface SourceMappingsParameters extends WorkParameters {

		Property<File> getInput();

		Property<File> getOutput();

		Property<File> getJar();

	}

	public static abstract class PatchMappings implements WorkAction<SourceMappingsParameters>, AnonymousClassReindexer {

		@Override
		public void execute() {
			File in = getParameters().getInput().get();
			File out = getParameters().getOutput().get();
			File jar = getParameters().getJar().get();

			try {
				reindexAnonymousClasses(jar, in, out, Mapper.INTERMEDIARY);
			} catch (IOException e) {
				throw new RuntimeException("error while running anonymous class reindexer", e);
			}
		}
	}
}
