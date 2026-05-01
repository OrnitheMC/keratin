package net.ornithemc.keratin.api.task.mapping;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import net.fabricmc.mappingio.MappedElementKind;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingWriter;
import net.fabricmc.mappingio.adapter.MappingDstNsReorder;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MappingTree.ClassMapping;
import net.fabricmc.mappingio.tree.MappingTree.MethodMapping;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public interface LambdaMethodMapper {

	default void fillLambdaMethodMappings(File input, File output, File jar, String namespace) throws IOException {
		_fillLambdaMethodMappings(input, output, jar, namespace);
	}

	static void _fillLambdaMethodMappings(File input, File output, File jar, String namespace) throws IOException {
		MemoryMappingTree mappings = new MemoryMappingTree();
		MappingReader.read(input.toPath(), new MappingDstNsReorder(mappings, List.of(namespace)));

		new MethodMapper(mappings, namespace).run(jar);

		try (MappingWriter writer = MappingWriter.create(output.toPath(), MappingFormat.TINY_2_FILE)) {
			mappings.accept(writer);
		}
	}

	class MethodMapper {

		private final MemoryMappingTree mappings;
		private final int namespace;

		private final Map<String, Map<String, List<String>>> lambdaMethodsByClass;

		public MethodMapper(MemoryMappingTree mappings, String dstNs) throws IOException {
			this.mappings = mappings;
			this.namespace = this.mappings.getNamespaceId(dstNs);

			this.lambdaMethodsByClass = new HashMap<>();
		}

		public void run(File jar) throws IOException {
			findLambdaMethods(jar);
			mapLambdaMethods();
		}

		private void findLambdaMethods(File jar) throws IOException {
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

		private void mapLambdaMethods() {
			for (Map.Entry<String, Map<String, List<String>>> e : lambdaMethodsByClass.entrySet()) {
				String className = e.getKey();
				Map<String, List<String>> lambdaMethods = e.getValue();

				ClassMapping cls = mappings.getClass(className);
				String mappedClassName = cls == null ? null : cls.getDstName(namespace);

				int nextLambdaIndex = 0;

				for (Map.Entry<String, List<String>> me : lambdaMethods.entrySet()) {
					String enclosing = me.getKey();
					String enclosingName = enclosing.substring(0, enclosing.indexOf('('));
					String enclosingDescriptor = enclosing.substring(enclosing.indexOf('('));

					MethodMapping enclosingMethod = mappings.getMethod(className, enclosingName, enclosingDescriptor);
					String mappedEnclosingName = enclosingMethod == null ? null : enclosingMethod.getDstName(namespace);

					String mappedLambdaNameBase = (mappedEnclosingName == null ? enclosingName : mappedEnclosingName);

					if (mappedLambdaNameBase.equals("<init>")) {
						mappedLambdaNameBase = "lambda$new$";
					} else if (mappedLambdaNameBase.equals("<clinit>")) {
						mappedLambdaNameBase = "lambda$static$";
					} else {
						mappedLambdaNameBase = "lambda$" + mappedLambdaNameBase + "$";
					}

					for (String lambda : me.getValue()) {
						String lambdaName = lambda.substring(0, lambda.indexOf('('));
						String lambdaDescriptor = lambda.substring(lambda.indexOf('('));

						String mappedLambdaName = mappedLambdaNameBase + nextLambdaIndex++;

						mappings.visitClass(className);
						if (mappedClassName == null) {
							mappings.visitDstName(MappedElementKind.CLASS, namespace, className);
						}
						mappings.visitMethod(lambdaName, lambdaDescriptor);
						mappings.visitDstName(MappedElementKind.METHOD, namespace, mappedLambdaName);
					}
				}
			}
		}

		private class ClassParser extends ClassVisitor {

			public ClassParser(int api) {
				super(api);
			}

			private Map<String, List<String>> lambdaMethods;
			private Set<String> potentialLambdaMethods;
			private Map<String, String> potentialEnclosingMethods;

			@Override
			public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
				lambdaMethods = lambdaMethodsByClass.computeIfAbsent(name, key -> new HashMap<>());
				potentialLambdaMethods = new HashSet<>();
				potentialEnclosingMethods = new LinkedHashMap<>();
			}

			@Override
			public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
				if ((access & Opcodes.ACC_PRIVATE) != 0 && (access & Opcodes.ACC_SYNTHETIC) != 0) {
					potentialLambdaMethods.add(name + descriptor);
				}

				return new MethodVisitor(Opcodes.ASM9) {

					@Override
					public void visitInvokeDynamicInsn(String n, String d, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
						String bootstrapOwner = bootstrapMethodHandle.getOwner();
						String bootstrapName = bootstrapMethodHandle.getName();

						if (bootstrapMethodArguments.length == 3 && bootstrapMethodArguments[1] instanceof Handle handle
							&& "java/lang/invoke/LambdaMetafactory".equals(bootstrapOwner) && "metafactory".equals(bootstrapName)
						) {
							potentialEnclosingMethods.put(handle.getName() + handle.getDesc(), name + descriptor);
						}
					}
				};
			}

			@Override
			public void visitEnd() {
				potentialEnclosingMethods.keySet().retainAll(potentialLambdaMethods);

				// first pass: collect all lambdas directly contained by an enclosing method
				Map<String, List<String>> lambdas = new HashMap<>();

				for (Map.Entry<String, String> e : potentialEnclosingMethods.entrySet()) {
					String lambda = e.getKey();
					String enclosing = e.getValue();

					lambdas.computeIfAbsent(enclosing, key -> new ArrayList<>()).add(lambda);
				}

				// second pass: collect all lambdas from a top-level enclosing method, recursively
				for (String enclosing : lambdas.keySet()) {
					if (!potentialEnclosingMethods.containsKey(enclosing)) {
						lambdaMethods.put(enclosing, collectLambdas(enclosing, lambdas));
					}
				}
			}

			private List<String> collectLambdas(String enclosing, Map<String, List<String>> hierarchy) {
				List<String> lambdas = new ArrayList<>();
				collectLambdas(enclosing, hierarchy, lambdas);
				return lambdas;
			}

			private void collectLambdas(String enclosing, Map<String, List<String>> hierarchy, List<String> lambdas) {
				List<String> contained = hierarchy.get(enclosing);

				if (contained != null) {
					for (String lambda : contained) {
						if (lambdas.add(lambda)) {
							collectLambdas(lambda, hierarchy, lambdas);
						}
					}
				}
			}
		}
	}
}
