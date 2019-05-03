package values;

import java.util.ArrayList;
import java.util.HashMap;

import interpreter.ExceptionSemantic;

/**
 * @see https://docs.oracle.com/javase/8/docs/api/java/util/HashMap.html
 * @author amrwc
 */
public class ValueObject extends ValueAbstract {
	
	private HashMap<String, Value> internalValue = new HashMap<String, Value>();

	public ValueObject() {}

	public String getName() {
		return "Object";
	}

	public int compare(Value v) {
		HashMap<String, Value> map = ((ValueObject) v).internalValue;
		return internalValue.equals(map) ? 0 : 1;
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
			case "remove":
				protoArgs.forEach(arg -> remove(arg.stringValue()));
				break;
			case "tryRemove":
				protoArgs.forEach(arg -> tryRemove(arg.stringValue()));
				break;
			default:
				throw new ExceptionSemantic("There is no prototype function \"" + protoFunc + "\" in ValueObject class.");
		}

		return null;
	}

	public void add(String name, Value v) {
		internalValue.putIfAbsent(name, v);
	}

	public Value get(String name) {
		Value value = internalValue.get(name);
		if (value != null) return value;
		throw new ExceptionSemantic("Object key \"" + name + "\" is undefined or equal to null.");
	}

	public void set(String name, Value v) {
		if (name == null || name == "null" || v == null)
			throw new ExceptionSemantic("Neither key nor value of an object can be null.");
		internalValue.put(name, v);
	}

	private void remove(String name) {
		if (internalValue.containsKey(name))
			internalValue.remove(name);
		else
			throw new ExceptionSemantic("This ValueObject does not contain the \"" + name + "\" key.");
	}

	private void tryRemove(String name) {
		internalValue.remove(name);
	}

	// Returns the key-value pairs in '{key: value}' notation.
	public String toString() {
		if (internalValue.size() == 0) return "{}";
		String result = "{";
		for (HashMap.Entry<String, Value> entry : internalValue.entrySet())
			result += entry.getKey() + ": " + entry.getValue() + ", ";
		return result.substring(0, result.length() - 2) + "}";
	}
}
