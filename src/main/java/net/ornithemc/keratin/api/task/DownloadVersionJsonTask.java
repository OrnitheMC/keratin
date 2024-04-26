package net.ornithemc.keratin.api.task;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

import org.apache.commons.io.FileUtils;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskAction;

import net.ornithemc.keratin.manifest.VersionsManifest;

public abstract class DownloadVersionJsonTask extends KeratinTask {

	public abstract Property<String> getUrl();

	public abstract Property<File> getOutput();

	public VersionsManifest.Entry findManifestEntry(String minecraftVersion) {
		VersionsManifest manifest = getExtension().getVersionsManifest();
		Optional<VersionsManifest.Entry> entry = manifest.find(minecraftVersion);

		return entry.orElseThrow(() -> new RuntimeException("versions manifest does not have an entry for Minecraft version " + minecraftVersion));
	}

	@TaskAction
	public void run() throws IOException {
		getProject().getLogger().lifecycle(":downloading " + getExtension().getMinecraftVersion().get() + " json to " + getOutput().get().getName());

		URL url = new URL(getUrl().get());
		File file = getOutput().get();

		if (!file.exists() || isRefreshDependencies()) {
			FileUtils.copyURLToFile(url, file);
		}
	}
}
