package net.ornithemc.keratin.api;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.ornithemc.keratin.KeratinGradleExtension;

public interface MinecraftVersionPredicate {

	boolean test(MinecraftVersion minecraftVersion);

	record SingleVersion(MinecraftVersion minecraftVersion) implements MinecraftVersionPredicate {

		@Override
		public boolean test(MinecraftVersion minecraftVersion) {
			return minecraftVersion.compareTo(this.minecraftVersion) == 0;
		}

		@Override
		public String toString() {
			return this.minecraftVersion.id();
		}
	}

	record VersionRange(MinecraftVersion lowerBound, MinecraftVersion upperBound) implements MinecraftVersionPredicate {

		@Override
		public boolean test(MinecraftVersion minecraftVersion) {
			// if neither of two versions have shared versioning
			// a common side must exist for comparison to be possible
			if (this.lowerBound != null && !(minecraftVersion.hasSharedVersioning() || this.lowerBound.hasSharedVersioning() || minecraftVersion.hasCommonSide(this.lowerBound))) {
				return false;
			}
			if (this.upperBound != null && !(minecraftVersion.hasSharedVersioning() || this.upperBound.hasSharedVersioning() || minecraftVersion.hasCommonSide(this.upperBound))) {
				return false;
			}

			// now do the actual semantic version comparisons
			if (this.lowerBound != null && minecraftVersion.compareTo(this.lowerBound) < 0) {
				return false;
			}
			if (this.upperBound != null && minecraftVersion.compareTo(this.upperBound) > 0) {
				return false; 
			}

			return true;
		}

		@Override
		public String toString() {
			return (this.lowerBound == null ? "" : this.lowerBound.id()) + " .. " + (this.upperBound == null ? "" : this.upperBound.id());
		}
	}

	interface Operation extends MinecraftVersionPredicate {

		MinecraftVersionPredicate withOperand(MinecraftVersionPredicate operand);

		record Not(MinecraftVersionPredicate operand) implements Operation {

			@Override
			public boolean test(MinecraftVersion minecraftVersion) {
				return !this.operand.test(minecraftVersion);
			}

			@Override
			public MinecraftVersionPredicate withOperand(MinecraftVersionPredicate operand) {
				if (operand instanceof Not not) {
					return not.operand; // simplify 'not not'
				} else {
					return new Not(operand);
				}
			}

			@Override
			public String toString() {
				return "!" + this.operand.toString();
			}
		}

		record Or(MinecraftVersionPredicate... operands) implements Operation {

			@Override
			public boolean test(MinecraftVersion minecraftVersion) {
				for (MinecraftVersionPredicate predicate : this.operands) {
					if (predicate.test(minecraftVersion)) {
						return true;
					}
				}

				return false;
			}

			@Override
			public MinecraftVersionPredicate withOperand(MinecraftVersionPredicate operand) {
				MinecraftVersionPredicate[] operands = new MinecraftVersionPredicate[this.operands.length + 1];
				System.arraycopy(this.operands, 0, operands, 0, this.operands.length);
				operands[this.operands.length] = operand;
				return new Or(operands);
			}

			@Override
			public String toString() {
				return "(" + Stream.of(this.operands).map(Object::toString).collect(Collectors.joining(" || ")) + ")";
			}
		}

		record And(MinecraftVersionPredicate... operands) implements Operation {

			@Override
			public boolean test(MinecraftVersion minecraftVersion) {
				for (MinecraftVersionPredicate predicate : this.operands) {
					if (!predicate.test(minecraftVersion)) {
						return false;
					}
				}

				return true;
			}

			@Override
			public MinecraftVersionPredicate withOperand(MinecraftVersionPredicate operand) {
				MinecraftVersionPredicate[] operands = new MinecraftVersionPredicate[this.operands.length + 1];
				System.arraycopy(this.operands, 0, operands, 0, this.operands.length);
				operands[this.operands.length] = operand;
				return new And(operands);
			}

			@Override
			public String toString() {
				return "(" + Stream.of(this.operands).map(Object::toString).collect(Collectors.joining(" && ")) + ")";
			}
		}
	}

	final class Parser {

		public static MinecraftVersionPredicate parse(KeratinGradleExtension keratin, String s) {
			return new Parser(keratin, s).parse();
		}

