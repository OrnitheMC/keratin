package net.ornithemc.keratin.api.task.build;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.adapter.MappingDstNsReorder;
import net.fabricmc.mappingio.tree.MappingTree.FieldMapping;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public interface FieldMappingChecker {

	default void checkFieldMappings(File mappings, File jar, String namespace) throws IOException {
		_checkFieldMappings(mappings, jar, namespace);
	}

	static void _checkFieldMappings(File mappings, File jar, String namespace) throws IOException {
		MemoryMappingTree mappingTree = new MemoryMappingTree();
		MappingReader.read(mappings.toPath(), new MappingDstNsReorder(mappingTree, List.of(namespace)));

		new MappingChecker(mappingTree, namespace).run(jar);
	}

	class MappingChecker {

		private static final String SEPARATOR = "::";

		private final MemoryMappingTree mappings;
		private final int namespace;

		private final Map<String, String> superClasses;
		private final Map<String, Set<String>> fieldsByClass;
		private final Map<String, Set<String>> mappedFieldsByClass;
		private final Map<String, Set<String>> mappedFieldNamesByClass;
		private final Map<String, Set<String>> fieldReferences;

		private int foundIssues;

		public MappingChecker(MemoryMappingTree mappings, String dstNs) throws IOException {
			this.mappings = mappings;
			this.namespace = this.mappings.getNamespaceId(dstNs);

			this.superClasses = new HashMap<>();
			this.fieldsByClass = new HashMap<>();
			this.mappedFieldsByClass = new HashMap<>();
			this.mappedFieldNamesByClass = new HashMap<>();
			this.fieldReferences = new HashMap<>();
		}

		private void logIssue(String issue) {
			foundIssues++;
			System.err.println(issue);
		}

		public void run(File jar) throws IOException {
			readJar(jar);
			checkFieldMappings();

			if (foundIssues > 0) {
				throw new IOException("found " + foundIssues + " issues with field mappings!");
			}
		}

		private void readJar(File jar) throws IOException {
			try (JarInputStream js = new JarInputStream(new FileInputStream(jar))) {
				for (JarEntry entry; (entry = js.getNextJarEntry()) != null; ) {
					if (entry.getName().endsWith(".class")) {
						ClassReader reader = new ClassReader(js);
						ClassVisitor visitor = new ClassParser(Opcodes.ASM9);

						reader.accept(visitor, ClassReader.SKIP_DEBUG);
					}
				}
			}
		}

		private String mapClassName(String className) {
			return mappings.mapClassName(className, namespace);
		}

		private String mapFieldName(String owner, String name, String descriptor) {
			FieldMapping field = null;

			do {
				field = mappings.getField(owner, name, descriptor);
				owner = superClasses.get(owner);
			} while (field == null && owner != null);

			if (field != null) {
				String mappedName = field.getName(namespace);

				if (mappedName != null) {
					name = mappedName;
				}
			}

			return name;
		}

		private String findFieldOwner(String className, String field, boolean mapped) {
			Map<String, Set<String>> fieldsMap = mapped ? mappedFieldsByClass : fieldsByClass;

			while (className != null) {
				Set<String> fields = fieldsMap.get(className);

				if (fields != null && fields.contains(field)) {
					break;
				}

				className = superClasses.get(className);
			}

			return className;
		}

		private void checkFieldMappings() {
			for (Map.Entry<String, Set<String>> e : fieldReferences.entrySet()) {
				String className = e.getKey();
				Set<String> fields = e.getValue();

				if (!fieldsByClass.containsKey(className)) {
					continue;
				}

				for (String field : fields) {
					String[] parts = field.split(SEPARATOR);
					String name = parts[0];
					String descriptor = parts[1];

					String mappedName = mapFieldName(className, name, descriptor);
					String mappedDesc = mappings.mapDesc(descriptor, namespace);
					String mappedField = mappedName + SEPARATOR + mappedDesc;

					String owner = findFieldOwner(className, field, false);
					String mappedOwner = findFieldOwner(className, mappedField, true);

					if (!Objects.equals(owner, mappedOwner)) {
						logIssue("field " + mappedName + " in " + mapClassName(mappedOwner) + " is hiding " + mapClassName(owner) + "." + mappedField);
					}
				}
			}
		}

		private class ClassParser extends ClassVisitor {

			private String className;
			private Set<String> fields;
			private Set<String> mappedFields;
			private Set<String> mappedFieldNames;

			public ClassParser(int api) {
				super(api);
			}

			@Override
			public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
				className = name;

				superClasses.put(name, superName);

				fields = fieldsByClass.computeIfAbsent(name, key -> new HashSet<>());
				mappedFields = mappedFieldsByClass.computeIfAbsent(name, key -> new HashSet<>());
				mappedFieldNames = mappedFieldNamesByClass.computeIfAbsent(name, key -> new HashSet<>());
			}

			@Override
			public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
				fields.add(name + SEPARATOR + descriptor);

				name = mapFieldName(className, name, descriptor);
				descriptor = mappings.mapDesc(descriptor, namespace);

				mappedFields.add(name + SEPARATOR + descriptor);

				if (!mappedFieldNames.add(name)) {
					logIssue("duplicate field name " + name + " in class " + mapClassName(className));
				}

				return null;
			}

			@Override
			public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
				return new MethodVisitor(Opcodes.ASM9) {

					@Override
					public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
						fieldReferences.computeIfAbsent(owner, key -> new HashSet<>()).add(name + SEPARATOR + descriptor);
					}
				};
			}
		}
	}
}
