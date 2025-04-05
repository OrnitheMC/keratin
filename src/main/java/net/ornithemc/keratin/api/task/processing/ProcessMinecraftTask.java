package net.ornithemc.keratin.api.task.processing;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
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
	public abstract Property<Boolean> getObfuscateVariableNames();

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

		workQueue.submit(ProcessMinecraft.class, parameters -> {
			parameters.getInputJar().set(mappedJars.getMainIntermediaryJar(minecraftVersion));
			parameters.getLibraries().set(libraries.getLibraries(minecraftVersion));
			parameters.getObfuscateVariableNames().set(getObfuscateVariableNames().get());
			parameters.getLvtPatchedJar().set(processedJars.getMainLvtPatchedIntermediaryJar(minecraftVersion));
			parameters.getExceptionsFile().set(exceptions.getMainIntermediaryExceptionsFile(minecraftVersion));
			parameters.getExceptionsPatchedJar().set(processedJars.getMainExceptionsPatchedIntermediaryJar(minecraftVersion));
			parameters.getSignaturesFile().set(signatures.getMainIntermediarySignaturesFile(minecraftVersion));
			parameters.getSignaturePatchedJar().set(processedJars.getMainSignaturePatchedIntermediaryJar(minecraftVersion));
			parameters.getPreenedJar().set(processedJars.getMainPreenedIntermediaryJar(minecraftVersion));
			parameters.getNestsFile().set(nests.getMainIntermediaryNestsFile(minecraftVersion));
			parameters.getNestedJar().set(processedJars.getMainNestedIntermediaryJar(minecraftVersion));
			parameters.getOutputJar().set(processedJars.getMainProcessedIntermediaryJar(minecraftVersion));
		});
	}
}
