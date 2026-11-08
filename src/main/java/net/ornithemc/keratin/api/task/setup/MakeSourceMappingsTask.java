package net.ornithemc.keratin.api.task.setup;

import java.io.File;
import java.io.IOException;

import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingWriter;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.mapping.Mapper;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles.SetupFiles;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles.SourceMappings;
import net.ornithemc.keratin.files.KeratinFiles;

public abstract class MakeSourceMappingsTask extends MinecraftTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		ExceptionsAndSignaturesDevelopmentFiles excsAndSigs = files.getExceptionsAndSignaturesDevelopmentFiles();
		SetupFiles setupFiles = excsAndSigs.getSetupFiles();
		SourceMappings sourceMappings = excsAndSigs.getSourceMappings();

		if (minecraftVersion.canBeMergedAsObfuscated()) {
			workQueue.submit(PatchMappings.class, parameters -> {
				parameters.getIntermediaryMappings().set(setupFiles.getMergedIntermediaryMappingsFile(minecraftVersion));
				parameters.getNamedMappings().set(setupFiles.getMergedNamedMappingsFile(minecraftVersion));
				parameters.getMappings().set(sourceMappings.getMergedMappingsFile(minecraftVersion));
			});
		} else {
			if (minecraftVersion.hasClient()) {
				workQueue.submit(PatchMappings.class, parameters -> {
					parameters.getIntermediaryMappings().set(setupFiles.getClientIntermediaryMappingsFile(minecraftVersion));
					parameters.getNamedMappings().set(setupFiles.getClientNamedMappingsFile(minecraftVersion));
					parameters.getMappings().set(sourceMappings.getClientMappingsFile(minecraftVersion));
				});
			}
			if (minecraftVersion.hasServer()) {
				workQueue.submit(PatchMappings.class, parameters -> {
					parameters.getIntermediaryMappings().set(setupFiles.getServerIntermediaryMappingsFile(minecraftVersion));
					parameters.getNamedMappings().set(setupFiles.getServerNamedMappingsFile(minecraftVersion));
					parameters.getMappings().set(sourceMappings.getServerMappingsFile(minecraftVersion));
				});
			}
		}
	}

	public interface SourceMappingsParameters extends WorkParameters {

		Property<File> getIntermediaryMappings();

		Property<File> getNamedMappings();

		Property<File> getMappings();

	}

	public static abstract class PatchMappings implements WorkAction<SourceMappingsParameters> {

		@Override
		public void execute() {
			File intermediary = getParameters().getIntermediaryMappings().get();
			File named = getParameters().getNamedMappings().get();
			File out = getParameters().getMappings().get();

			try {
				MemoryMappingTree mappings = new MemoryMappingTree();
				MappingReader.read(intermediary.toPath(), new MappingSourceNsSwitch(mappings, Mapper.INTERMEDIARY));
				MappingReader.read(named.toPath(), mappings);

				try (MappingWriter writer = MappingWriter.create(out.toPath(), MappingFormat.TINY_2_FILE)) {
					mappings.accept(new MappingSourceNsSwitch(writer, Mapper.OFFICIAL, true));
				}
			} catch (IOException e) {
				throw new RuntimeException("error merging intermediary and named mappings mappings", e);
			}
		}
	}
}
