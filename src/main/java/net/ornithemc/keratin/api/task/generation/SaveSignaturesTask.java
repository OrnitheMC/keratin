package net.ornithemc.keratin.api.task.generation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.gradle.workers.WorkQueue;

import io.github.gaming32.signaturechanger.tree.MemberReference;
import io.github.gaming32.signaturechanger.tree.SignatureInfo;
import io.github.gaming32.signaturechanger.tree.SigsClass;
import io.github.gaming32.signaturechanger.tree.SigsFile;
import io.github.gaming32.signaturechanger.visitor.SigsFileWriter;
import io.github.gaming32.signaturechanger.visitor.SigsReader;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.processing.Exceptor;

public abstract class SaveSignaturesTask extends MinecraftTask implements Exceptor {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		if (minecraftVersion.hasSharedObfuscation()) {
			saveSignatures(
				files.getBaseMergedSignatures(minecraftVersion),
				files.getGeneratedMergedSignatures(minecraftVersion),
				files.getMergedSignatures(minecraftVersion)
			);
		} else {
			if (minecraftVersion.hasClient()) {
				saveSignatures(
					files.getBaseClientSignatures(minecraftVersion),
					files.getGeneratedClientSignatures(minecraftVersion),
					files.getClientSignatures(minecraftVersion)
				);
			}
			if (minecraftVersion.hasServer()) {
				saveSignatures(
					files.getBaseServerSignatures(minecraftVersion),
					files.getGeneratedServerSignatures(minecraftVersion),
					files.getServerSignatures(minecraftVersion)
				);
			}
		}
	}

	private void saveSignatures(File inputBase, File inputGenerated, File output) throws IOException {
		SigsFile base = new SigsFile();
		SigsFile generated = new SigsFile();

		try (SigsReader sr = new SigsReader(Files.newBufferedReader(inputBase.toPath()))) {
			sr.accept(base);
		}
		try (SigsReader sr = new SigsReader(Files.newBufferedReader(inputGenerated.toPath()))) {
			sr.accept(generated);
		}

		Iterator<Map.Entry<String, SigsClass>> cit = generated.classes.entrySet().iterator();

		while (cit.hasNext()) {
			Map.Entry<String, SigsClass> ce = cit.next();
			String clsName = ce.getKey();
			SigsClass generatedCls = ce.getValue();
			SigsClass baseCls = base.classes.get(clsName);

			if (baseCls != null) {
				Iterator<Map.Entry<MemberReference, SignatureInfo>> mit = generatedCls.members.entrySet().iterator();

				while (mit.hasNext()) {
					Map.Entry<MemberReference, SignatureInfo> me = mit.next();
					MemberReference memberRef = me.getKey();
					SignatureInfo generatedMember = me.getValue();
					SignatureInfo baseMember = baseCls.members.get(memberRef);

					if (Objects.equals(baseMember, generatedMember)) {
						mit.remove();
					}
				}

				if (generatedCls.members.isEmpty() && Objects.equals(baseCls.signatureInfo, generatedCls.signatureInfo)) {
					cit.remove();
				}
			}
		}

		if (!generated.classes.isEmpty()) {
			Map<String, SigsClass> sortedClasses = new TreeMap<>(generated.classes);
			generated.classes.clear();
			generated.classes.putAll(sortedClasses);

			for (SigsClass generatedClass : generated.classes.values()) {
				Map<MemberReference, SignatureInfo> sortedMembers = new TreeMap<>((r1, r2) -> {
					int c = r1.name().compareTo(r2.name());
					return c != 0 ? c : r1.desc().getDescriptor().compareTo(r2.desc().getDescriptor());
				});
				sortedMembers.putAll(generatedClass.members);
				generatedClass.members.clear();
				generatedClass.members.putAll(sortedMembers);
			}

			try (SigsFileWriter sw = new SigsFileWriter(Files.newBufferedWriter(output.toPath()))) {
				generated.accept(sw);
			}
		} else if (output.exists()) {
			getProject().delete(output);
		}
	}
}
