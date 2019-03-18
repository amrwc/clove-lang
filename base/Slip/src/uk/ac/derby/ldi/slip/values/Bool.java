package uk.ac.derby.ldi.slip.values;

public class Bool extends AbstractValue {

	private static final long serialVersionUID = 0;
	
	private static Bool theTrue = new Bool(true);
	private static Bool theFalse = new Bool(false);
	
	private boolean internalValue;
	
	public static Value getTrue() {
		return theTrue;
	}
	
	public static Value getFalse() {
		return theFalse;
	}
	
	public Bool(boolean b) {
		internalValue = b;
	}
	
	public String getTypeName() {
		return "boolean";
	}
	
	/** Convert this to a primitive boolean. */
	public boolean booleanValue() {
		return internalValue;
	}
	
	public Value or(Value v) {
		return new Bool(internalValue || v.booleanValue());
	}

	public Value and(Value v) {
		return new Bool(internalValue && v.booleanValue());
	}

	public Value not() {
		return new Bool(!internalValue);
	}

	public int compareTo(Value v) {
		if (internalValue == v.booleanValue())
			return 0;
		else if (internalValue)
			return 1;
		else
			return -1;
	}
	
	public int hashCode() {
		return (internalValue) ? 1 : 2;
	}
	
	public String toString() {
		return "" + internalValue;
	}
}
