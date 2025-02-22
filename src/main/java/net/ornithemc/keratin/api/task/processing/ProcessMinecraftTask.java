package net.ornithemc.keratin.api.task.processing;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class ProcessMinecraftTask extends MinecraftTask implements Processor {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		workQueue.submit(ProcessMinecraft.class, parameters -> {
			parameters.getInputJar().set(files.getMainIntermediaryJar(minecraftVersion));
			parameters.getRavenFile().set(files.getMainIntermediaryRavenFile(minecraftVersion));
			parameters.getExceptionsPatchedJar().set(files.getMainExceptionsPatchedIntermediaryJar(minecraftVersion));
			parameters.getSparrowFile().set(files.getMainIntermediarySparrowFile(minecraftVersion));
			parameters.getSignaturePatchedJar().set(files.getMainSignaturePatchedIntermediaryJar(minecraftVersion));
			parameters.getPreenedJar().set(files.getMainPreenedIntermediaryJar(minecraftVersion));
			parameters.getNestsFile().set(files.getMainIntermediaryNests(minecraftVersion));
			parameters.getNestedJar().set(files.getMainNestedIntermediaryJar(minecraftVersion));
			parameters.getOutputJar().set(files.getMainProcessedIntermediaryJar(minecraftVersion));
		});
	}
}
