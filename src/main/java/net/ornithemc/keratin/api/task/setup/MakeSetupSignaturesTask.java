package net.ornithemc.keratin.api.task.setup;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.workers.WorkQueue;
import org.objectweb.asm.commons.Remapper;

import com.google.common.io.Files;

import io.github.gaming32.signaturechanger.tree.MemberReference;
import io.github.gaming32.signaturechanger.tree.SignatureInfo;
import io.github.gaming32.signaturechanger.tree.SigsClass;
import io.github.gaming32.signaturechanger.tree.SigsFile;
import io.github.gaming32.signaturechanger.visitor.SigsFileWriter;
import io.github.gaming32.signaturechanger.visitor.SigsReader;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles;
import net.ornithemc.keratin.files.OrnitheFiles;
import net.ornithemc.keratin.matching.Matches;
import net.ornithemc.keratin.matching.MatchesUtil;

public abstract class MakeSetupSignaturesTask extends MinecraftTask {

	@Internal
	public abstract Property<String> getFromMinecraftVersion();

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFiles files = keratin.getFiles();

		ExceptionsAndSignaturesDevelopmentFiles excsAndSigs = files.getExceptionsAndSignaturesDevelopmentFiles();

		MinecraftVersion fromMinecraftVersion = getFromMinecraftVersion().isPresent()
			? MinecraftVersion.parse(keratin, getFromMinecraftVersion().get())
			: null;

		if (minecraftVersion.hasSharedObfuscation()) {
			File sigs = excsAndSigs.getMergedSignaturesFile(minecraftVersion);
			File setup = excsAndSigs.getMergedSignaturesFile(minecraftVersion);

			if (sigs.exists()) {
				Files.copy(sigs, setup);
			} else {
				if (fromMinecraftVersion == null) {
					setup.createNewFile();
				} else if (fromMinecraftVersion.hasSharedObfuscation()) {
					File fromSigs = excsAndSigs.getMergedSignaturesFile(fromMinecraftVersion);

					updateSignatures(
						fromMinecraftVersion.id(),
						"merged",
						minecraftVersion.id(),
						"merged",
						fromSigs,
						setup
					);
				} else {
					throw new RuntimeException("cannot update from <1.3 version to >=1.3 version!");
				}
			}
		} else {
			if (minecraftVersion.hasClient()) {
				File sigs = excsAndSigs.getClientSignaturesFile(minecraftVersion);
				File setup = excsAndSigs.getClientSignaturesFile(minecraftVersion);

				if (sigs.exists()) {
					Files.copy(sigs, setup);
				} else {
					if (fromMinecraftVersion == null) {
						setup.createNewFile();
					} else if (fromMinecraftVersion.hasSharedObfuscation()) {
						throw new RuntimeException("cannot update from <1.3 version to >=1.3 version!");
					} else if (fromMinecraftVersion.hasClient()) {
						File fromSigs = excsAndSigs.getClientSignaturesFile(fromMinecraftVersion);

						updateSignatures(
							fromMinecraftVersion.client().id(),
							"client",
							minecraftVersion.client().id(),
							"client",
							fromSigs,
							setup
						);
					} else {
						setup.createNewFile();
					}
				}
			}
			if (minecraftVersion.hasServer()) {
				File sigs = excsAndSigs.getServerSignaturesFile(minecraftVersion);
				File setup = excsAndSigs.getServerSignaturesFile(minecraftVersion);

				if (sigs.exists()) {
					Files.copy(sigs, setup);
				} else {
					if (fromMinecraftVersion == null) {
						setup.createNewFile();
					} else if (fromMinecraftVersion.hasSharedObfuscation()) {
						throw new RuntimeException("cannot update from <1.3 version to >=1.3 version!");
					} else if (fromMinecraftVersion.hasServer()) {
						File fromSigs = excsAndSigs.getServerSignaturesFile(fromMinecraftVersion);

						updateSignatures(
							fromMinecraftVersion.server().id(),
							"server",
							minecraftVersion.server().id(),
							"server",
							fromSigs,
							setup
						);
					} else {
						setup.createNewFile();
					}
				}
			}
		}
	}

	private void updateSignatures(String fromMinecraftVersion, String fromSide, String toMinecraftVersion, String toSide, File from, File to) throws IOException {
		KeratinGradleExtension keratin = getExtension();

		if (!from.exists()) {
			throw new RuntimeException("signatures for " + fromMinecraftVersion + " to update from do not exist!");
		}

		Matches matches = keratin.findMatches(fromSide, fromMinecraftVersion, toSide, toMinecraftVersion);
		Remapper mapper = MatchesUtil.makeRemapper(matches.file(), matches.inverted());

		SigsFile fromSigs = new SigsFile();
		SigsFile toSigs = new SigsFile();

		try (SigsReader sr = new SigsReader(java.nio.file.Files.newBufferedReader(from.toPath()))) {
			sr.accept(fromSigs);
		}

		for (Map.Entry<String, SigsClass> ce : fromSigs.classes.entrySet()) {
			String fromClsName = ce.getKey();
			String toClsName = mapper.map(fromClsName);

			if (toClsName == null) {
				continue;
			}

			SigsClass fromCls = ce.getValue();
			SigsClass toCls = new SigsClass();

			String fromClsSig = fromCls.signatureInfo.signature();

			if (fromClsSig != null) {
				String toClsSig = mapper.mapSignature(fromClsSig, false);

				if (toClsSig == null) {
					continue;
				}

				toCls.signatureInfo = new SignatureInfo(fromCls.signatureInfo.mode(), toClsSig);
			}

			toSigs.classes.put(toClsName, toCls);

			for (Map.Entry<MemberReference, SignatureInfo> me : fromCls.members.entrySet()) {
				mapper.map(fromClsName);

				String fromMmbName = me.getKey().name();
				String fromMmbDesc = me.getKey().desc().getDescriptor();
				String toMmbName = (fromMmbDesc.charAt(0) == '(')
					? mapper.mapMethodName(fromClsName, fromMmbName, fromMmbDesc)
					: mapper.mapFieldName(fromClsName, fromMmbName, fromMmbDesc);
				String toMmbDesc = mapper.mapDesc(fromMmbDesc);

				if (toMmbName == null || toMmbDesc == null) {
					continue;
				}

				SignatureInfo fromMtd = me.getValue();
				String fromMmbSig = fromMtd.signature();
				String toMmbSig = mapper.mapSignature(fromMmbSig, fromMmbDesc.charAt(0) != '(');

				if (toMmbSig == null) {
					continue;
				}

				SignatureInfo toMtd = new SignatureInfo(fromMtd.mode(), toMmbSig);
				toCls.members.put(new MemberReference(toMmbName, toMmbDesc), toMtd);
			}
		}

		try (SigsFileWriter sw = new SigsFileWriter(java.nio.file.Files.newBufferedWriter(to.toPath()))) {
			toSigs.accept(sw);
		}
	}
}
