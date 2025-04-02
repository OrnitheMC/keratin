package net.ornithemc.keratin.api.task.processing;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class ProcessMinecraftTask extends MinecraftTask implements Processor {

	@Internal
	public abstract Property<Boolean> getObfuscateVariableNames();

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		workQueue.submit(ProcessMinecraft.class, parameters -> {
			parameters.getInputJar().set(files.getMainIntermediaryJar(minecraftVersion));
			parameters.getLibraries().set(files.getLibraries(minecraftVersion));
			parameters.getObfuscateVariableNames().set(getObfuscateVariableNames().get());
			parameters.getLvtPatchedJar().set(files.getMainLvtPatchedIntermediaryJar(minecraftVersion));
			parameters.getExceptionsFile().set(files.getMainIntermediaryExceptionsFile(minecraftVersion));
			parameters.getExceptionsPatchedJar().set(files.getMainExceptionsPatchedIntermediaryJar(minecraftVersion));
			parameters.getSignaturesFile().set(files.getMainIntermediarySignaturesFile(minecraftVersion));
			parameters.getSignaturePatchedJar().set(files.getMainSignaturePatchedIntermediaryJar(minecraftVersion));
			parameters.getPreenedJar().set(files.getMainPreenedIntermediaryJar(minecraftVersion));
			parameters.getNestsFile().set(files.getMainIntermediaryNestsFile(minecraftVersion));
			parameters.getNestedJar().set(files.getMainNestedIntermediaryJar(minecraftVersion));
			parameters.getOutputJar().set(files.getMainProcessedIntermediaryJar(minecraftVersion));
		});
	}
}
