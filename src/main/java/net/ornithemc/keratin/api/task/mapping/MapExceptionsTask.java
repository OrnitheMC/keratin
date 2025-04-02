package net.ornithemc.keratin.api.task.mapping;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;

public abstract class MapExceptionsTask extends MappingTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		String srcNs = getSourceNamespace().get();
		String dstNs = getTargetNamespace().get();

		validateNamespaces(srcNs, dstNs);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		boolean fromOfficial = OFFICIAL.equals(srcNs);

		int clientBuild = keratin.getExceptionsBuild(minecraftVersion, GameSide.CLIENT);
		int serverBuild = keratin.getExceptionsBuild(minecraftVersion, GameSide.SERVER);
		int mergedBuild = keratin.getExceptionsBuild(minecraftVersion, GameSide.MERGED);

		if (minecraftVersion.canBeMerged() && (!fromOfficial || minecraftVersion.hasSharedObfuscation())) {
			if (minecraftVersion.hasSharedObfuscation() ? (mergedBuild > 0) : (clientBuild > 0 || serverBuild > 0)) {
				workQueue.submit(MapExceptions.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getBrokenInnerClasses().set(fromOfficial && minecraftVersion.hasBrokenInnerClasses());
					parameters.getInput().set(fromOfficial ? files.getMergedExceptionsFile(minecraftVersion) : files.getIntermediaryMergedExceptionsFile(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? files.getIntermediaryMergedExceptionsFile(minecraftVersion) : files.getNamedExceptionsFile(minecraftVersion));
					parameters.getMappings().set(fromOfficial ? files.getFilledMergedIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion));
				});
			}
		} else {
			if (minecraftVersion.hasClient() && (minecraftVersion.hasSharedObfuscation() ? (mergedBuild > 0) : (clientBuild > 0))) {
				workQueue.submit(MapExceptions.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getBrokenInnerClasses().set(fromOfficial && minecraftVersion.hasBrokenInnerClasses());
					parameters.getInput().set(fromOfficial ? files.getClientExceptionsFile(minecraftVersion) : files.getIntermediaryClientExceptionsFile(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? files.getIntermediaryClientExceptionsFile(minecraftVersion) : files.getNamedExceptionsFile(minecraftVersion));
					parameters.getMappings().set(fromOfficial ? files.getFilledClientIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion));
				});
			}
			if (minecraftVersion.hasServer() && (minecraftVersion.hasSharedObfuscation() ? (mergedBuild > 0) : (serverBuild > 0))) {
				workQueue.submit(MapExceptions.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getBrokenInnerClasses().set(fromOfficial && minecraftVersion.hasBrokenInnerClasses());
					parameters.getInput().set(fromOfficial ? files.getServerExceptionsFile(minecraftVersion) : files.getIntermediaryServerExceptionsFile(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? files.getIntermediaryServerExceptionsFile(minecraftVersion) : files.getNamedExceptionsFile(minecraftVersion));
					parameters.getMappings().set(fromOfficial ? files.getFilledServerIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion));
				});
			}
		}
	}

	private static void validateNamespaces(String srcNs, String dstNs) {
		boolean valid = switch (dstNs) {
			case INTERMEDIARY -> OFFICIAL.equals(srcNs);
			case NAMED -> INTERMEDIARY.equals(srcNs);
			default -> false;
		};
		if (!valid) {
			throw new IllegalStateException("cannot map exceptions from " + srcNs + " to " + dstNs);
		}
	}
}
