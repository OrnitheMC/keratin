package net.ornithemc.keratin.api.task.generation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import net.fabricmc.mappingio.MappedElementKind;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingWriter;
import net.fabricmc.mappingio.adapter.ForwardingMappingVisitor;
import net.fabricmc.mappingio.adapter.MappingNsCompleter;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

import net.ornithemc.keratin.api.task.mapping.Mapper;

public interface AnonymousClassReindexer {

	default void reindexAnonymousClasses(File jarFile, File mappingsInFile, File mappingsOutFile, String namespace) throws IOException {
		_reindexAnonymousClasses(jarFile, mappingsInFile, mappingsOutFile, namespace);
	}

	static void _reindexAnonymousClasses(File jarFile, File mappingsInFile, File mappingsOutFile, String namespace) throws IOException {
		Map<String, String> newIndices = new HashMap<>();

		try (JarInputStream js = new JarInputStream(new FileInputStream(jarFile))) {
			for (JarEntry entry; (entry = js.getNextJarEntry()) != null; ) {
				if (entry.getName().endsWith(".class")) {
					ClassReader reader = new ClassReader(js);
					ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9) {

						private String outerClass;
						private int nextAnonymousClassIndex = 1;

						@Override
						public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
							outerClass = name + "$";
						}

						@Override
						public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
							return new MethodVisitor(Opcodes.ASM9) {

								@Override
								public void visitTypeInsn(int opcode, String type) {
									if (opcode == Opcodes.NEW && type.startsWith(outerClass)) {
										String innerName = type.substring(outerClass.length());

										if (isAnonymousClassIndex(innerName)) {
											newIndices.put(type, Integer.toString(nextAnonymousClassIndex++));
										}
									}
								}

								private boolean isAnonymousClassIndex(String innerName) {
									for (int i = 0; i < innerName.length(); i++) {
										if (!Character.isDigit(innerName.charAt(i))) {
											return false;
										}
									}

									return true;
								}
							};
						}
					};

					reader.accept(visitor, ClassReader.SKIP_DEBUG);
				}
			}
		}

		String sourceNs = MappingReader.getNamespaces(mappingsInFile.toPath()).get(0);

		MemoryMappingTree mappingsIn = new MemoryMappingTree();
		MappingReader.read(mappingsInFile.toPath(), new MappingSourceNsSwitch(mappingsIn, namespace));

		MemoryMappingTree mappingsOut = new MemoryMappingTree();
		mappingsIn.accept(new MappingNsCompleter(new ForwardingMappingVisitor(new MappingSourceNsSwitch(mappingsOut, sourceNs, true)) {

			private List<String> newDstIndices = new ArrayList<>();

			@Override
			public void visitNamespaces(String srcNamespace, List<String> dstNamespaces) throws IOException {
				super.visitNamespaces(srcNamespace, dstNamespaces);
			}

			@Override
			public boolean visitClass(String srcName) throws IOException {
				newDstIndices.clear();

				for (int i = -1; (i = srcName.indexOf('$', i + 1)) > 0; ) {
					String outerName = srcName.substring(0, i);
					newDstIndices.add(newIndices.get(outerName));
				}
				newDstIndices.add(newIndices.get(srcName));

				return super.visitClass(srcName);
			}

			@Override
			public void visitDstName(MappedElementKind targetKind, int namespace, String name) throws IOException {
				if (targetKind == MappedElementKind.CLASS) {
					StringBuilder sb = new StringBuilder();

					int from = 0;
					int to = 0;

					for (int i = 0; i < newDstIndices.size(); i++) {
						from = to;
						to = name.indexOf('$', to + 1);

						String newIndex = newDstIndices.get(i);

						if (newIndex == null) {
							if (to < 0) {
								to = name.length();
							}

							sb.append(name.substring(from, to));
						} else {
							sb.append('$');
							sb.append(newIndex);
						}
					}

					name = sb.toString();
				}

				super.visitDstName(targetKind, namespace, name);
			}
		}, Map.of(Mapper.NAMED, Mapper.INTERMEDIARY)));

		try (MappingWriter writer = MappingWriter.create(mappingsOutFile.toPath(), MappingFormat.TINY_2_FILE)) {
			mappingsOut.accept(writer);
		}
	}
}
