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

	/**
	 * @read https://stackoverflow.com/a/9898528/10620237
	 * @author amrwc
	 */
	public Value div(Value v) {
		var res = (v instanceof ValueRational)
			? internalValue / v.doubleValue()
			: internalValue / (double) v.longValue();

		// Try returning ValueInteger if the outcome can be parsed to int/long.
		if ((res == Math.floor(res)) && !Double.isInfinite(res))
		    return new ValueInteger((long) Math.floor(res));
		else
			return new ValueRational(res);
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
