package dev.amrw.clovelang.values;

import java.util.ArrayList;

import dev.amrw.clovelang.interpreter.ExceptionSemantic;
import dev.amrw.clovelang.interpreter.Parser;
import dev.amrw.clovelang.parser.ast.SimpleNode;

/**
 * @author dave
 */
public abstract class ValueAbstract implements Value {

	@Override
	public abstract String getName();

	@Override
	public abstract int compare(Value v);

	@Override
	public abstract <T> T getRawValue();

	/**
	 * Dereferences a value in a nested expression.
	 * 
	 * @param {SimpleNode} node -- node in question
	 * @param {Value}      v -- value to be dereferenced
	 * @param {int}        currChild -- current child of the node being parsed
	 * @param {Parser}     p -- the instance of Parser currently running
	 * @returns {Value} the dereferenced value
	 */
	@Override
	public Value dereference(SimpleNode node, Value v, int currChild, Parser p) {
		throw new ExceptionSemantic("Value type '" + getName() + "' doesn't support"
				+ " custom dereferencing.");
	}

	/**
	 * Execute a prototype function.
	 * 
	 * @param {String}           protoFunc -- prototype function name
	 * @param {ArrayList<Value>} protoArgs -- arguments for the function
	 * @returns {Value} result of the prototype function
	 * @author amrwc
	 */
	@Override
	public Value execProto(String protoFunc, ArrayList<Value> protoArgs) {
		switch (protoFunc) {
		case "getClass":
			return new ValueString(getName());
		default:
			throw new ExceptionSemantic("There is no prototype function '" + protoFunc
					+ "' in the '" + getName() + "' class.");
		}
	}

	/**
	 * Creates correct Value-type from primitives, or gives a ValueReflection object
	 * for everything else.
	 * 
	 * @param {Object} v
	 * @returns {Value} Value-type corresponding to the resulting primitive
	 * @author amrwc
	 */
	public static Value getCorrespondingValue(Object v) {
		if (v instanceof Boolean)
			return new ValueBoolean((boolean) v);
		if (v instanceof Short)
			return new ValueInteger((short) v);
		if (v instanceof Integer)
			return new ValueInteger((int) v);
		if (v instanceof Long)
			return new ValueLong((long) v);
		if (v instanceof Float)
			return new ValueFloat((float) v);
		if (v instanceof Double)
			return new ValueDouble((double) v);
		if (v instanceof String)
			return new ValueString((String) v);

		return new ValueReflection(v.getClass(), v);
	}

	@Override
	public Value or(Value v) {
		throw ExceptionSemantic.binaryOperationError(this, "OR", v);
	}

	@Override
	public Value and(Value v) {
		throw ExceptionSemantic.binaryOperationError(this, "AND", v);
	}

	@Override
	public Value not() {
		throw ExceptionSemantic.unaryOperationError(this, "NOT");
	}

	@Override
	public Value add(Value v) {
		throw ExceptionSemantic.binaryOperationError(this, "+", v);
	}

	@Override
	public Value subtract(Value v) {
		throw ExceptionSemantic.binaryOperationError(this, "-", v);
	}

	@Override
	public Value mult(Value v) {
		throw ExceptionSemantic.binaryOperationError(this, "*", v);
	}

	@Override
	public Value div(Value v) {
		throw ExceptionSemantic.binaryOperationError(this, "/", v);
	}

	@Override
	public Value mod(Value v) {
		throw ExceptionSemantic.binaryOperationError(this, "%", v);
	}

	@Override
	public Value unary_plus() {
		throw ExceptionSemantic.unaryOperationError(this, "+");
	}

	@Override
	public Value unary_minus() {
		throw ExceptionSemantic.unaryOperationError(this, "-");
	}

	/** Convert this to a primitive string. */
	@Override
	public String stringValue() {
		throw new ExceptionSemantic("Cannot convert '" + getName() + "' to string.");
	}

	/** Convert this to a primitive double. */
	@Override
	public double doubleValue() {
		throw new ExceptionSemantic("Cannot convert '" + getName() + "' to double.");
	}

	/** Test this value and another for equality. */
	@Override
	public Value eq(Value v) {
		return new ValueBoolean(compare(v) == 0);
	}

	/** Test this value and another for non-equality. */
	@Override
	public Value neq(Value v) {
		return new ValueBoolean(compare(v) != 0);
	}

	/** Test this value and another for >= */
	@Override
	public Value gte(Value v) {
		booleanTest(v, ">=");
		return new ValueBoolean(compare(v) >= 0);
	}

	/** Test this value and another for <= */
	@Override
	public Value lte(Value v) {
		booleanTest(v, "<=");
		return new ValueBoolean(compare(v) <= 0);
	}

	/** Test this value and another for > */
	@Override
	public Value gt(Value v) {
		booleanTest(v, ">");
		return new ValueBoolean(compare(v) > 0);
	}

	/** Test this value and another for < */
	@Override
	public Value lt(Value v) {
		booleanTest(v, "<");
		return new ValueBoolean(compare(v) < 0);
	}

	/**
	 * Tests whether the given Value is a boolean, and if it is, it will throw an
	 * exception citing usage of an unsupported operator on the boolean type.
	 * 
	 * @param {Value}  v
	 * @param {String} operator
	 */
	private void booleanTest(Value v, String operator) {
		if (getName().equals("boolean"))
			throw new ExceptionSemantic("The operator '" + operator
					+ "' is undefined for the argument type(s) '" + getName() + "', '"
					+ v.getName() + " (" + v.getRawValue() + ")'.");
	}
}
