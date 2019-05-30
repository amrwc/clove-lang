package values;

import java.util.ArrayList;

import interpreter.ExceptionSemantic;
import interpreter.Parser;
import parser.ast.SimpleNode;

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
		throw new ExceptionSemantic("Value type " + getName() + " doesn't support"
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
			throw new ExceptionSemantic("There is no prototype function \"" + protoFunc
					+ "\" in " + getName() + " class.");
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

		try {
			return new ValueReflection(v);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		throw new ExceptionSemantic("Coudln't get a corresponding value from " + v + ".");
	}

	@Override
	public Value or(Value v) {
		throw new ExceptionSemantic(
				"Cannot perform OR on " + getName() + " and " + v.getName());
	}

	@Override
	public Value and(Value v) {
		throw new ExceptionSemantic(
				"Cannot perform AND on " + getName() + " and " + v.getName());
	}

	@Override
	public Value not() {
		throw new ExceptionSemantic("Cannot perform NOT on " + getName());
	}

	@Override
	public Value add(Value v) {
		throw new ExceptionSemantic(
				"Cannot perform + on " + getName() + " and " + v.getName());
	}

	@Override
	public Value subtract(Value v) {
		throw new ExceptionSemantic(
				"Cannot perform - on " + getName() + " and " + v.getName());
	}

	@Override
	public Value mult(Value v) {
		throw new ExceptionSemantic(
				"Cannot perform * on " + getName() + " and " + v.getName());
	}

	@Override
	public Value div(Value v) {
		throw new ExceptionSemantic(
				"Cannot perform / on " + getName() + " and " + v.getName());
	}

	@Override
	public Value mod(Value v) {
		throw new ExceptionSemantic(
				"Cannot perform % on " + getName() + " and " + v.getName());
	}

	@Override
	public Value unary_plus() {
		throw new ExceptionSemantic("Cannot perform + on " + getName());
	}

	@Override
	public Value unary_minus() {
		throw new ExceptionSemantic("Cannot perform - on " + getName());
	}

	/** Convert this to a primitive string. */
	@Override
	public String stringValue() {
		throw new ExceptionSemantic("Cannot convert " + getName() + " to string.");
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
		return new ValueBoolean(compare(v) >= 0);
	}

	/** Test this value and another for <= */
	@Override
	public Value lte(Value v) {
		return new ValueBoolean(compare(v) <= 0);
	}

	/** Test this value and another for > */
	@Override
	public Value gt(Value v) {
		return new ValueBoolean(compare(v) > 0);
	}

	/** Test this value and another for < */
	@Override
	public Value lt(Value v) {
		return new ValueBoolean(compare(v) < 0);
	}
}
