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
	 * @param {Value} v -- value to be dereferenced
	 * @param {int} currChild -- current child of the node being parsed
	 * @param {Parser} p -- the instance of Parser currently running
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
	 * @param {String} protoFunc -- prototype function name
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
				throw new ExceptionSemantic("There is no prototype function \""
					+ protoFunc + "\" in " + getName() + " class.");
		}
	}

	@Override
	public Value or(Value v) {
		throw new ExceptionSemantic("Cannot perform OR on " + getName() + " and " + v.getName());
	}

	@Override
	public Value and(Value v) {
		throw new ExceptionSemantic("Cannot perform AND on " + getName() + " and " + v.getName());
	}

	@Override
	public Value not() {
		throw new ExceptionSemantic("Cannot perform NOT on " + getName());
	}

	@Override
	public Value add(Value v) {
		throw new ExceptionSemantic("Cannot perform + on " + getName() + " and " + v.getName());
	}

	@Override
	public Value subtract(Value v) {
		throw new ExceptionSemantic("Cannot perform - on " + getName() + " and " + v.getName());
	}

	@Override
	public Value mult(Value v) {
		throw new ExceptionSemantic("Cannot perform * on " + getName() + " and " + v.getName());
	}

	@Override
	public Value div(Value v) {
		throw new ExceptionSemantic("Cannot perform / on " + getName() + " and " + v.getName());
	}

	@Override
	public Value mod(Value v) {
		throw new ExceptionSemantic("Cannot perform % on " + getName() + " and " + v.getName());
	}

	@Override
	public Value unary_plus() {
		throw new ExceptionSemantic("Cannot perform + on " + getName());
	}

	@Override
	public Value unary_minus() {
		throw new ExceptionSemantic("Cannot perform - on " + getName());
	}

	/** Convert this to a primitive double. */
	@Override
	public double doubleValue() {
		throw new ExceptionSemantic("Cannot convert " + getName() + " to rational.");
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
