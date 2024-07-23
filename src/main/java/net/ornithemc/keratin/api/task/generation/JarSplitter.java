package net.ornithemc.keratin.api.task.generation;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.RecordComponentVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.RecordComponentNode;

import net.fabricmc.stitch.util.StitchUtil;

import net.ornithemc.keratin.api.task.merging.Merger.MergerParameters;

public interface JarSplitter {

	interface SplitterParameters extends WorkParameters {

		Property<File> getClient();

		Property<File> getServer();

		Property<File> getMerged();

	}

	abstract class SplitJar implements WorkAction<MergerParameters>, JarSplitter {

		@Override
		public void execute() {
			File client = getParameters().getClient().get();
			File server = getParameters().getServer().get();
			File merged = getParameters().getMerged().get();

			try {
				splitJar(client, server, merged);
			} catch (IOException e) {
				throw new UncheckedIOException("error while running splitter", e);
			}
		}
	}

	default void splitJar(File client, File server, File merged) throws IOException {
		_splitJar(client, server, merged);
	}

	static void _splitJar(File client, File server, File merged) throws IOException {
		if (!merged.exists()) {
			throw new IllegalStateException("merged jar does not exist!");
		}

		if (client.exists()) client.delete();
		if (server.exists()) server.delete();

		FileSystem clientFs = StitchUtil.getJarFileSystem(client, true).get();
		FileSystem serverFs = StitchUtil.getJarFileSystem(server, true).get();
		FileSystem mergedFs = StitchUtil.getJarFileSystem(merged, false).get();

		ClassSplitter splitter = new ClassSplitter(Opcodes.ASM9);

		for (Path path : Files.walk(mergedFs.getPath("/")).toList()) {
			if (!Files.isRegularFile(path) || !path.getFileName().toString().endsWith(".class")) {
				continue;
			}

			byte[] bytes = Files.readAllBytes(path);
			ClassReader reader = new ClassReader(bytes);

			// for the purposes of generating class/field/method
			// sigs and excs, the code is not needed
			reader.accept(splitter, ClassReader.SKIP_CODE);

			ClassNode c = splitter.getClient();
			ClassNode s = splitter.getServer();

			if (c != null) {
				writeClass(c, clientFs, path);
			}
			if (s != null) {
				writeClass(s, serverFs, path);
			}
		}

		clientFs.close();
		serverFs.close();
		mergedFs.close();
	}

	private static void writeClass(ClassNode cls, FileSystem fs, Path path) throws IOException {
		Path file = fs.getPath(path.toString());

		if (file.getParent() != null) {
			Files.createDirectories(file.getParent());
		}

		ClassWriter writer = new ClassWriter(0);
		cls.accept(writer);

		Files.write(file, writer.toByteArray(), StandardOpenOption.CREATE_NEW);
	}

	class ClassSplitter extends ClassVisitor {

		private static final String ENVIRONMENT_ANNOTATION = "Lnet/fabricmc/api/Environment;";
		private static final String ENVIRONMENT_INTERFACES_ANNOTATION = "Lnet/fabricmc/api/EnvironmentInterfaces;";
		private static final String CLIENT_SIDE = "CLIENT";
		private static final String SERVER_SIDE = "SERVER";
	
		private final int api;

		private final Set<String> notClient;
		private final Set<String> notServer;

		private ClassNode client;
		private ClassNode server;

		private String currentClass;

		public ClassSplitter(int api) {
			super(api);

			this.api = api;

			this.notClient = new HashSet<>();
			this.notServer = new HashSet<>();
		}

		public ClassNode getClient() {
			return client;
		}

		public ClassNode getServer() {
			return server;
		}

		@Override
		public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
			(client = new ClassNode()).visit(version, access, name, signature, superName, interfaces);
			(server = new ClassNode()).visit(version, access, name, signature, superName, interfaces); 

			currentClass = name;
		}

		@Override
		public void visitSource(String source, String debug) {
			if (client != null) client.visitSource(source, debug);
			if (server != null) server.visitSource(source, debug);
		}

