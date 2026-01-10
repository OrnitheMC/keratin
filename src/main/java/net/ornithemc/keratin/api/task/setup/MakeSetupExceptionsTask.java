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
import net.ornithemc.keratin.api.JarType;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles.SetupFiles;
import net.ornithemc.keratin.files.KeratinFiles;
import net.ornithemc.keratin.matching.Matches;
import net.ornithemc.keratin.matching.MatchesUtil;

public abstract class MakeSetupExceptionsTask extends MinecraftTask {

	@Internal
	public abstract Property<String> getFromMinecraftVersion();

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		ExceptionsAndSignaturesDevelopmentFiles excsFiles = files.getExceptionsAndSignaturesDevelopmentFiles();
		SetupFiles setupExcsFiles = excsFiles.getSetupFiles();

		MinecraftVersion fromMinecraftVersion = getFromMinecraftVersion().isPresent()
			? MinecraftVersion.parse(keratin, getFromMinecraftVersion().get())
			: null;

		if (minecraftVersion.hasSharedObfuscation()) {
			File excs = excsFiles.getMergedExceptionsFile(minecraftVersion);
			File setup = setupExcsFiles.getMergedExceptionsFile(minecraftVersion);

			if (excs.exists()) {
				Files.copy(excs, setup);
			} else {
				if (fromMinecraftVersion == null) {
					setup.createNewFile();
				} else if (fromMinecraftVersion.hasSharedObfuscation()) {
					File fromExcs = excsFiles.getMergedExceptionsFile(fromMinecraftVersion);

					updateExceptions(
						fromMinecraftVersion.id(),
						JarType.MERGED,
						minecraftVersion.id(),
						JarType.MERGED,
						fromExcs,
						setup
					);
				} else {
					throw new RuntimeException("cannot update from <1.3 version to >=1.3 version!");
				}
			}
		} else {
			if (minecraftVersion.hasClient()) {
				File excs = excsFiles.getClientExceptionsFile(minecraftVersion);
				File setup = setupExcsFiles.getClientExceptionsFile(minecraftVersion);

				if (excs.exists()) {
					Files.copy(excs, setup);
				} else {
					if (fromMinecraftVersion == null) {
						setup.createNewFile();
					} else if (fromMinecraftVersion.hasSharedObfuscation()) {
						throw new RuntimeException("cannot update from <1.3 version to >=1.3 version!");
					} else if (fromMinecraftVersion.hasClient()) {
						File fromExcs = excsFiles.getClientExceptionsFile(fromMinecraftVersion);

						updateExceptions(
							fromMinecraftVersion.client().id(),
							JarType.CLIENT,
							minecraftVersion.client().id(),
							JarType.CLIENT,
							fromExcs,
							setup
						);
					} else {
						setup.createNewFile();
					}
				}
			}
			if (minecraftVersion.hasServer()) {
				File excs = excsFiles.getServerExceptionsFile(minecraftVersion);
				File setup = setupExcsFiles.getServerExceptionsFile(minecraftVersion);

				if (excs.exists()) {
					Files.copy(excs, setup);
				} else {
					if (fromMinecraftVersion == null) {
						setup.createNewFile();
					} else if (fromMinecraftVersion.hasSharedObfuscation()) {
						throw new RuntimeException("cannot update from <1.3 version to >=1.3 version!");
					} else if (fromMinecraftVersion.hasServer()) {
						File fromExcs = excsFiles.getServerExceptionsFile(fromMinecraftVersion);

						updateExceptions(
							fromMinecraftVersion.server().id(),
							JarType.SERVER,
							minecraftVersion.server().id(),
							JarType.SERVER,
							fromExcs,
							setup
						);
					} else {
						setup.createNewFile();
					}
				}
			}
		}
	}

	private void updateExceptions(String fromMinecraftVersion, JarType fromType, String toMinecraftVersion, JarType toType, File from, File to) throws IOException {
		KeratinGradleExtension keratin = getExtension();

		if (!from.exists()) {
			throw new RuntimeException("exceptions for " + fromMinecraftVersion + " to update from do not exist!");
		}

		Matches matches = keratin.findMatches(fromType, fromMinecraftVersion, toType, toMinecraftVersion);
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
