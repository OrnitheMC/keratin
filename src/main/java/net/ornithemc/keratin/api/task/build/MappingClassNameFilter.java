package net.ornithemc.keratin.api.task.build;

import java.io.IOException;
import java.util.regex.Pattern;

import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.ForwardingMappingVisitor;

public class MappingClassNameFilter extends ForwardingMappingVisitor {

	private final String classNamePattern;

	public MappingClassNameFilter(MappingVisitor next, String classNamePattern) {
		super(next);

		this.classNamePattern = classNamePattern;
	}

	@Override
	public boolean visitClass(String name) throws IOException {
		return Pattern.matches(classNamePattern, name) && super.visitClass(name);
	}
}
