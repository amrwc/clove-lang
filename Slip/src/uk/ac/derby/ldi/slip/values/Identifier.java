package uk.ac.derby.ldi.slip.values;

import uk.ac.derby.ldi.slip.exceptions.*;

public class Identifier extends Operator {
	
	private static final long serialVersionUID = 0;
	
	private String name;
	
	/** Future Optimisation:  If the resolver doesn't change, use a cached version
	 * of the retrieved value.
	 */
	
	public Value evaluate(Resolver resolver) {
		Value v = resolver.find(new Str(name));
		if (v == null)
			return this;		
		else
			return v;
	}
	
	public Value evaluate(Resolver resolver, Walker iterator) {
		Value v = resolver.find(new Str(name));
		if (v == null)
			throw new Fatal("Operator or variable '" + name + "' not found.");
		else
			return v.evaluate(resolver, iterator);
	}
	
	public Identifier(String id) {
		name = id;
		if (name.indexOf(' ') >= 0)
			throw new Fatal("Identifier '" + id + "' may not contain spaces.");
	}
	
	public int compareTo(Value v) {
		return name.compareTo(v.stringValue());		
	}
	
	public int hashCode() {
		return name.hashCode();
	}
	
	public String getOperatorName() {
		return "identifier";
	}
	
	/** Convert this to a String. */
	public String toString() {
		return name;		
	}
}
