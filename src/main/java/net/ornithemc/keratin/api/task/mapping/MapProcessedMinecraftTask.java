package net.ornithemc.keratin.api.task.mapping;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.settings.ProcessorSettings;
import net.ornithemc.keratin.api.task.merging.Merger;
import net.ornithemc.keratin.files.GlobalCache.LibrariesCache;
import net.ornithemc.keratin.files.GlobalCache.ProcessedJarsCache;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles.BuildFiles;
import net.ornithemc.keratin.files.OrnitheFiles;

public abstract class MapProcessedMinecraftTask extends MappingTask implements Merger {

	@Internal
	public abstract Property<Boolean> getForDecompile();

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		String srcNs = getSourceNamespace().get();
		String dstNs = getTargetNamespace().get();

		validateNamespaces(srcNs, dstNs);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFiles files = keratin.getFiles();

		ProcessedJarsCache processedJars = files.getGlobalCache().getProcessedJarsCache();
		LibrariesCache libraries = files.getGlobalCache().getLibrariesCache();
		BuildFiles buildFiles = files.getMappingsDevelopmentFiles().getBuildFiles();

		ProcessorSettings processorSettings = getForDecompile().get()
			? keratin.getProcessorSettingsForDecompile(minecraftVersion)
			: keratin.getProcessorSettings(minecraftVersion);

		workQueue.submit(MapJar.class, parameters -> {
			parameters.getOverwrite().set(keratin.isCacheInvalid());
			parameters.getInput().set(processedJars.getProcessedIntermediaryJar(minecraftVersion, processorSettings));
			parameters.getOutput().set(buildFiles.getProcessedNamedJar(minecraftVersion.id()));
			parameters.getMappings().set(buildFiles.getProcessedMappingsFile(minecraftVersion));
			parameters.getLibraries().set(libraries.getLibraries(minecraftVersion));
			parameters.getSourceNamespace().set(srcNs);
			parameters.getTargetNamespace().set(dstNs);
		});
	}

	private static void validateNamespaces(String srcNs, String dstNs) {
		boolean valid = switch (dstNs) {
			case NAMED -> INTERMEDIARY.equals(srcNs);
			default -> false;
		};
		if (!valid) {
			throw new IllegalStateException("cannot map processed Minecraft from " + srcNs + " to " + dstNs);
		}
	}
}