		private static final MinecraftVersionPredicate UNINITIALIZED_EXPRESSION = new SingleVersion(null);
		private static final char NOT   = '!';
		private static final char OR    = '|';
		private static final char AND   = '&';
		private static final char RANGE = '.';

		private final KeratinGradleExtension keratin;
		private final String input;
		private final Deque<MinecraftVersionPredicate> expressionStack;

		private int index;
		private int localStack;
		private StringBuilder minecraftVersion;

		private Parser(KeratinGradleExtension keratin, String input) {
			this.keratin = keratin;
			this.input = input;
			this.expressionStack = new ArrayDeque<>();
		}

		private MinecraftVersionPredicate parse() {
			int operator = 0;
			int poperator = 0;

			while (this.index < this.input.length()) {
				char chr = this.input.charAt(this.index++);

				if (this.parseOperator(chr)) {
					poperator = operator;
					operator = chr;

					MinecraftVersion minecraftVersion = this.parseMinecraftVersion();

					if (operator == poperator) {
						this.chainExpression(operator, minecraftVersion);
					} else {
						this.switchExpression(operator, poperator, minecraftVersion);
					}

					switch (operator) {
					case NOT   -> this.handleNotExpression();
					case OR    -> this.handleOrExpression();
					case AND   -> this.handleAndExpression();
					case RANGE -> this.handleRangeExpression();
					default -> throw MinecraftVersionPredicateParseException.invalidSyntax(this.input, this.index);
					}
				} else {
					this.buildMinecraftVersion(chr);
				}
			}

			// handle string end
			this.switchExpression(0, operator, this.parseMinecraftVersion());

			return this.expressionStack.pop();
		}

		private boolean parseOperator(char chr) {
			// one-char operators, chaining allowed
			if (chr == NOT) {
				return true;
			}
			// two-char operators, no chaining allowed
			if (chr == OR || chr == AND || chr == RANGE) {
				if (this.index < this.input.length() && this.input.charAt(this.index) == chr) {
					this.index++;

					if (this.index < this.input.length() && this.input.charAt(this.index) == chr) {
						throw this.invalidSyntax("invalid token '" + chr + chr + chr + "'"); // triple-char not allowed
					} else {
						return true;
					}
				}
			}

			return false;
		}

		private void buildMinecraftVersion(char chr) {
			if (this.minecraftVersion == null && !Character.isWhitespace(chr)) { // ignore leading whitespace
				this.minecraftVersion = new StringBuilder();
			}
			if (this.minecraftVersion != null) {
				this.minecraftVersion.append(chr);
			}
		}

		private MinecraftVersion parseMinecraftVersion() {
			StringBuilder id = this.minecraftVersion;
			this.minecraftVersion = null;

			if (id != null) {
				try {
					return this.keratin.getMinecraftVersion(id.toString().trim());
				} catch (Throwable t) {
					throw this.invalidSyntax("Minecraft version " + id + " does not exist!");
				}
			} else {
				return null;
			}
		}

		private void chainExpression(int operator, MinecraftVersion minecraftVersion) {
			switch (operator) {
			case NOT -> {
				if (minecraftVersion != null) {
					throw this.invalidSyntax("NOT operator cannot precede operand!");
				}
			}
			case OR, AND -> {
				if (minecraftVersion == null) {
					throw this.invalidSyntax((operator == OR ? "OR" : "AND") + " operator cannot precede first operand!");
				}
			}
			case RANGE -> {
				throw this.invalidSyntax("RANGE expression cannot be chained!");
			}
			}

			if (this.offerMinecraftVersion(minecraftVersion)) {
				this.popExpression();
			}
		}

		private void switchExpression(int operator, int poperator, MinecraftVersion minecraftVersion) {
			// validate previous operation
			switch (poperator) {
			case RANGE -> {
				if (minecraftVersion == null) {
					MinecraftVersionPredicate expression = this.expressionStack.peek();

					if (expression instanceof VersionRange range && range.lowerBound == null) {
						throw this.invalidSyntax("RANGE expression must have a lower bound or an upper bound!");
					}
				}
			}
			}

			// validate new operation
			switch (operator) {
			case NOT -> {
				if (poperator == RANGE || minecraftVersion != null) {
					throw this.invalidSyntax("NOT operator is not a valid token inside RANGE expressions!");
				}
			}
			case OR, AND -> {
				if (poperator != RANGE && minecraftVersion == null) {
					throw this.invalidSyntax((operator == OR ? "OR" : "AND") + " operator cannot precede first operand!");
				}
			}
			}

			// handle precedence
			if (operator == RANGE || (operator == NOT && poperator != RANGE)) {
				if (!this.offerMinecraftVersion(minecraftVersion)) {
					this.pushExpression();
				}
			} else {
				this.offerMinecraftVersion(minecraftVersion);

				while (this.localStack > 1) {
					this.popExpression();
				}
			}
		}

