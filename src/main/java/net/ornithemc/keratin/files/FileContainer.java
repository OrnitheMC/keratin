package net.ornithemc.keratin.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Function;

import org.gradle.api.Project;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;

public class FileContainer {

	protected final Project project;
	protected final KeratinGradleExtension keratin;
	protected final KeratinFiles files;

	protected FileContainer(KeratinGradleExtension keratin, KeratinFiles files) {
		this.project = keratin.getProject();
		this.keratin = keratin;
		this.files = files;
	}

	protected void mkdirs() throws IOException {
	}

	protected void mkdirs(File dir) throws IOException {
		Files.createDirectories(dir.toPath());
	}

	protected int getIntermediaryGen() {
		return keratin.getIntermediaryGen().get();
	}

	protected File pickFileForPresentSides(MinecraftVersion minecraftVersion, Function<MinecraftVersion, File> client, Function<MinecraftVersion, File> server, Function<MinecraftVersion, File> merged) {
		if (minecraftVersion.hasClient() && minecraftVersion.hasServer()) {
			return merged.apply(minecraftVersion);
		} else {
			if (minecraftVersion.hasClient()) {
				return client.apply(minecraftVersion);
			}
			if (minecraftVersion.hasServer()) {
				return server.apply(minecraftVersion);
			}
		}

		throw new RuntimeException("somehow Minecraft version " + minecraftVersion.id() + " is neither client nor server!");
	}
}
