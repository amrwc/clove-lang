package values;

import java.util.ArrayList;

import interpreter.ExceptionSemantic;

public class ValueString extends ValueAbstract {
	
	private String internalValue;
	
	/** Return a ValueString given a quote-delimited source string. */
	public static ValueString stripDelimited(String b) {
		return new ValueString(b.substring(1, b.length() - 1));
	}
	
	public ValueString(String b) {
		internalValue = b;
	}

	/**
	 * Execute a prototype function.
	 * 
	 * @param protoFunc
	 * @param protoArgs
	 * @return Value
	 * @author amrwc
	 */
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
	
	public String getName() {
		return "ValueString";
	}
	
	/** Convert this to a String. */
	public String stringValue() {
		return internalValue;		
	}

	public int compare(Value v) {
		return internalValue.compareTo(v.stringValue());
	}
	
	/** Add performs string concatenation. */
	public Value add(Value v) {
		return new ValueString(internalValue + v.stringValue());
	}
	
	public String toString() {
		return internalValue;
	}
}
