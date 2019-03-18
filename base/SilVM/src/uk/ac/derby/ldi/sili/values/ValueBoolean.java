package uk.ac.derby.ldi.sili.values;

public class ValueBoolean extends ValueAbstract {

	private static final ValueBoolean _true = new ValueBoolean(true);
	private static final ValueBoolean _false = new ValueBoolean(false);
	
	private boolean internalValue;

	/* Methods used by XML serialization and nothing else. */

	public ValueBoolean() {
	}

	public boolean getValue() {
		return internalValue;
	}
	
	public void setValue(boolean v) {
		internalValue = v;
	}
	
	/* Public methods from here on down. */
	
	public static ValueBoolean getTrue() {
		return _true;
	}
	
	public static ValueBoolean getFalse() {
		return _false;
	}
	
	public static ValueBoolean getBoolean(boolean b) {
		return (b) ? _true : _false;
	}
	
	protected ValueBoolean(boolean b) {
		internalValue = b;
	}

	public String getTypeName() {
		return "BOOLEAN";
	}
	
	/** Convert this to a primitive boolean. */
	public boolean booleanValue() {
		return internalValue;
	}
	
	/** Convert this to a primitive long. */
	public long longValue() {
		return (internalValue) ? 1 : 0;
	}
	
	/** Convert this to a primitive double. */
	public double doubleValue() {
		return (internalValue) ? 1 : 0;
	}
	
	/** Convert this to a primitive string. */
	public String stringValue() {
		return (internalValue) ? "true" : "false";
	}
	
	public Value xor(Value v) {
		return new ValueBoolean(internalValue ^ v.booleanValue());		
	}
	
	public Value or(Value v) {
		return new ValueBoolean(internalValue || v.booleanValue());
	}

	public Value and(Value v) {
		return new ValueBoolean(internalValue && v.booleanValue());
	}

	public Value not() {
		return new ValueBoolean(!internalValue);
	}

	public int compareTo(Value v) {
		if (internalValue == v.booleanValue())
			return 0;
		else if (internalValue)
			return 1;
		else
			return -1;
	}
	
	public String toString() {
		return "" + internalValue;
	}
}
