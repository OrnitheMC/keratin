package net.ornithemc.keratin.api.task.setup;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import net.fabricmc.mappingio.MappedElementKind;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingWriter;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MappingTree.ClassMapping;
import net.fabricmc.mappingio.tree.MappingTree.MethodArgMapping;
import net.fabricmc.mappingio.tree.MappingTree.MethodMapping;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public interface MappingsFiller {

	default void fillMappings(File input, File output, File jar, Collection<File> libraries, String namespace) throws IOException {
		_fillMappings(input, output, jar, libraries, namespace);
	}

	static void _fillMappings(File input, File output, File jar, Collection<File> libraries, String namespace) throws IOException {
		MemoryMappingTree mappings = new MemoryMappingTree();
		MappingReader.read(input.toPath(), mappings);

		new MethodMappingPropagator(mappings, namespace).run(jar, libraries);

		try (MappingWriter writer = MappingWriter.create(output.toPath(), MappingFormat.TINY_2_FILE)) {
			mappings.accept(writer);
		}
	}

	class MethodMappingPropagator {

		private final MemoryMappingTree mappings;
		private final int namespace;
		private final boolean named;

		private final Set<String> jarClasses;
		private final Map<String, Set<String>> superClasses;
		private final Map<String, Set<String>> subClasses;
		private final Map<String, Set<String>> methodsByClass;
		private final Map<String, Map<String, String>> bridgeMethodsByClass;

		public MethodMappingPropagator(MemoryMappingTree mappings, String dstNs) throws IOException {
			this.mappings = mappings;
			this.namespace = this.mappings.getNamespaceId(dstNs);
			this.named = "named".equals(dstNs);

			this.jarClasses = new HashSet<>();
			this.superClasses = new HashMap<>();
			this.subClasses = new HashMap<>();
			this.methodsByClass = new HashMap<>();
			this.bridgeMethodsByClass = new HashMap<>();
		}

		public void run(File jar, Collection<File> libraries) throws IOException {
			readJar(jar, true);
			for (File library : libraries) {
				readJar(library, false);
			}
			if (named) {
				findBridgeMethods();
			}
			propagateMappings();
		}

		private void readJar(File jar, boolean main) throws IOException {
			try (JarInputStream js = new JarInputStream(new FileInputStream(jar))) {
				for (JarEntry entry; (entry = js.getNextJarEntry()) != null; ) {
					if (entry.getName().endsWith(".class")) {
						ClassReader reader = new ClassReader(js);
						ClassVisitor visitor = new ClassParser(Opcodes.ASM9, main);

						reader.accept(visitor, ClassReader.SKIP_DEBUG);
					}
				}
			}
		}

		private void readJavaClass(String className) {
			if (className.startsWith("java/")) {
				try {
					ClassReader reader = new ClassReader(className);
					ClassVisitor visitor = new ClassParser(Opcodes.ASM9, false);

					reader.accept(visitor, ClassReader.SKIP_DEBUG);
				} catch (IOException ignored) {
				}
			}
		}

		private Set<String> getSuperClasses(String className) {
			if (!superClasses.containsKey(className)) {
				readJavaClass(className);
			}

			return superClasses.getOrDefault(className, Collections.emptySet());
		}

		private void findBridgeMethods() {
			for (Map.Entry<String, Map<String, String>> e : bridgeMethodsByClass.entrySet()) {
				String className = e.getKey();
				Map<String, String> bridgeMethods = e.getValue();

				Iterator<Map.Entry<String, String>> it = bridgeMethods.entrySet().iterator();

				while (it.hasNext()) {
					Map.Entry<String, String> method = it.next();
					String bridge = method.getKey();
					String specialized = method.getValue();

					if (methodExistsInSuperClasses(className, bridge)) {
						int i = bridge.indexOf('(');
						String bridgeDescriptor = bridge.substring(i);
						int j = specialized.indexOf('(');
						String specializedDescriptor = specialized.substring(j);
						
						if (isBridgeMethod(bridgeDescriptor, specializedDescriptor)) {
							continue;
						}
					}

					it.remove();
				}
			}
		}

		private boolean methodExistsInSuperClasses(String className, String method) {
			for (String superClass : getSuperClasses(className)) {
				Set<String> methods = methodsByClass.get(className);

				if (methods.contains(method) || methodExistsInSuperClasses(superClass, method)) {
					return true;
				}
			}

			return false;
		}

		private boolean isBridgeMethod(String bridgeDescriptor, String specializedDescriptor) {
			Type bridgeType = Type.getType(bridgeDescriptor);
			Type specializedType = Type.getType(specializedDescriptor);

			Type[] bridgeArgTypes = bridgeType.getArgumentTypes();
			Type bridgeReturnType = bridgeType.getReturnType();
			Type[] specializedArgTypes = specializedType.getArgumentTypes();
			Type specializedReturnType = specializedType.getReturnType();

			if (bridgeArgTypes.length != specializedArgTypes.length) {
				return false;
			}

			for (int i = 0; i < bridgeArgTypes.length; i++) {
				if (!areTypesBridgeCompatible(bridgeArgTypes[i], specializedArgTypes[i])) {
					return false;
				}
			}

			return areTypesBridgeCompatible(bridgeReturnType, specializedReturnType);
		}

		private boolean areTypesBridgeCompatible(Type typeForBridge, Type typeForSpecialized) {
			if (typeForBridge.equals(typeForSpecialized)) {
				return true;
			}
			if (typeForBridge.getSort() != typeForSpecialized.getSort()) {
				return false;
			}

			switch (typeForBridge.getSort()) {
			case Type.OBJECT:
				return areClassesBridgeCompatible(typeForBridge.getInternalName(), typeForSpecialized.getInternalName());
			case Type.ARRAY:
				if (typeForBridge.getDimensions() != typeForSpecialized.getDimensions()) {
					return false;
				}

				return areTypesBridgeCompatible(typeForBridge.getElementType(), typeForSpecialized.getElementType());
			}

			return false;
		}

		private boolean areClassesBridgeCompatible(String classNameForBridge, String classNameForSpecialized) {
			if (classNameForBridge.equals(classNameForSpecialized)) {
				return true;
			}
			if ("java/lang/Object".equals(classNameForSpecialized)) {
				return false;
			}

			for (String superClassNameForSpecialized : getSuperClasses(classNameForSpecialized)) {
				if (areClassesBridgeCompatible(classNameForBridge, superClassNameForSpecialized)) {
					return true;
				}
			}

			return false;
		}

		private void propagateMappings() {
			for (Map.Entry<String, Set<String>> e : methodsByClass.entrySet()) {
				String className = e.getKey();
				Set<String> methods = e.getValue();
				Map<String, String> bridgeMethods = bridgeMethodsByClass.getOrDefault(className, Collections.emptyMap());

				for (String method : methods) {
					int i = method.indexOf('(');
					String methodName = method.substring(0, i);
					String methodDescriptor = method.substring(i);

					MethodMapping methodMapping = mappings.getMethod(className, methodName, methodDescriptor);

					String methodDstName = null;
					List<String> methodArgDstNames = new ArrayList<>();

					if (methodMapping != null) {
						methodDstName = methodMapping.getDstName(namespace);

						for (MethodArgMapping a : methodMapping.getArgs()) {
							String argDstName = a.getDstName(namespace);

							if (argDstName != null && argDstName.startsWith("p_")) {
								while (a.getLvIndex() > methodArgDstNames.size()) {
									methodArgDstNames.add(null);
								}

								methodArgDstNames.add(a.getDstName(namespace));
							}
						}
					}

					if (methodDstName == null && bridgeMethods.containsKey(methodName + methodDescriptor)) {
						methodDstName = methodName;
					}

					if (methodDstName != null) {
						propagate(className, methodName, methodDescriptor, methodDstName, methodArgDstNames);
					}
				}
			}
		}

		private void propagate(String className, String methodName, String methodDescriptor, String methodDstName, List<String> methodArgDstNames) {
			if (!jarClasses.contains(className)) {
				return; // this class does not appear in the jar, but on of the libraries (thanks realms...)
			}

			Set<String> methods = methodsByClass.get(className);

			if (methods.contains(methodName + methodDescriptor)) {
				ClassMapping classMapping = mappings.getClass(className);
				MethodMapping methodMapping = mappings.getMethod(className, methodName, methodDescriptor);

				if (methodMapping == null || !Objects.equals(methodDstName, methodMapping.getDstName(namespace))) {
					mappings.visitClass(className);
					if (classMapping == null) {
						mappings.visitDstName(MappedElementKind.CLASS, namespace, className);
					}
					mappings.visitMethod(methodName, methodDescriptor);
					mappings.visitDstName(MappedElementKind.METHOD, namespace, methodDstName);

					for (int lvIndex = 0; lvIndex < methodArgDstNames.size(); lvIndex++) {
						String methodArgDstName = methodArgDstNames.get(lvIndex);

						if (methodArgDstName != null) {
							mappings.visitMethodArg(-1, lvIndex, null);
							mappings.visitDstName(MappedElementKind.METHOD_ARG, namespace, methodArgDstName);
						}
					}
				}
			}

			Set<String> implementers = subClasses.getOrDefault(className, Collections.emptySet());

			for (String implementer : implementers) {
				MethodMapping methodMapping = mappings.getMethod(implementer, methodName, methodDescriptor);

				if (methodMapping == null || !Objects.equals(methodDstName, methodMapping.getDstName(namespace))) {
					propagate(implementer, methodName, methodDescriptor, methodDstName, methodArgDstNames);
				}
			}

			Map<String, String> bridgeMethods = bridgeMethodsByClass.getOrDefault(className, Collections.emptyMap());
			String specializedMethod = bridgeMethods.get(methodName + methodDescriptor);

			if (specializedMethod != null) {
				int i = specializedMethod.indexOf('(');
				String specializedName = specializedMethod.substring(0, i);
				String specializedDescriptor = specializedMethod.substring(i);

				propagate(className, specializedName, specializedDescriptor, methodDstName, methodArgDstNames);
			}
		}

		private class ClassParser extends ClassVisitor {

			private final boolean main;

			public ClassParser(int api, boolean main) {
				super(api);

				this.main = main;
			}

			private String className;
			private Set<String> methods;
			private Map<String, String> bridgeMethods;

			@Override
			public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
				className = name;

				if (main) {
					jarClasses.add(name);
				}

				superClasses.computeIfAbsent(name, key -> new HashSet<>()).add(superName);
				for (String itf : interfaces) {
					superClasses.computeIfAbsent(name, key -> new HashSet<>()).add(itf);
				}

				subClasses.computeIfAbsent(superName, key -> new HashSet<>()).add(name);
				for (String itf : interfaces) {
					subClasses.computeIfAbsent(itf, key -> new HashSet<>()).add(name);
				}

				methods = methodsByClass.computeIfAbsent(name, key -> new HashSet<>());
				if (main && named) {
					bridgeMethods = bridgeMethodsByClass.computeIfAbsent(name, key -> new HashMap<>());
				}
			}

			@Override
			public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
				// we only care for methods that can be inherited, so no private or static methods
				if ((access & Opcodes.ACC_PRIVATE) == 0 && (access & Opcodes.ACC_STATIC) == 0) {
					// bridge methods are compiler generated, but the obfuscator might have removed
					// the bridge access flag, in which case it must still not be final
					if (methods.add(name + descriptor) && main && named && ((access & Opcodes.ACC_BRIDGE) != 0 || (access & Opcodes.ACC_FINAL) == 0)) {
						return new MethodVisitor(Opcodes.ASM9) {

							private int invocations;
							private boolean potentialBridge = true;
							private String potentialSpecializedMethod;

							@Override
							public void visitInsn(int opcode) {
								switch (opcode) {
								case Opcodes.IRETURN:
								case Opcodes.LRETURN:
								case Opcodes.FRETURN:
								case Opcodes.DRETURN:
								case Opcodes.ARETURN:
								case Opcodes.RETURN:
									break;
								default:
									potentialBridge = false;
								}
							}

							@Override
							public void visitIntInsn(int opcode, int operand) {
								potentialBridge = false;
							}

							@Override
							public void visitVarInsn(int opcode, int varIndex) {
								switch (opcode) {
								case Opcodes.ILOAD:
								case Opcodes.LLOAD:
								case Opcodes.FLOAD:
								case Opcodes.DLOAD:
								case Opcodes.ALOAD:
									break;
								default:
									potentialBridge = false;
								}
							}

							@Override
							public void visitTypeInsn(int opcode, String type) {
								if (opcode != Opcodes.CHECKCAST) {
									potentialBridge = false;
								}
							}

							@Override
							public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
								potentialBridge = false;
							}

							@Override
							public void visitMethodInsn(int opcode, String invokedMethodOwner, String invokedMethodName, String invokedMethodDescriptor, boolean isInterface) {
								switch (opcode) {
								case Opcodes.INVOKEINTERFACE:
								case Opcodes.INVOKESPECIAL:
								case Opcodes.INVOKEVIRTUAL:
									if (++invocations == 1) {
										if (invokedMethodOwner.equals(className) && !invokedMethodDescriptor.equals(descriptor)) {
											potentialSpecializedMethod = invokedMethodName + invokedMethodDescriptor;
										}
									} else {
										potentialBridge = false;
									}
									break;
								default:
									potentialBridge = false;
								}
							}

							@Override
							public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
								potentialBridge = false;
							}

							@Override
							public void visitJumpInsn(int opcode, Label label) {
								potentialBridge = false;
							}

							@Override
							public void visitEnd() {
								if (potentialBridge && potentialSpecializedMethod != null) {
									bridgeMethods.put(name + descriptor, potentialSpecializedMethod);
								}
							}
						};
					}
				}

				return null;
			}
		}
	}
}
