package values;

import java.util.ArrayList;
import java.util.Vector;
import java.util.stream.Collectors;

import interpreter.ExceptionSemantic;
import interpreter.Parser;
import parser.ast.SimpleNode;

/**
 * @see https://docs.oracle.com/javase/8/docs/api/java/util/Vector.html
 * @author amrwc
 */
public class ValueArray extends ValueAbstract {
	private Vector<Value> internalValue;
	private int capacity;
	
	public ValueArray() {
		internalValue = new Vector<Value>();
		capacity = 0;
	}

	public ValueArray(int capacity) {
		internalValue = new Vector<Value>(capacity);
		this.capacity = capacity;
	}

	public ValueArray(Vector<Value> valueArray) {
		internalValue = valueArray;
	}

	public String getName() {
		return "ValueArray";
	}

	public int compare(Value v) {
		Vector<Value> arr = ((ValueArray) v).internalValue;
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
		final ValueArray valueList = (ValueArray) v;
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
			case "capacity":
			case "cap":
				return new ValueInteger(capacity);
			case "copy":
				return new ValueArray(new Vector<Value>(internalValue));
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
			case "resize":
				resize(protoArgs.get(0));
				break;
			case "shift":
				return internalValue.remove(0);
			default:
				throw new ExceptionSemantic("There is no prototype function \""
					+ protoFunc + "\" in ValueArray class.");
		}

		return null;
	}

	public void append(Value v) {
		if (internalValue.size() + 1 > capacity)
			throw new ExceptionSemantic("The ValueArray of capacity " + capacity
				+ " is full and cannot take any more values.");
		if (v == null)
			throw new ExceptionSemantic("The argument for ValueArray.append()"
				+ " cannot be null.");

		internalValue.add(v);
	}

	/**
	 * Find the index of a Value in the ValueArray.
	 * Returns -1 if it's not found.
	 * 
	 * @param {Value} v -- Value to be found
	 * @return {ValueInteger} index of the Value in the ValueArray
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
			throw new ExceptionSemantic("The index " + i + " is out of bounds of the array with length "
			+ internalValue.size() + ".");

		Value val = internalValue.get(i);
		if (val != null) return val;
		throw new ExceptionSemantic("Value of index " + i + " in the array is undefined or equal to null.");
	}

	public void set(int i, Value v) {
		if (i < 0)
			throw new ExceptionSemantic("The index in ValueArray cannot be negative.");
		if (v == null)
			throw new ExceptionSemantic("The Value passed into ValueArray.set() cannot be null.");
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
	 * Resizes the ValueArray -- increases the internal capacity
	 * and the Vector's size.
	 * 
	 * @param {ValueInteger} len
	 */
	private void resize(Value len) {
		int newLen = (int) len.longValue();
		capacity = newLen;

		// If the internal capacity exceeds the Vector's cap, adjust the latter.
		if (capacity > internalValue.capacity()) {
			internalValue.setSize(capacity);

			// Remove all the null values assigned by the setSize() method.
			// The loop ends at the index after the last non-null value.
			int firstNull = internalValue.indexOf(null);
			for (int i = capacity - 1; i >= firstNull; i--)
				internalValue.remove(i);
		}
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
