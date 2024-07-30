package net.ornithemc.keratin.matching;

import java.io.File;
import java.io.IOException;

import org.objectweb.asm.commons.Remapper;

import net.ornithemc.mappingutils.io.matcher.MatchSide;
import net.ornithemc.mappingutils.io.matcher.Matches;
import net.ornithemc.mappingutils.io.matcher.Matches.ClassMatch;
import net.ornithemc.mappingutils.io.matcher.Matches.FieldMatch;
import net.ornithemc.mappingutils.io.matcher.Matches.MethodMatch;
import net.ornithemc.mappingutils.io.matcher.MatchesReader;

public class MatchesUtil {

	public static Remapper makeRemapper(File file, boolean inverted) throws IOException {
		Matches matches = MatchesReader.read(file.toPath());
		MatchSide side = inverted ? MatchSide.B : MatchSide.A;

		return new Remapper() {

			private ClassMatch currentClass;
			private FieldMatch currentField;
			private MethodMatch currentMethod;

			private boolean inSignature;
			private boolean signatureValid;

			@Override
			public String map(String className) {
				currentClass = matches.getClass(className, side);
				currentField = null;
				currentMethod = null;
				if (inSignature && (currentClass == null || !currentClass.matched())) {
					signatureValid = false;
				}
				return currentClass == null ? className : (currentClass.matched() ? currentClass.name(side.opposite()) : null);
			}

			@Override
			public String mapFieldName(String className, String name, String descriptor) {
				currentField = (currentClass == null || !currentClass.matched()) ? null : currentClass.getField(name, descriptor, side);
				currentMethod = null;
				return currentField == null ? name : (currentField.matched() ? currentField.name(side.opposite()) : null);
			}

			@Override
			public String mapMethodName(String className, String name, String descriptor) {
				currentMethod = (currentClass == null || !currentClass.matched()) ? null : currentClass.getMethod(name, descriptor, side);
				currentField = null;
				return currentMethod == null ? name : (currentMethod.matched() ? currentMethod.name(side.opposite()) : null);
			}

			@Override
			public String mapDesc(String descriptor) {
				if (descriptor.charAt(0) == '(') {
					return currentMethod == null ? descriptor : (currentMethod.matched() ? currentMethod.desc(side.opposite()) : null);
				} else {
					return currentField == null ? descriptor : (currentField.matched() ? currentField.desc(side.opposite()) : null);
				}
			}

			@Override
			public String mapSignature(String signature, boolean typeSignature) {
				try {
					inSignature = true;
					signatureValid = true;

					signature = super.mapSignature(signature, typeSignature);

					if (!signatureValid) {
						signature = null;
					}

					inSignature = false;
					signatureValid = false;

					return signature;
				} catch (Throwable t) {
					return null;
				}
			}
		};
	}
}