		@Override
		public ModuleVisitor visitModule(String name, int access, String version) {
			ModuleVisitor c = (client == null) ? null : client.visitModule(name, access, version);
			ModuleVisitor s = (server == null) ? null : server.visitModule(name, access, version);

			return new ModuleVisitor(api) {

				@Override
				public void visitMainClass(String mainClass) {
					if (c != null) c.visitMainClass(mainClass);
					if (s != null) s.visitMainClass(mainClass);
				}

				@Override
				public void visitPackage(String packaze) {
					if (c != null) c.visitPackage(packaze);
					if (s != null) s.visitPackage(packaze);
				}

				@Override
				public void visitRequire(String module, int access, String version) {
					if (c != null) c.visitRequire(module, access, version);
					if (s != null) s.visitRequire(module, access, version);
				}

				@Override
				public void visitExport(String packaze, int access, String... modules) {
					if (c != null) c.visitExport(packaze, access, modules);
					if (s != null) s.visitExport(packaze, access, modules);
				}

				@Override
				public void visitOpen(String packaze, int access, String... modules) {
					if (c != null) c.visitOpen(packaze, access, modules);
					if (s != null) s.visitOpen(packaze, access, modules);
				}

				@Override
				public void visitUse(String service) {
					if (c != null) c.visitUse(service);
					if (s != null) s.visitUse(service);
				}

				@Override
				public void visitProvide(String service, String... providers) {
					if (c != null) c.visitProvide(service, providers);
					if (s != null) s.visitProvide(service, providers);
				}

				@Override
				public void visitEnd() {
					if (c != null) c.visitEnd();
					if (s != null) s.visitEnd();
				}
			};
		}

		@Override
		public void visitNestHost(String nestHost) {
			if (client != null) client.visitNestHost(nestHost);
			if (server != null) server.visitNestHost(nestHost);
		}

		@Override
		public void visitOuterClass(String owner, String name, String descriptor) {
			// anonymous classes do not have the environment annotations,
			// so we split them based on if their outer class is split
			if (notClient.contains(owner)) {
				client = null;
			}
			if (notServer.contains(owner)) {
				server = null;
			}

			if (client != null) client.visitOuterClass(owner, name, descriptor);
			if (server != null) server.visitOuterClass(owner, name, descriptor);
		}

