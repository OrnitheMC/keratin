package net.ornithemc.keratin.api.task.build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class CompressMappingsTask extends MinecraftTask {

	@Internal
	public abstract Property<File> getMappings();

	@Internal
	public abstract Property<File> getCompressedMappings();

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws IOException {
		File file = getMappings().get();
		File compressedFile = getCompressedMappings().get();

		try (FileInputStream fis = new FileInputStream(file); FileOutputStream fos = new FileOutputStream(compressedFile); GZIPOutputStream os = new GZIPOutputStream(fos)) {
			byte[] buffer = new byte[1024];

			for (int length; (length = fis.read(buffer)) > 0; ) {
				os.write(buffer, 0, length);
			}
		}
	}
}
