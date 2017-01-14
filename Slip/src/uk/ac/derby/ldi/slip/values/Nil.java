package uk.ac.derby.ldi.slip.values;

public class Nil extends AbstractValue {

	private static final long serialVersionUID = 0;
	private static Nil value = new Nil();
	
	/** Nil is singleton. */
	private Nil() {
	}
	
	public static Nil getInstance() {
		return value;
	}
	
	public String getTypeName() {
		return "nil";
	}
	
	/** True if this is null.  Only Nil should return true. */
	public boolean isNil() {
		return true;
	}

	public int compareTo(Value v) {
		if (v instanceof Nil)
			return 0;
		else
			return 1;
	}
	
	public String toString() {
		return "nil";
	}
}
