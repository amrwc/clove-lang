package uk.ac.derby.ldi.sili.values;

public class ValueInteger extends ValueAbstract {
	private static final ValueInteger _zero = new ValueInteger(0);

	private long internalValue;

	/* Methods used by XML serialization and nothing else. */

	public ValueInteger() {
	}

	public long getValue() {
		return internalValue;
	}
	
	public void setValue(long v) {
		internalValue = v;
	}
	
	/* Public methods from here on down. */
	
	public static ValueInteger getZero() {
		return _zero;
	}
	
	public ValueInteger(long b) {
		internalValue = b;
	}

	public String getTypeName() {
		return "INTEGER";
	}

	/** Convert this to a primitive boolean. */
	public boolean booleanValue() {
		return (internalValue != 0) ? true : false;
	}
	
	/** Convert this to a primitive long. */
	public long longValue() {
		return internalValue;
	}
	
	/** Convert this to a primitive double. */
	public double doubleValue() {
		return (double)internalValue;
	}
	
	/** Convert this to a primitive String. */
	public String stringValue() {
		return "" + internalValue;
	}

	public int compareTo(Value v) {
		if (internalValue == v.longValue())
			return 0;
		else if (internalValue > v.longValue())
			return 1;
		else
			return -1;
	}
	
	public Value add(Value v) {
		return new ValueInteger(internalValue + v.longValue());
	}

	public Value subtract(Value v) {
		return new ValueInteger(internalValue - v.longValue());
	}

	public Value mult(Value v) {
		return new ValueInteger(internalValue * v.longValue());
	}

	public Value div(Value v) {
		return new ValueInteger(internalValue / v.longValue());
	}

	public Value unary_plus() {
		return new ValueInteger(internalValue);
	}

	public Value unary_minus() {
		return new ValueInteger(-internalValue);
	}
	
	public String toString() {
		return "" + internalValue;
	}
}
