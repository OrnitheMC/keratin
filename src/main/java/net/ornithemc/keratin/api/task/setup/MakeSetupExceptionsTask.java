package net.ornithemc.keratin.api.task.setup;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.workers.WorkQueue;

import org.objectweb.asm.commons.Remapper;

import com.google.common.io.Files;

import net.ornithemc.exceptor.io.ClassEntry;
import net.ornithemc.exceptor.io.ExceptionsFile;
import net.ornithemc.exceptor.io.ExceptorIo;
import net.ornithemc.exceptor.io.MethodEntry;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.matching.Matches;
import net.ornithemc.keratin.matching.MatchesUtil;

public abstract class MakeSetupExceptionsTask extends MinecraftTask {

	@Internal
	public abstract Property<String> getFromMinecraftVersion();

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (details.sharedMappings()) {
			File excs = files.getMergedExceptions(minecraftVersion);
			File setup = files.getSetupMergedExceptions(minecraftVersion);

			if (excs.exists()) {
				Files.copy(excs, setup);
			} else {
				String fromMinecraftVersion = getFromMinecraftVersion().getOrNull();

				if (fromMinecraftVersion == null) {
					setup.createNewFile();
				} else {
					VersionDetails fromDetails = keratin.getVersionDetails(fromMinecraftVersion);

					if (fromDetails.sharedMappings()) {
						File fromExcs = files.getMergedExceptions(fromMinecraftVersion);

						updateExceptions(
							fromMinecraftVersion,
							"merged",
							minecraftVersion,
							"merged",
							fromExcs,
							setup
						);
					} else {
						throw new RuntimeException("cannot update from <1.3 version to >=1.3 version!");
					}
				}
			}
		} else {
			if (details.client()) {
				File excs = files.getClientExceptions(minecraftVersion);
				File setup = files.getSetupClientExceptions(minecraftVersion);

				if (excs.exists()) {
					Files.copy(excs, setup);
				} else {
					String fromMinecraftVersion = getFromMinecraftVersion().getOrNull();

					if (fromMinecraftVersion == null) {
						setup.createNewFile();
					} else {
						VersionDetails fromDetails = keratin.getVersionDetails(fromMinecraftVersion);

						if (fromDetails.sharedMappings()) {
							throw new RuntimeException("cannot update from <1.3 version to >=1.3 version!");
						} else if (fromDetails.client()) {
							File fromExcs = files.getClientExceptions(fromMinecraftVersion);

							updateExceptions(
								fromMinecraftVersion,
								"client",
								minecraftVersion,
								"client",
								fromExcs,
								setup
							);
						}
					}
				}
			}
			if (details.server()) {
				File excs = files.getServerExceptions(minecraftVersion);
				File setup = files.getSetupServerExceptions(minecraftVersion);

				if (excs.exists()) {
					Files.copy(excs, setup);
				} else {
					String fromMinecraftVersion = getFromMinecraftVersion().getOrNull();

					if (fromMinecraftVersion == null) {
						setup.createNewFile();
					} else {
						VersionDetails fromDetails = keratin.getVersionDetails(fromMinecraftVersion);

						if (fromDetails.sharedMappings()) {
							throw new RuntimeException("cannot update from <1.3 version to >=1.3 version!");
						} else if (fromDetails.server()) {
							File fromExcs = files.getServerExceptions(fromMinecraftVersion);

							updateExceptions(
								fromMinecraftVersion,
								"server",
								minecraftVersion,
								"server",
								fromExcs,
								setup
							);
						}
					}
				}
			}
		}
	}

	private void updateExceptions(String fromMinecraftVersion, String fromSide, String toMinecraftVersion, String toSide, File from, File to) throws IOException {
		KeratinGradleExtension keratin = getExtension();

		if (!from.exists()) {
			throw new RuntimeException("exceptions for " + fromMinecraftVersion + " to update from do not exist!");
		}

		Matches matches = keratin.findMatches(fromSide, fromMinecraftVersion, toSide, toMinecraftVersion);
		Remapper mapper = MatchesUtil.makeAsmRemapper(matches.file(), matches.inverted());

		ExceptionsFile fromExcs = ExceptorIo.read(from.toPath());
		ExceptionsFile toExcs = new ExceptionsFile();

		for (Map.Entry<String, ClassEntry> ce : fromExcs.classes().entrySet()) {
			String fromClsName = ce.getKey();
			String toClsName = mapper.map(fromClsName);

			if (toClsName == null) {
				continue;
			}

			ClassEntry fromCls = ce.getValue();
			ClassEntry toCls = new ClassEntry(toClsName);

			toExcs.classes().put(toCls.name(), toCls);

			for (Map.Entry<String, MethodEntry> me : fromCls.methods().entrySet()) {
				String fromMtdName = me.getValue().name();
				String fromMtdDesc = me.getValue().descriptor();
				String toMtdName = mapper.mapMethodName(fromClsName, fromMtdName, fromMtdDesc);
				String toMtdDesc = mapper.mapDesc(fromMtdDesc);

				if (toMtdName == null || toMtdDesc == null) {
					continue;
				}

				MethodEntry fromMtd = me.getValue();
				MethodEntry toMtd = new MethodEntry(fromMtdName, fromMtdDesc);

				toCls.methods().put(toMtd.name() + toMtd.descriptor(), toMtd);

				for (String fromExc : fromMtd.exceptions()) {
					String toExc = mapper.map(fromExc);

					if (toExc != null) {
						toMtd.exceptions().add(toExc);
					}
				}
			}
		}

		ExceptorIo.write(to.toPath(), toExcs);
	}
}
