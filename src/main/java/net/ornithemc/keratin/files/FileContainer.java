package net.ornithemc.keratin.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.Callable;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.util.Versioned;

public class FileContainer {

	protected final Project project;
	protected final KeratinGradleExtension keratin;

	protected FileContainer(KeratinGradleExtension keratin) {
		this.project = keratin.getProject();
		this.keratin = keratin;
	}

	protected void mkdirs() throws IOException {
	}

	protected void mkdirs(File dir) throws IOException {
		Files.createDirectories(dir.toPath());
	}

	protected Property<File> fileProperty(Callable<File> provider) {
		Property<File> property = this.project.getObjects().property(File.class);
		property.convention(this.project.provider(provider));
		property.finalizeValueOnRead();
		return property;
	}

	protected int getIntermediaryGen() {
		return keratin.getIntermediaryGen().get();
	}

	protected File pickFileForPresentSides(MinecraftVersion minecraftVersion, Versioned<MinecraftVersion, File> client, Versioned<MinecraftVersion, File> server, Versioned<MinecraftVersion, File> merged) {
		if (minecraftVersion.hasClient() && minecraftVersion.hasServer()) {
			return merged.get(minecraftVersion);
		} else {
			if (minecraftVersion.hasClient()) {
				return client.get(minecraftVersion);
			}
			if (minecraftVersion.hasServer()) {
				return server.get(minecraftVersion);
			}
		}

		throw new RuntimeException("somehow Minecraft version " + minecraftVersion.id() + " is neither client nor server!");
	}
}
