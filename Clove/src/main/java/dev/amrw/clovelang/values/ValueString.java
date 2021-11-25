package dev.amrw.clovelang.values;

import java.util.ArrayList;

import dev.amrw.clovelang.interpreter.ExceptionSemantic;
import dev.amrw.clovelang.interpreter.Parser;
import dev.amrw.clovelang.parser.ast.SimpleNode;

/**
 * @author dave
 * @author amrwc
 */
public class ValueString extends ValueAbstract {

	private final String internalValue;

	/** Return a ValueString given a quote-delimited source string. */
	public static ValueString stripDelimited(String b) {
		return new ValueString(b.substring(1, b.length() - 1));
	}

	public ValueString(String b) {
		internalValue = b;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getRawValue() {
		return internalValue;
	}

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
		final ValueString valueString = (ValueString) v;
		final int index = (int) ((ValueInteger) p.doChild(node, currChild)).getRawValue();
		final String str = "" + valueString.stringValue().charAt(index);
		return new ValueString(str);
	}

	/**
	 * Execute a prototype function.
	 * 
	 * @param protoFunc
	 * @param protoArgs
	 * @return Value
	 * @author amrwc
	 */
	@Override
	public Value execProto(String protoFunc, ArrayList<Value> protoArgs) {
		switch (protoFunc) {
		case "getClass":
			return new ValueString(getName());
		case "length":
			return length();
		default:
			throw new ExceptionSemantic("There is no prototype function \"" + protoFunc
					+ "\" in ValueString class.");
		}
	}

	private Value length() {
		return new ValueInteger(internalValue.length());
	}

	@Override
	public String getName() {
		return "ValueString";
	}

	/** Convert this to a String. */
	@Override
	public String stringValue() {
		return internalValue;
	}

	@Override
	public int compare(Value v) {
		return internalValue.compareTo(v.stringValue());
	}

	/** Add performs string concatenation. */
	@Override
	public Value add(Value v) {
		return new ValueString(internalValue + v.stringValue());
	}

	@Override
	public String toString() {
		return internalValue;
	}
}
