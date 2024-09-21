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
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.matching.Matches;
import net.ornithemc.keratin.matching.MatchesUtil;

public abstract class MakeSetupExceptionsTask extends MinecraftTask {

	@Internal
	public abstract Property<String> getFromMinecraftVersion();

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		MinecraftVersion fromMinecraftVersion = getFromMinecraftVersion().isPresent()
			? MinecraftVersion.parse(keratin, getFromMinecraftVersion().get())
			: null;

		if (minecraftVersion.hasSharedObfuscation()) {
			File excs = files.getMergedExceptions(minecraftVersion);
			File setup = files.getSetupMergedExceptions(minecraftVersion);

			if (excs.exists()) {
				Files.copy(excs, setup);
			} else {
				if (fromMinecraftVersion == null) {
					setup.createNewFile();
				} else if (fromMinecraftVersion.hasSharedObfuscation()) {
					File fromExcs = files.getMergedExceptions(fromMinecraftVersion);

					updateExceptions(
						fromMinecraftVersion.id(),
						"merged",
						minecraftVersion.id(),
						"merged",
						fromExcs,
						setup
					);
				} else {
					throw new RuntimeException("cannot update from <1.3 version to >=1.3 version!");
				}
			}
		} else {
			if (minecraftVersion.hasClient()) {
				File excs = files.getClientExceptions(minecraftVersion);
				File setup = files.getSetupClientExceptions(minecraftVersion);

				if (excs.exists()) {
					Files.copy(excs, setup);
				} else {
					if (fromMinecraftVersion == null) {
						setup.createNewFile();
					} else if (fromMinecraftVersion.hasSharedObfuscation()) {
						throw new RuntimeException("cannot update from <1.3 version to >=1.3 version!");
					} else if (fromMinecraftVersion.hasClient()) {
						File fromExcs = files.getClientExceptions(fromMinecraftVersion);

						updateExceptions(
							fromMinecraftVersion.client().id(),
							"client",
							minecraftVersion.client().id(),
							"client",
							fromExcs,
							setup
						);
					}
				}
			}
			if (minecraftVersion.hasServer()) {
				File excs = files.getServerExceptions(minecraftVersion);
				File setup = files.getSetupServerExceptions(minecraftVersion);

				if (excs.exists()) {
					Files.copy(excs, setup);
				} else {
					if (fromMinecraftVersion == null) {
						setup.createNewFile();
					} else if (fromMinecraftVersion.hasSharedObfuscation()) {
						throw new RuntimeException("cannot update from <1.3 version to >=1.3 version!");
					} else if (fromMinecraftVersion.hasServer()) {
						File fromExcs = files.getServerExceptions(fromMinecraftVersion);

						updateExceptions(
							fromMinecraftVersion.server().id(),
							"server",
							minecraftVersion.server().id(),
							"server",
							fromExcs,
							setup
						);
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
		Remapper mapper = MatchesUtil.makeRemapper(matches.file(), matches.inverted());

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
				mapper.map(fromClsName);

				String fromMtdName = me.getValue().name();
				String fromMtdDesc = me.getValue().descriptor();
				String toMtdName = mapper.mapMethodName(fromClsName, fromMtdName, fromMtdDesc);
				String toMtdDesc = mapper.mapDesc(fromMtdDesc);

				if (toMtdName == null || toMtdDesc == null) {
					continue;
				}

				MethodEntry fromMtd = me.getValue();
				MethodEntry toMtd = new MethodEntry(toMtdName, toMtdDesc);

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
