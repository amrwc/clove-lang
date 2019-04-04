package uk.ac.derby.ldi.sili2.values;

import java.util.HashMap;

public class ValueObject extends ValueAbstract {
	
	private HashMap<String, Value> internalValue = new HashMap<String, Value>();
	
	public ValueObject() {}
	
	public String getName() {
		return "Object";
	}

	public int compare(Value v) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void add(String name, Value v) {
		internalValue.put(name, v);
		System.out.println(internalValue.get(name));
	}
	
	/** Convert this to a String. */
//	public String stringValue() {
//		return internalValue;		
//	}

//	public int compare(Value v) {
//		return internalValue.compareTo(v.stringValue());
//	}
	
	/** Add performs string concatenation. */
//	public Value add(Value v) {
//		return new ValueObject(internalValue + v.stringValue());
//	}
	
//	public String toString() {
//		return internalValue;
//	}
}
