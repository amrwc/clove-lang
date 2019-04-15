package values;

public class ValueRational extends ValueAbstract {

	private double internalValue;
	
	public ValueRational(double b) {
		internalValue = b;
	}
	
	public String getName() {
		return "rational";
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
		if (internalValue == v.doubleValue())
			return 0;
		else if (internalValue > v.doubleValue())
			return 1;
		else
			return -1;
	}
	
	public Value add(Value v) {
		return new ValueRational(internalValue + v.doubleValue());
	}

	public Value subtract(Value v) {
		return new ValueRational(internalValue - v.doubleValue());
	}

	public Value mult(Value v) {
		return new ValueRational(internalValue * v.doubleValue());
	}

	public Value div(Value v) {
		return new ValueRational(internalValue / v.doubleValue());
	}

	public Value unary_plus() {
		return new ValueRational(internalValue);
	}

	public Value unary_minus() {
		return new ValueRational(-internalValue);
	}
	
	public String toString() {
		return "" + internalValue;
	}
}
