package net.ornithemc.keratin.api.task.mapping.graph;

import net.ornithemc.mappingutils.io.MappingTarget;
import net.ornithemc.mappingutils.io.MappingValidator;
import net.ornithemc.mappingutils.io.Mappings.ClassMapping;
import net.ornithemc.mappingutils.io.Mappings.FieldMapping;
import net.ornithemc.mappingutils.io.Mappings.MethodMapping;
import net.ornithemc.mappingutils.io.Mappings.ParameterMapping;
import net.ornithemc.mappingutils.io.diff.DiffSide;
import net.ornithemc.mappingutils.io.diff.MappingsDiff;
import net.ornithemc.mappingutils.io.diff.MappingsDiff.ClassDiff;
import net.ornithemc.mappingutils.io.diff.MappingsDiff.Diff;
import net.ornithemc.mappingutils.io.diff.MappingsDiff.FieldDiff;
import net.ornithemc.mappingutils.io.diff.MappingsDiff.MethodDiff;
import net.ornithemc.mappingutils.io.diff.MappingsDiff.ParameterDiff;
import net.ornithemc.mappingutils.io.diff.MappingsDiffValidator;

public class Validators {

	public static final MappingValidator REMOVE_DUMMY_MAPPINGS = new MappingValidator() {

		@Override
		public boolean validate(ClassMapping c) {
			if (!MappingsDiff.safeIsDiff(ClassMapping.getSimplified(c.src()), c.get()) && (c.get().startsWith("C_") || c.get().startsWith("net/minecraft/unmapped/C_"))) {
				c.set("");
				return !c.getJavadoc().isEmpty() || c.hasChildren();
			}

			return true;
		}

		@Override
		public boolean validate(FieldMapping f) {
			if (!MappingsDiff.safeIsDiff(f.src(), f.get()) && f.get().startsWith("f_")) {
				f.set("");
				return !f.getJavadoc().isEmpty();
			}

			return true;
		}

		@Override
		public boolean validate(MethodMapping m) {
			if (!MappingsDiff.safeIsDiff(m.src(), m.get()) && (m.get().startsWith("m_") || m.get().equals("<init>") || m.get().equals("<clinit>"))) {
				m.set("");
				return !m.getJavadoc().isEmpty() || m.hasChildren();
			}

			return true;
		}

		@Override
		public boolean validate(ParameterMapping p) {
			if (p.get().startsWith("p_")) {
				p.set("");
				return !p.getJavadoc().isEmpty();
			}

			return true;
		}
	};
	public static final MappingsDiffValidator INSERT_DUMMY_MAPPINGS = new MappingsDiffValidator() {

		@Override
		public boolean validate(ClassDiff c) {
			return check(c);
		}

		@Override
		public boolean validate(FieldDiff f) {
			return check(f);
		}

		@Override
		public boolean validate(MethodDiff m) {
			return check(m);
		}

		@Override
		public boolean validate(ParameterDiff p) {
			return check(p);
		}

		private boolean check(Diff d) {
			if (d.isDiff()) {
				if (d.get(DiffSide.A).isEmpty()) {
					// new mappings should be ignored, as any un-mapped members
					// should already be present as dummy mappings
					System.out.println("ignoring illegal change " + d);
					return false;
				}
				if (d.get(DiffSide.B).isEmpty()) {
					// removing a mapping is changed into a dummy mapping
					if (d.target() == MappingTarget.CLASS) {
						d.set(DiffSide.B, ClassMapping.getSimplified(d.src()));
					} else if (d.target() == MappingTarget.PARAMETER) {
						d.set(DiffSide.B, "p_" + ((ParameterDiff)d).getIndex());
					} else {
						d.set(DiffSide.B, d.src());
					}
				}
			}

			return true;
		}
	};
}
