package values;

import java.util.ArrayList;
import java.util.stream.Collectors;

import interpreter.ExceptionSemantic;
import interpreter.Parser;
import parser.ast.SimpleNode;

/**
 * @see https://docs.oracle.com/javase/8/docs/api/index.html?java/util/ArrayList.html
 * @author amrwc
 */
public class ValueList extends ValueAbstract {

	private ArrayList<Value> internalValue = new ArrayList<Value>();
	
	public ValueList() {}

	public ValueList(ArrayList<Value> valueList) {
		internalValue = valueList;
	}

	public String getName() {
		return "ValueList";
	}

	public int compare(Value v) {
		ArrayList<Value> arr = ((ValueList) v).internalValue;
		return internalValue.equals(arr) ? 0 : 1;
	}

	/**
	 * Dereferences a value in a nested expression.
	 * 
	 * @param node -- node in question
	 * @param v -- value to be dereferenced
	 * @param currChild -- current child of the node being parsed
	 * @returns {Value} the dereferenced value
	 */
	public Value dereference(SimpleNode node, Value v, int currChild) {
		final Parser par = new Parser();
		final ValueList valueList = (ValueList) v;
		final int index = (int) ((ValueInteger) par.doChild(node, currChild)).longValue();
		return valueList.get(index);
	}

	/**
	 * Execute a prototype function.
	 * 
	 * @param {String} protoFunc -- prototype function name
	 * @param {ArrayList<Value>} protoArgs -- arguments for the function
	 * @returns {Value} result of the prototype function
	 * @author amrwc
	 */
	public Value execProto(String protoFunc, ArrayList<Value> protoArgs) {
		switch (protoFunc) {
			case "append":
			case "push":
				protoArgs.forEach(arg -> append(arg));
				break;
			case "copy":
				return new ValueList(new ArrayList<Value>(internalValue));
			case "getClass":
				return new ValueString(getName());
			case "indexOf":
				return findIndex(protoArgs.get(0));
			case "length":
				return length();
			case "pop":
				return internalValue.remove(internalValue.size() - 1);
			case "remove":
				return internalValue.remove((int) protoArgs.get(0).longValue());
			case "shift":
				return internalValue.remove(0);
			default:
				throw new ExceptionSemantic("There is no prototype function \""
					+ protoFunc + "\" in ValueList class.");
		}

		return null;
	}

	public void append(Value v) {
		if (v == null) throw new ExceptionSemantic("The argument for ValueList.append() cannot be null.");
		internalValue.add(v);
	}

	/**
	 * Find the index of a Value in the ValueList.
	 * Returns -1 if it's not found.
	 * 
	 * @param {Value} v -- Value to be found
	 * @return {ValueInteger} index of the Value in the ValueList
	 */
	private Value findIndex(Value v) {
		String strVal = v.stringValue();
		for (int i = 0; i < internalValue.size(); i++) {
			if (internalValue.get(i).stringValue().equals(strVal))
				return new ValueInteger(i);
		}
		return new ValueInteger(-1);
	}

	public Value get(int i) {
		if (internalValue.size() <= i)
			throw new ExceptionSemantic("The index " + i + " is out of bounds of the list with length "
			+ internalValue.size() + ".");

		Value val = internalValue.get(i);
		if (val != null) return val;
		throw new ExceptionSemantic("Value of index " + i + " in the list is undefined or equal to null.");
	}

	public void set(int i, Value v) {
		if (i < 0) throw new ExceptionSemantic("The index in ValueList cannot be negative.");
		if (v == null) throw new ExceptionSemantic("The Value passed into ValueList.set() cannot be null.");
		internalValue.set(i, v);
	}

	// To be used in Clove-lang because it resolves to a Value.
	private Value length() {
		return new ValueInteger((long) internalValue.size());
	}

	// To be used internally, by the Parser implementation.
	public int size() {
		return internalValue.size();
	}

	/**
	 * @read https://stackoverflow.com/a/23183963/10620237
	 */
	public String toString() {
		String strVal = internalValue
							.stream()
							.map(Object::toString)
							.collect(Collectors.joining(", "));
		return "[" + strVal + "]";
	}

	public String stringValue() {
		return toString();
	}
}