		private void popExpression() {
			MinecraftVersionPredicate expression = this.expressionStack.pop();
			MinecraftVersionPredicate enclosing = this.expressionStack.pop();

			if (enclosing == UNINITIALIZED_EXPRESSION) {
				enclosing = expression;
			} else if (enclosing instanceof Operation operation) {
				enclosing = operation.withOperand(expression);
			} else if (enclosing instanceof VersionRange range && expression instanceof SingleVersion version) {
				enclosing = new VersionRange(range.lowerBound, version.minecraftVersion);
			} else {
				throw this.invalidSyntax();
			}

			this.expressionStack.push(enclosing);
			this.localStack--;
		}

		private void pushExpression() {
			this.expressionStack.push(UNINITIALIZED_EXPRESSION);
			this.localStack++;
		}

		private boolean offerMinecraftVersion(MinecraftVersion minecraftVersion) {
			if (minecraftVersion != null) {
				this.expressionStack.push(new SingleVersion(minecraftVersion));
				this.localStack++;

				return true;
			} else {
				return false;
			}
		}

		private void handleNotExpression() {
			MinecraftVersionPredicate expression = this.expressionStack.pop();
			MinecraftVersionPredicate not = new Operation.Not(null);

			if (expression != UNINITIALIZED_EXPRESSION) {
				this.expressionStack.push(expression);
			}

			this.expressionStack.push(not);
		}

		private void handleOrExpression() {
			MinecraftVersionPredicate expression = this.expressionStack.pop();
			MinecraftVersionPredicate or;

			if (expression instanceof Operation.Or) {
				or = expression;
			} else {
				or = new Operation.Or(expression);
			}

			this.expressionStack.push(or);
		}

		private void handleAndExpression() {
			MinecraftVersionPredicate expression = this.expressionStack.pop();
			MinecraftVersionPredicate and;

			if (expression instanceof Operation.And) {
				and = expression;
			} else {
				and = new Operation.And(expression);
			}

			this.expressionStack.push(and);
		}

		private void handleRangeExpression() {
			MinecraftVersionPredicate expression = this.expressionStack.pop();
			MinecraftVersionPredicate range;

			if (expression == UNINITIALIZED_EXPRESSION) {
				// version range without lower bound
				range = new VersionRange(null, null);
			} else if (expression instanceof SingleVersion version) {
				// version range with lower bound
				range = new VersionRange(version.minecraftVersion, null);
			} else {
				throw this.invalidSyntax(expression + " is not valid in a RANGE expression");
			}

			this.expressionStack.push(range);
		}

		private MinecraftVersionPredicateParseException invalidSyntax() {
			return MinecraftVersionPredicateParseException.invalidSyntax(this.input, this.index);
		}
	
		private MinecraftVersionPredicateParseException invalidSyntax(String message) {
			return MinecraftVersionPredicateParseException.invalidSyntax(this.input, this.index, new MinecraftVersionPredicateParseException(message));
		}
	}

	@SuppressWarnings("serial")
	class MinecraftVersionPredicateParseException extends RuntimeException {
		
		public MinecraftVersionPredicateParseException(String message) {
			super(message);
		}
	
		public MinecraftVersionPredicateParseException(String message, Throwable t) {
			super(message, t);
		}
	
		public static MinecraftVersionPredicateParseException invalidSyntax(String input, int index) {
			return new MinecraftVersionPredicateParseException("Invalid syntax at index " + --index + " (" + input.substring(0, ++index) + " <- HERE)");
		}
	
		public static MinecraftVersionPredicateParseException invalidSyntax(String input, int index, Throwable t) {
			return new MinecraftVersionPredicateParseException("Invalid syntax at index " + --index + " (" + input.substring(0, ++index) + " <- HERE)", t);
		}
	}
}