		@Override
		public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
			if (applyAnnotation(descriptor)) {
				AnnotationVisitor c = (client == null) ? null : client.visitAnnotation(descriptor, visible);
				AnnotationVisitor s = (server == null) ? null : server.visitAnnotation(descriptor, visible);

				return new ForwardingAnnotationVisitor(api, c, s);
			} else {
				return new SplitterAnnotationVisitor(api, this::splitOnSide, this::splitOnInterfaces);
			}
		}

		@Override
		public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
			AnnotationVisitor c = (client == null) ? null : client.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
			AnnotationVisitor s = (server == null) ? null : server.visitTypeAnnotation(typeRef, typePath, descriptor, visible);

			return new ForwardingAnnotationVisitor(api, c, s);
		}

		@Override
		public void visitAttribute(Attribute attribute) {
			if (client != null) client.visitAttribute(attribute);
			if (server != null) server.visitAttribute(attribute);
		}

		@Override
		public void visitNestMember(String nestMember) {
			if (client != null) client.visitNestMember(nestMember);
			if (server != null) server.visitNestMember(nestMember);
		}

		@Override
		public void visitPermittedSubclass(String permittedSubclass) {
			if (client != null) client.visitPermittedSubclass(permittedSubclass);
			if (server != null) server.visitPermittedSubclass(permittedSubclass);
		}

		@Override
		public void visitInnerClass(String name, String outerName, String innerName, int access) {
			if (client != null) client.visitInnerClass(name, outerName, innerName, access);
			if (server != null) server.visitInnerClass(name, outerName, innerName, access);

			// keep track of which classes are client only or server only, and have inner classes
			if (client == null) {
				notClient.add(currentClass);
			}
			if (server == null) {
				notServer.add(currentClass);
			}
		}

		@Override
		public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
			RecordComponentVisitor c = (client == null) ? null : client.visitRecordComponent(name, descriptor, signature);
			RecordComponentVisitor s = (server == null) ? null : server.visitRecordComponent(name, descriptor, signature);

			return new RecordComponentVisitor(api) {

				@Override
				public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
					if (applyAnnotation(descriptor)) {
						AnnotationVisitor cc = (c == null) ? null : c.visitAnnotation(descriptor, visible);
						AnnotationVisitor ss = (s == null) ? null : s.visitAnnotation(descriptor, visible);

						return new ForwardingAnnotationVisitor(api, cc, ss);
					} else {
						return new SplitterAnnotationVisitor(api, this::splitOnSide, interfaces -> { });
					}
				}

				@Override
				public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
					AnnotationVisitor cc = (c == null) ? null : c.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
					AnnotationVisitor ss = (s == null) ? null : s.visitTypeAnnotation(typeRef, typePath, descriptor, visible);

					return new ForwardingAnnotationVisitor(api, cc, ss);
				}

				@Override
				public void visitAttribute(Attribute attribute) {
					if (c != null) c.visitAttribute(attribute);
					if (s != null) s.visitAttribute(attribute);
				}

				@Override
				public void visitEnd() {
					if (c != null) c.visitEnd();
					if (s != null) s.visitEnd();
				}

				private void splitOnSide(String side) {
					if (!side.equals(CLIENT_SIDE)) {
						((ClassNode) client).recordComponents.remove((RecordComponentNode) c);
					}
					if (!side.equals(SERVER_SIDE)) {
						((ClassNode) server).recordComponents.remove((RecordComponentNode) s);
					}
				}
			};
		}

		@Override
		public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
			FieldVisitor c = (client == null) ? null : client.visitField(access, name, descriptor, signature, value);
			FieldVisitor s = (server == null) ? null : server.visitField(access, name, descriptor, signature, value);

			return new FieldVisitor(api) {

				@Override
				public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
					if (applyAnnotation(descriptor)) {
						AnnotationVisitor cc = (c == null) ? null : c.visitAnnotation(descriptor, visible);
						AnnotationVisitor ss = (s == null) ? null : s.visitAnnotation(descriptor, visible);

						return new ForwardingAnnotationVisitor(api, cc, ss);
					} else {
						return new SplitterAnnotationVisitor(api, this::splitOnSide, interfaces -> { });
					}
				}

				@Override
				public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
					AnnotationVisitor cc = (c == null) ? null : c.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
					AnnotationVisitor ss = (s == null) ? null : s.visitTypeAnnotation(typeRef, typePath, descriptor, visible);

					return new ForwardingAnnotationVisitor(api, cc, ss);
				}

				@Override
				public void visitAttribute(Attribute attribute) {
					if (c != null) c.visitAttribute(attribute);
					if (s != null) s.visitAttribute(attribute);
				}

				@Override
				public void visitEnd() {
					if (c != null) c.visitEnd();
					if (s != null) s.visitEnd();
				}

				private void splitOnSide(String side) {
					if (!side.equals(CLIENT_SIDE)) {
						((ClassNode) client).fields.remove((FieldNode) c);
					}
					if (!side.equals(SERVER_SIDE)) {
						((ClassNode) server).fields.remove((FieldNode) s);
					}
				}
			};
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
			MethodVisitor c = (client == null) ? null : client.visitMethod(access, name, descriptor, signature, exceptions);
			MethodVisitor s = (server == null) ? null : server.visitMethod(access, name, descriptor, signature, exceptions);

			return new MethodVisitor(api) {

				@Override
				public void visitParameter(String name, int access) {
					if (c != null) c.visitParameter(name, access);
					if (s != null) s.visitParameter(name, access);
				}

				@Override
				public AnnotationVisitor visitAnnotationDefault() {
					AnnotationVisitor cc = (c == null) ? null : c.visitAnnotationDefault();
					AnnotationVisitor ss = (s == null) ? null : s.visitAnnotationDefault();

					return new ForwardingAnnotationVisitor(api, cc, ss);
				}

				@Override
				public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
					if (applyAnnotation(descriptor)) {
						AnnotationVisitor cc = (c == null) ? null : c.visitAnnotation(descriptor, visible);
						AnnotationVisitor ss = (s == null) ? null : s.visitAnnotation(descriptor, visible);

						return new ForwardingAnnotationVisitor(api, cc, ss);
					} else {
						return new SplitterAnnotationVisitor(api, this::splitOnSide, interfaces -> { });
					}
				}

				@Override
				public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
					AnnotationVisitor cc = (c == null) ? null : c.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
					AnnotationVisitor ss = (s == null) ? null : s.visitTypeAnnotation(typeRef, typePath, descriptor, visible);

					return new ForwardingAnnotationVisitor(api, cc, ss);
				}

				@Override
				public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
					if (c != null) c.visitAnnotableParameterCount(parameterCount, visible);
					if (s != null) s.visitAnnotableParameterCount(parameterCount, visible);
				}

				@Override
				public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
					AnnotationVisitor cc = (c == null) ? null : c.visitParameterAnnotation(parameter, descriptor, visible);
					AnnotationVisitor ss = (s == null) ? null : s.visitParameterAnnotation(parameter, descriptor, visible);

					return new ForwardingAnnotationVisitor(api, cc, ss);
				}

				@Override
				public void visitAttribute(Attribute attribute) {
					if (c != null) c.visitAttribute(attribute);
					if (s != null) s.visitAttribute(attribute);
				}

				@Override
				public void visitCode() {
					if (c != null) c.visitCode();
					if (s != null) s.visitCode();
				}

				@Override
				public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
					if (c != null) c.visitFrame(type, numLocal, local, numStack, stack);
					if (s != null) s.visitFrame(type, numLocal, local, numStack, stack);
				}

				@Override
				public void visitInsn(int opcode) {
					if (c != null) c.visitInsn(opcode);
					if (s != null) s.visitInsn(opcode);
				}

				@Override
				public void visitIntInsn(int opcode, int operand) {
					if (c != null) c.visitIntInsn(opcode, operand);
					if (s != null) s.visitIntInsn(opcode, operand);
				}

				@Override
				public void visitVarInsn(int opcode, int varIndex) {
					if (c != null) c.visitVarInsn(opcode, varIndex);
					if (s != null) s.visitVarInsn(opcode, varIndex);
				}

				@Override
				public void visitTypeInsn(int opcode, String type) {
					if (c != null) c.visitTypeInsn(opcode, type);
					if (s != null) s.visitTypeInsn(opcode, type);
				}

				@Override
				public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
					if (c != null) c.visitFieldInsn(opcode, owner, name, descriptor);
					if (s != null) s.visitFieldInsn(opcode, owner, name, descriptor);
				}

				@Override
				public void visitMethodInsn(int opcode, String owner, String name, String descriptor) {
					if (c != null) c.visitMethodInsn(opcode, owner, name, descriptor);
					if (s != null) s.visitMethodInsn(opcode, owner, name, descriptor);
				}

				@Override
				public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
					if (c != null) c.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
					if (s != null) s.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
				}

				@Override
				public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
					if (c != null) c.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
					if (s != null) s.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
				}

				@Override
				public void visitJumpInsn(int opcode, Label label) {
					if (c != null) c.visitJumpInsn(opcode, label);
					if (s != null) s.visitJumpInsn(opcode, label);
				}

				@Override
				public void visitLabel(Label label) {
					if (c != null) c.visitLabel(label);
					if (s != null) s.visitLabel(label);
				}

				@Override
				public void visitLdcInsn(Object value) {
					if (c != null) c.visitLdcInsn(value);
					if (s != null) s.visitLdcInsn(value);
				}

				@Override
				public void visitIincInsn(int varIndex, int increment) {
					if (c != null) c.visitIincInsn(varIndex, increment);
					if (s != null) s.visitIincInsn(varIndex, increment);
				}

				@Override
				public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
					if (c != null) c.visitTableSwitchInsn(min, max, dflt, labels);
					if (s != null) s.visitTableSwitchInsn(min, max, dflt, labels);
				}

				@Override
				public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
					if (c != null) c.visitLookupSwitchInsn(dflt, keys, labels);
					if (s != null) s.visitLookupSwitchInsn(dflt, keys, labels);
				}

				@Override
				public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
					if (c != null) c.visitMultiANewArrayInsn(descriptor, numDimensions);
					if (s != null) s.visitMultiANewArrayInsn(descriptor, numDimensions);
				}

				@Override
				public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
					AnnotationVisitor cc = (c == null) ? null : c.visitInsnAnnotation(typeRef, typePath, descriptor, visible);
					AnnotationVisitor ss = (s == null) ? null : s.visitInsnAnnotation(typeRef, typePath, descriptor, visible);

					return new ForwardingAnnotationVisitor(api, cc, ss);
				}

				@Override
				public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
					if (c != null) c.visitTryCatchBlock(start, end, handler, type);
					if (s != null) s.visitTryCatchBlock(start, end, handler, type);
				}

				@Override
				public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
					AnnotationVisitor cc = (c == null) ? null : c.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible);
					AnnotationVisitor ss = (s == null) ? null : s.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible);

					return new ForwardingAnnotationVisitor(api, cc, ss);
				}

				@Override
				public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
					if (c != null) c.visitLocalVariable(name, descriptor, signature, start, end, index);
					if (s != null) s.visitLocalVariable(name, descriptor, signature, start, end, index);
				}

				@Override
				public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
					AnnotationVisitor cc = (c == null) ? null : c.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible);
					AnnotationVisitor ss = (s == null) ? null : s.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible);

					return new ForwardingAnnotationVisitor(api, cc, ss);
				}

				@Override
				public void visitLineNumber(int line, Label start) {
					if (c != null) c.visitLineNumber(line, start);
					if (s != null) s.visitLineNumber(line, start);
				}

				@Override
				public void visitMaxs(int maxStack, int maxLocals) {
					if (c != null) c.visitMaxs(maxStack, maxLocals);
					if (s != null) s.visitMaxs(maxStack, maxLocals);
				}

				@Override
				public void visitEnd() {
					if (c != null) c.visitEnd();
					if (s != null) s.visitEnd();
				}

				private void splitOnSide(String side) {
					if (!side.equals(CLIENT_SIDE) && c != null) {
						((ClassNode) client).methods.remove((MethodNode) c);
					}
					if (!side.equals(SERVER_SIDE) && s != null) {
						((ClassNode) server).methods.remove((MethodNode) s);
					}
				}
			};
		}

		@Override
		public void visitEnd() {
			if (client != null) client.visitEnd();
			if (server != null) server.visitEnd();
		}

		private boolean applyAnnotation(String descriptor) {
			switch (descriptor) {
				case ENVIRONMENT_ANNOTATION:
				case ENVIRONMENT_INTERFACES_ANNOTATION:
					return false;
				default:
					return true;
			}
		}

		private void splitOnSide(String side) {
			if (!side.equals(CLIENT_SIDE)) {
				client = null;
			}
			if (!side.equals(SERVER_SIDE)) {
				server = null;
			}
		}

		private void splitOnInterfaces(Map<String, List<String>> interfaces) {
			for (Map.Entry<String, List<String>> e : interfaces.entrySet()) {
				String side = e.getKey();
				List<String> sidedInterfaces = e.getValue();

				if (!side.equals(CLIENT_SIDE) && client != null) {
					((ClassNode) client).interfaces.removeAll(sidedInterfaces);
				}
				if (!side.equals(SERVER_SIDE) && server != null) {
					((ClassNode) server).interfaces.removeAll(sidedInterfaces);
				}
			}
		}

		private class ForwardingAnnotationVisitor extends AnnotationVisitor {

			private AnnotationVisitor client;
			private AnnotationVisitor server;

			public ForwardingAnnotationVisitor(int api, AnnotationVisitor client, AnnotationVisitor server) {
				super(api);

				this.client = client;
				this.server = server;
			}

			@Override
			public void visit(String name, Object value) {
				if (client != null) client.visit(name, value);
				if (server != null) server.visit(name, value);
			}

			@Override
			public void visitEnum(String name, String descriptor, String value) {
				if (client != null) client.visitEnum(name, descriptor, value);
				if (server != null) server.visitEnum(name, descriptor, value);
			}

			@Override
			public AnnotationVisitor visitAnnotation(String name, String descriptor) {
				AnnotationVisitor c = (client == null) ? null : client.visitAnnotation(name, descriptor);
				AnnotationVisitor s = (server == null) ? null : server.visitAnnotation(name, descriptor);

				return new ForwardingAnnotationVisitor(api, c, s);
			}

			@Override
			public AnnotationVisitor visitArray(String name) {
				AnnotationVisitor c = (client == null) ? null : client.visitArray(name);
				AnnotationVisitor s = (server == null) ? null : server.visitArray(name);

				return new ForwardingAnnotationVisitor(api, c, s);
			}

			@Override
			public void visitEnd() {
				if (client != null) client.visitEnd();
				if (server != null) server.visitEnd();
			}
		}

		private class SplitterAnnotationVisitor extends AnnotationVisitor {

			private static final String ENVIRONMENT_TYPE_ANNOTATION = "Lnet/fabricmc/api/EnvType;";
			private static final String ENVIRONMENT_INTERFACE_ANNOTATION = "Lnet/fabricmc/api/EnvironmentInterface;";

			private final Consumer<String> sideSplitter;
			private final Consumer<Map<String, List<String>>> interfacesSplitter;

			private String side;
			private Map<String, List<String>> interfaces;

			public SplitterAnnotationVisitor(int api, Consumer<String> sideSplitter, Consumer<Map<String, List<String>>> interfacesSplitter) {
				super(api);

				this.sideSplitter = sideSplitter;
				this.interfacesSplitter = interfacesSplitter;
			}

			@Override
			public void visitEnum(String name, String descriptor, String value) {
				if (name.equals("value") && descriptor.equals(ENVIRONMENT_TYPE_ANNOTATION)) {
					side = value;
				}
			}

			@Override
			public AnnotationVisitor visitArray(String name) {
				if ("value".equals(name)) {
					interfaces = new HashMap<>();

					return new AnnotationVisitor(api) {

						@Override
						public AnnotationVisitor visitAnnotation(String name, String descriptor) {
							if (name == null && descriptor.equals(ENVIRONMENT_INTERFACE_ANNOTATION)) {
								return new AnnotationVisitor(api) {

									private String side;
									private String type;

									@Override
									public void visit(String name, Object value) {
										if (name.equals("itf")) {
											type = ((Type) value).getInternalName();
										}
									}

									@Override
									public void visitEnum(String name, String descriptor, String value) {
										if (name.equals("value") && descriptor.equals(ENVIRONMENT_TYPE_ANNOTATION)) {
											side = value;
										}
									}

									@Override
									public void visitEnd() {
										if (type != null && side != null) {
											interfaces.computeIfAbsent(side, key -> new ArrayList<>()).add(type);
										}

										side = null;
										type = null;
									}
								};
							}

							return null;
						}
					};
				}

				return null;
			}

			@Override
			public void visitEnd() {
				if (side != null) {
					sideSplitter.accept(side);
				} else if (interfaces != null) {
					interfacesSplitter.accept(interfaces);
				}

				side = null;
				interfaces = null;
			}
		}
	}
}
