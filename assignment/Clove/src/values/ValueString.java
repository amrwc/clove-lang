package values;

import java.util.ArrayList;

import interpreter.ExceptionSemantic;
import interpreter.Parser;
import parser.ast.SimpleNode;

public class ValueString extends ValueAbstract {
	
	private final String internalValue;
	
	/** Return a ValueString given a quote-delimited source string. */
	public static ValueString stripDelimited(String b) {
		return new ValueString(b.substring(1, b.length() - 1));
	}
	
	public ValueString(String b) {
		internalValue = b;
	}

	/**
	 * Dereferences a value in a nested expression.
	 * 
	 * @param node -- node in question
	 * @param v -- value to be dereferenced
	 * @param currChild -- current child of the node being parsed
	 * @returns {Value} the dereferenced value
	 */
	@Override
	public Value dereference(SimpleNode node, Value v, int currChild) {
		final Parser par = new Parser();
		final ValueString valueString = (ValueString) v;
		final int index = (int) ((ValueInteger) par.doChild(node, currChild)).longValue();
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
				throw new ExceptionSemantic("There is no prototype function \"" + protoFunc + "\" in ValueString class.");
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
