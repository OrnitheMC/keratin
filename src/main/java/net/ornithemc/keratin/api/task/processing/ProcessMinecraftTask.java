package net.ornithemc.keratin.api.task.processing;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.processing.Processor.ProcessMinecraft;

public abstract class ProcessMinecraftTask extends MinecraftTask implements Nester, SignaturePatcher {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) {
		getProject().getLogger().lifecycle(":processing Minecraft " + minecraftVersion);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		workQueue.submit(ProcessMinecraft.class, parameters -> {
			parameters.getInputJar().set(files.getMainIntermediaryJar(minecraftVersion));
			parameters.getNestsFile().set(files.getMainIntermediaryNests(minecraftVersion));
			parameters.getNestedJar().set(files.getMainNestedIntermediaryJar(minecraftVersion));
			parameters.getSparrowFile().set(files.getMainIntermediarySparrowFile(minecraftVersion));
			parameters.getSignaturePatchedJar().set(files.getMainSignaturePatchedIntermediaryJar(minecraftVersion));
			parameters.getOutputJar().set(files.getMainProcessedIntermediaryJar(minecraftVersion));
		});
	}
}
