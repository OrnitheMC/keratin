package net.ornithemc.keratin.api.task.mapping;

import java.io.File;
import java.io.IOException;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingWriter;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

import net.ornithemc.keratin.api.task.KeratinTask;

public abstract class ConvertMappingsFromTinyV1ToTinyV2Task extends KeratinTask {

	@InputFile
	public abstract Property<File> getInput();

	@OutputFile
	public abstract Property<File> getOutput();

	@TaskAction
	public void run() throws IOException {
		File input = getInput().get();
		File output = getOutput().get();

		MemoryMappingTree mappings = new MemoryMappingTree();
		MappingReader.read(input.toPath(), mappings);
		try (MappingWriter mappingWriter = MappingWriter.create(output.toPath(), MappingFormat.TINY_2_FILE)) {
			mappings.accept(mappingWriter);
		}
	}
}
