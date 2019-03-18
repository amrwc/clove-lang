package uk.ac.derby.ldi.slip.values;

/** Given a name, a Resolver returns a Value associated with the name. */
public class Resolver extends AbstractValue {

	private static final long serialVersionUID = 0;
	
	private Resolver parent;
	private Slots storage;

	/** Create a child resolver.  Searches will take place first in the child, then
	 * the parent(s). */
	public Resolver(Resolver parentResolver) {
		parent = parentResolver;
		storage = new Slots();
	}
	/** Get a value from this Resolver.  Return null if not found. */
	public Value get(Value key) {
		return storage.get(key);
	}
	
	/** Insert a new Value into this Resolver. */
	public void put(Value key, Value value) {
		storage.put(key, value);
	}
	
	/** Attempt to find a key in the Resolver hierarchy.  Return null if not found. */
	public Value find(Value key) {
		Value v = get(key);
		if (v != null)
			return v;
		if (parent != null)
			return parent.find(key);
		return null;
	}
	
	/** Put a new Operator in this Resolver. */
	public void put(Operator operator) {
		put(new Str(operator.getOperatorName()), operator);
	}
	
	/** Set the value of the most local slot in the Resolver. 
	 * If not found in the root Resolver, create it in the root Resolver.
	 * 
	 * @param key - Value key
	 * @param value - Value to assign to variable
	 * @return - Value assigned
	 */
	public Value set(Value key, Value value) {
		if (get(key) != null)
			put(key, value);
		else if (parent != null)
			parent.put(key, value);
		else
			put(key, value);
		return value;
	}
	
	public String getTypeName() {
		return "resolver";
	}
}
