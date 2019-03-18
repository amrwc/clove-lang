package uk.ac.derby.ldi.slip.values;

import java.util.*;

/** A transactional set of storage slots. */
public class Slots extends AbstractValue {

	private static final long serialVersionUID = 0;

	private class Slot {
		private Value value;
		
		public Slot(Value v) {
			value = v;
		}
		
		public final Value getValue() {
			return value;
		}
	}
	
	private HashMap<Value, Slot> storage = new HashMap<Value, Slot>();
	
	/** Get a value from this Resolver.  Return null if not found. */
	public Value get(Value key) {
		Slot slot = storage.get(key);
		return (slot != null) ? slot.getValue() : null;
	}
	
	/** Insert a new Value into this Resolver. */
	public void put(Value key, Value value) {
		storage.put(key, new Slot(value));
	}

	public String getTypeName() {
		return "slots";
	}

}
