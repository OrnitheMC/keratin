package net.ornithemc.keratin.api.task.processing;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.settings.ProcessorSettings;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.GlobalCache;
import net.ornithemc.keratin.files.GlobalCache.ExceptionsCache;
import net.ornithemc.keratin.files.GlobalCache.LibrariesCache;
import net.ornithemc.keratin.files.GlobalCache.MappedJarsCache;
import net.ornithemc.keratin.files.GlobalCache.NestsCache;
import net.ornithemc.keratin.files.GlobalCache.ProcessedJarsCache;
import net.ornithemc.keratin.files.GlobalCache.SignaturesCache;
import net.ornithemc.keratin.files.OrnitheFiles;

public abstract class ProcessMinecraftTask extends MinecraftTask implements Processor {

	@Internal
	public abstract Property<Boolean> getForDecompile();

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFiles files = keratin.getFiles();

		GlobalCache globalCache = files.getGlobalCache();
		MappedJarsCache mappedJars = globalCache.getMappedJarsCache();
		ProcessedJarsCache processedJars = globalCache.getProcessedJarsCache();
		ExceptionsCache exceptions = globalCache.getExceptionsCache();
		SignaturesCache signatures = globalCache.getSignaturesCache();
		NestsCache nests = globalCache.getNestsCache();
		LibrariesCache libraries = globalCache.getLibrariesCache();

		ProcessorSettings settings = getForDecompile().get()
			? keratin.getProcessorSettingsForDecompile(minecraftVersion)
			: keratin.getProcessorSettings(minecraftVersion);

		workQueue.submit(ProcessMinecraft.class, parameters -> {
			parameters.getOverwrite().set(keratin.isCacheInvalid());
			parameters.getInputJar().set(mappedJars.getMainIntermediaryJar(minecraftVersion));
			parameters.getOutputJar().set(processedJars.getProcessedIntermediaryJar(minecraftVersion, settings));
			parameters.getLibraries().set(libraries.getLibraries(minecraftVersion));
			parameters.getObfuscateVariableNames().set(settings.obfuscateLocalVariableNames());
			parameters.getExceptionsFile().set(exceptions.getMainIntermediaryExceptionsFile(minecraftVersion, settings.exceptionsBuilds()));
			parameters.getSignaturesFile().set(signatures.getMainIntermediarySignaturesFile(minecraftVersion, settings.signaturesBuilds()));
			parameters.getNestsFile().set(nests.getMainIntermediaryNestsFile(minecraftVersion, settings.nestsBuilds()));
		});
	}
}
