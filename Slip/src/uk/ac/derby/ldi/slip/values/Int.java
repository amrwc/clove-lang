package uk.ac.derby.ldi.slip.values;

public class Int extends AbstractValue {
	
	private static final long serialVersionUID = 0;

	private long internalValue;
	
	public Int(long b) {
		internalValue = b;
	}
	
	public String getTypeName() {
		return "integer";
	}

	/** Convert this to a primitive integer. */
	public long longValue() {
		return internalValue;
	}
	
	/** Convert this to a primitive double. */
	public double doubleValue() {
		return (double)internalValue;
	}

	public int compareTo(Value v) {
		if (internalValue == v.longValue())
			return 0;
		else if (internalValue > v.longValue())
			return 1;
		else
			return -1;
	}
	
	// Based on Sun's implementation of Long
	public int hashCode() {
		return (int)(internalValue ^ (internalValue >>> 32));
	}
	
	public Value add(Value v) {
		return new Int(internalValue + v.longValue());
	}

	public Value subtract(Value v) {
		return new Int(internalValue - v.longValue());
	}

	public Value mult(Value v) {
		return new Int(internalValue * v.longValue());
	}

	public Value div(Value v) {
		return new Int(internalValue / v.longValue());
	}

	public Value unary_plus() {
		return new Int(internalValue);
	}

	public Value unary_minus() {
		return new Int(-internalValue);
	}
	
	public String toString() {
		return "" + internalValue;
	}
}
