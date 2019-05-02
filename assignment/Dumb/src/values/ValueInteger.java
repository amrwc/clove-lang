package values;

public class ValueInteger extends ValueAbstract {

	private long internalValue;
	
	public ValueInteger(long b) {
		internalValue = b;
	}
	
	public String getName() {
		return "integer";
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

	public int compare(Value v) {
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

	public Value mod(Value v) {
		return new ValueInteger(internalValue % v.longValue());
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
