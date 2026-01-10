package net.ornithemc.keratin.api.task.generation;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.gradle.workers.WorkQueue;

import net.ornithemc.exceptor.io.ClassEntry;
import net.ornithemc.exceptor.io.ExceptionsFile;
import net.ornithemc.exceptor.io.ExceptorIo;
import net.ornithemc.exceptor.io.MethodEntry;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.processing.Exceptor;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles.BuildFiles;
import net.ornithemc.keratin.files.KeratinFiles;

public abstract class SaveExceptionsTask extends MinecraftTask implements Exceptor {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		ExceptionsAndSignaturesDevelopmentFiles excsAndSigs = files.getExceptionsAndSignaturesDevelopmentFiles();
		BuildFiles buildFiles = excsAndSigs.getBuildFiles();

		if (minecraftVersion.canBeMergedAsObfuscated()) {
			saveExceptions(
				buildFiles.getBaseMergedExceptionsFile(minecraftVersion),
				buildFiles.getGeneratedMergedExceptionsFile(minecraftVersion),
				excsAndSigs.getMergedExceptionsFile(minecraftVersion)
			);
		} else {
			if (minecraftVersion.hasClient()) {
				saveExceptions(
					buildFiles.getBaseClientExceptionsFile(minecraftVersion),
					buildFiles.getGeneratedClientExceptionsFile(minecraftVersion),
					excsAndSigs.getClientExceptionsFile(minecraftVersion)
				);
			}
			if (minecraftVersion.hasServer()) {
				saveExceptions(
					buildFiles.getBaseServerExceptionsFile(minecraftVersion),
					buildFiles.getGeneratedServerExceptionsFile(minecraftVersion),
					excsAndSigs.getServerExceptionsFile(minecraftVersion)
				);
			}
		}
	}

	private void saveExceptions(File inputBase, File inputGenerated, File output) throws IOException {
		ExceptionsFile base = ExceptorIo.read(inputBase.toPath());
		ExceptionsFile generated = ExceptorIo.read(inputGenerated.toPath());

		Iterator<Map.Entry<String, ClassEntry>> cit = generated.classes().entrySet().iterator();

		while (cit.hasNext()) {
			Map.Entry<String, ClassEntry> ce = cit.next();
			String clsName = ce.getKey();
			ClassEntry generatedCls = ce.getValue();
			ClassEntry baseCls = base.classes().get(clsName);

			if (baseCls != null) {
				Iterator<Map.Entry<String, MethodEntry>> mit = generatedCls.methods().entrySet().iterator();

				while (mit.hasNext()) {
					Map.Entry<String, MethodEntry> me = mit.next();
					String methodRef = me.getKey();
					MethodEntry generatedMethod = me.getValue();
					MethodEntry baseMethod = baseCls.methods().get(methodRef);

					if (baseMethod != null && Objects.equals(baseMethod.exceptions(), generatedMethod.exceptions())) {
						mit.remove();
					}
				}

				if (generatedCls.methods().isEmpty()) {
					cit.remove();
				}
			} else if (generatedCls.methods().isEmpty()) {
				cit.remove();
			}
		}

		if (!generated.classes().isEmpty()) {
			Map<String, ClassEntry> sortedClasses = new TreeMap<>(generated.classes());
			generated.classes().clear();
			generated.classes().putAll(sortedClasses);

			for (ClassEntry generatedClass : generated.classes().values()) {
				Map<String, MethodEntry> sortedMethods = new TreeMap<>(generatedClass.methods());
				generatedClass.methods().clear();
				generatedClass.methods().putAll(sortedMethods);
			}

			ExceptorIo.write(output.toPath(), generated);
		} else if (output.exists()) {
			getProject().delete(output);
		}
	}
}
