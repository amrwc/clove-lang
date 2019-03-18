package uk.ac.derby.ldi.slip.values;

public class Str extends AbstractValue {
	
	private static final long serialVersionUID = 0;

	protected String internalValue;
	
	public Str(String b) {
		internalValue = b;
	}
	
	public String getTypeName() {
		return "string";
	}
	
	/** Convert this to a String. */
	public String toString() {
		return internalValue;		
	}
	
	public int compareTo(Value v) {
		return internalValue.compareTo(v.stringValue());
	}
	
	public int hashCode() {
		return internalValue.hashCode();
	}
	
	/** Add performs string concatenation. */
	public Value add(Value v) {
		return new Str(internalValue + v.stringValue());
	}
}
