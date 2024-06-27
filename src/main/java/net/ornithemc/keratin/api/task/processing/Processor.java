package net.ornithemc.keratin.api.task.processing;

import java.io.File;
import java.util.Map;

import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;

import com.google.common.io.Files;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.MappingWriter;
import net.fabricmc.mappingio.adapter.MappingNsRenamer;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public interface Processor {

	interface MinecraftProcessorParameters extends WorkParameters {

		Property<File> getInputJar();

		Property<File> getNestsFile();

		Property<File> getNestedJar();

		Property<File> getSparrowFile();

		Property<File> getSignaturePatchedJar();

		Property<File> getOutputJar();

	}

	abstract class ProcessMinecraft implements WorkAction<MinecraftProcessorParameters>, Nester, SignaturePatcher {

		@Override
		public void execute() {
			try {
				File data;
				File jarIn;
				File jarOut = getParameters().getInputJar().get();

				data = getParameters().getNestsFile().getOrNull();

				if (data != null) {
					jarIn = jarOut;
					jarOut = getParameters().getNestedJar().get();

					nestJar(jarIn, jarOut, data);
				}

				data = getParameters().getSparrowFile().getOrNull();

				if (data != null) {
					jarIn = jarOut;
					jarOut = getParameters().getSignaturePatchedJar().get();

					signaturePatchJar(jarIn, jarOut, data);
				}

				jarIn = jarOut;
				jarOut = getParameters().getOutputJar().get();

				Files.copy(jarIn, jarOut);
			} catch (Exception e) {
				throw new RuntimeException("error while processing Minecraft", e);
			}
		}
	}

	interface MappingsProcessorParameters extends WorkParameters {

		Property<File> getClientInputMappings();

		Property<File> getServerInputMappings();

		Property<File> getMergedInputMappings();

		Property<File> getClientNestsFile();

		Property<File> getServerNestsFile();

		Property<File> getMergedNestsFile();

		Property<File> getClientNestedMappings();

		Property<File> getServerNestedMappings();

		Property<File> getMergedNestedMappings();

		Property<File> getClientOutputMappings();

		Property<File> getServerOutputMappings();

		Property<File> getMergedOutputMappings();

	}

	abstract class ProcessMappings implements WorkAction<MappingsProcessorParameters>, Nester {

		@Override
		public void execute() {
			try {
				File clientData;
				File serverData;
				File mergedData;
				File clientMappingsIn;
				File serverMappingsIn;
				File mergedMappingsIn;
				File clientMappingsOut = getParameters().getClientInputMappings().getOrNull();
				File serverMappingsOut = getParameters().getServerInputMappings().getOrNull();
				File mergedMappingsOut = getParameters().getMergedInputMappings().getOrNull();

				clientData = getParameters().getClientNestsFile().getOrNull();
				serverData = getParameters().getServerNestsFile().getOrNull();
				mergedData = getParameters().getMergedNestsFile().getOrNull();

				if (clientData != null || serverData != null || mergedData != null) {
					clientMappingsIn = clientMappingsOut;
					serverMappingsIn = serverMappingsOut;
					mergedMappingsIn = mergedMappingsOut;
					clientMappingsOut = getParameters().getClientNestedMappings().getOrNull();
					serverMappingsOut = getParameters().getServerNestedMappings().getOrNull();
					mergedMappingsOut = getParameters().getMergedNestedMappings().getOrNull();

					if (clientData != null) {
						nestMappings(clientMappingsIn, clientMappingsOut, clientData);
					}
					if (serverData != null) {
						nestMappings(serverMappingsIn, serverMappingsOut, serverData);
					}
					if (mergedData != null) {
						nestMappings(mergedMappingsIn, mergedMappingsOut, mergedData);
					}
				}

				clientMappingsIn = clientMappingsOut;
				serverMappingsIn = serverMappingsOut;
				mergedMappingsIn = mergedMappingsOut;
				clientMappingsOut = getParameters().getClientOutputMappings().getOrNull();
				serverMappingsOut = getParameters().getServerOutputMappings().getOrNull();
				mergedMappingsOut = getParameters().getMergedOutputMappings().getOrNull();

				if (clientMappingsIn != null) {
					Files.copy(clientMappingsIn, clientMappingsOut);
				}
				if (serverMappingsIn != null) {
					Files.copy(serverMappingsIn, serverMappingsOut);
				}
				if (mergedMappingsIn != null) {
					Files.copy(mergedMappingsIn, mergedMappingsOut);
				}

				if (clientMappingsOut != null && serverMappingsOut != null && mergedMappingsOut != null) {
					MemoryMappingTree client = new MemoryMappingTree();
					MemoryMappingTree server = new MemoryMappingTree();

					MappingReader.read(clientMappingsOut.toPath(), client);
					MappingReader.read(serverMappingsOut.toPath(), server);

					MemoryMappingTree merged = new MemoryMappingTree();

					MappingVisitor clientVisitor = new MappingNsRenamer(merged, Map.of("official", "clientOfficial"));
					clientVisitor = new MappingSourceNsSwitch(clientVisitor, "intermediary");
					MappingVisitor serverVisitor = new MappingNsRenamer(merged, Map.of("official", "serverOfficial"));
					serverVisitor = new MappingSourceNsSwitch(serverVisitor, "intermediary");

					client.accept(clientVisitor);
					server.accept(serverVisitor);

					try (MappingWriter writer = MappingWriter.create(mergedMappingsOut.toPath(), MappingFormat.TINY_2_FILE)) {
						merged.accept(writer);
					}
				}
			} catch (Exception e) {
				throw new RuntimeException("error while processing mappings", e);
			}
		}
	}
}
