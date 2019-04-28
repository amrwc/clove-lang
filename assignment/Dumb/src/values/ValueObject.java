package values;

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

	public void remove(String name) {
		if (internalValue.containsKey(name))
			internalValue.remove(name);
		else
			throw new ExceptionSemantic("This ValueObject does not contain the \"" + name + "\" key.");
	}

	public void tryRemove(String name) {
		internalValue.remove(name);
	}

	public String toString() {
		return internalValue.toString();
	}
}
