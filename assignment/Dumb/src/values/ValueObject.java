package values;

import java.util.HashMap;

import interpreter.ExceptionSemantic;

public class ValueObject extends ValueAbstract {
	
	private HashMap<String, Value> internalValue = new HashMap<String, Value>();
	
	public ValueObject() {}
	
	public String getName() {
		return "Object";
	}

	public int compare(Value v) {
		// TODO Auto-generated method stub
//		return internalValue.compareTo(v);
		return 0;
	}
	
	public void add(String name, Value v) {
//		System.out.print("DEBUG, ValueObject.add: "); // DEBUG:
//		System.out.println(internalValue.get(name)); // DEBUG:
		internalValue.put(name, v);
	}
	
	public Value get(String name) {
//		System.out.print("DEBUG, ValueObject.get: "); // DEBUG:
//		System.out.println(internalValue.get(name)); // DEBUG:
//		if (internalValue.)
		return internalValue.get(name);
	}
	
	public void set(String name, Value v) {
//		System.out.print("DEBUG, ValueObject.set: "); // DEBUG:
//		System.out.println("oldValue: " + internalValue.put(name, v)); // DEBUG:
		internalValue.put(name, v);
	}

	public void remove(String name) {
		if (internalValue.containsKey(name)) {
			internalValue.remove(name);
		} else {
			throw new ExceptionSemantic("This ValueObject does not contain the \"" + name + "\" key.");
		}
	}

	public void tryRemove(String name) {
		internalValue.remove(name);
	}

	public String toString() {
		return internalValue.toString();
	}
}
