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
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.matching.Matches;
import net.ornithemc.keratin.matching.MatchesUtil;

public abstract class MakeSetupSignaturesTask extends MinecraftTask {

	@Internal
	public abstract Property<String> getFromMinecraftVersion();

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (details.sharedMappings()) {
			File sigs = files.getMergedSignatures(minecraftVersion);
			File setup = files.getSetupMergedSignatures(minecraftVersion);

			if (sigs.exists()) {
				Files.copy(sigs, setup);
			} else {
				String fromMinecraftVersion = getFromMinecraftVersion().getOrNull();

				if (fromMinecraftVersion == null) {
					setup.createNewFile();
				} else {
					VersionDetails fromDetails = keratin.getVersionDetails(fromMinecraftVersion);

					if (fromDetails.sharedMappings()) {
						File fromSigs = files.getMergedSignatures(fromMinecraftVersion);

						updateSignatures(
							fromMinecraftVersion,
							"merged",
							minecraftVersion,
							"merged",
							fromSigs,
							setup
						);
					} else {
						throw new RuntimeException("cannot update from <1.3 version to >=1.3 version!");
					}
				}
			}
		} else {
			if (details.client()) {
				File sigs = files.getClientSignatures(minecraftVersion);
				File setup = files.getSetupClientSignatures(minecraftVersion);

				if (sigs.exists()) {
					Files.copy(sigs, setup);
				} else {
					String fromMinecraftVersion = getFromMinecraftVersion().getOrNull();

					if (fromMinecraftVersion == null) {
						setup.createNewFile();
					} else {
						VersionDetails fromDetails = keratin.getVersionDetails(fromMinecraftVersion);

						if (fromDetails.sharedMappings()) {
							throw new RuntimeException("cannot update from <1.3 version to >=1.3 version!");
						} else if (fromDetails.client()) {
							File fromSigs = files.getClientSignatures(fromMinecraftVersion);

							updateSignatures(
								fromMinecraftVersion,
								"client",
								minecraftVersion,
								"client",
								fromSigs,
								setup
							);
						}
					}
				}
			}
			if (details.server()) {
				File sigs = files.getServerSignatures(minecraftVersion);
				File setup = files.getSetupServerSignatures(minecraftVersion);

				if (sigs.exists()) {
					Files.copy(sigs, setup);
				} else {
					String fromMinecraftVersion = getFromMinecraftVersion().getOrNull();

					if (fromMinecraftVersion == null) {
						setup.createNewFile();
					} else {
						VersionDetails fromDetails = keratin.getVersionDetails(fromMinecraftVersion);

						if (fromDetails.sharedMappings()) {
							throw new RuntimeException("cannot update from <1.3 version to >=1.3 version!");
						} else if (fromDetails.server()) {
							File fromSigs = files.getServerSignatures(fromMinecraftVersion);

							updateSignatures(
								fromMinecraftVersion,
								"server",
								minecraftVersion,
								"server",
								fromSigs,
								setup
							);
						}
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
