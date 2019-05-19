package values;

import java.util.ArrayList;
import java.util.Vector;
import java.util.stream.Collectors;

import interpreter.ExceptionSemantic;

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
	 * Execute a prototype function.
	 * 
	 * @param protoFunc
	 * @param protoArgs
	 * @return Value
	 * @author amrwc
	 */
	public Value execProto(String protoFunc, ArrayList<Value> protoArgs) {
		switch (protoFunc) {
			case "append":
				protoArgs.forEach(arg -> append(arg));
				break;
			case "copy":
				return new ValueArray(new Vector<Value>(internalValue));
			case "getClass":
				return new ValueString(getName());
			case "indexOf":
				return findIndex(protoArgs.get(0));
			case "length":
				return length();
			case "shift":
				return internalValue.remove(0);
			default:
				throw new ExceptionSemantic("There is no prototype function \"" + protoFunc + "\" in ValuArray class.");
		}

		return null;
	}

	public void append(Value v) {
		if (internalValue.size() + 1 > capacity)
			throw new ExceptionSemantic("The ValueArray is full and cannot be further appended.");
		if (v == null)
			throw new ExceptionSemantic("The argument for ValueArray.append() cannot be null.");
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

	public String toString() {
		// https://stackoverflow.com/a/23183963/10620237
		String strVal = internalValue
							.stream()
							.map(Object::toString)
							.collect(Collectors.joining(", "));
		return "[" + strVal + "]";
	}
}
