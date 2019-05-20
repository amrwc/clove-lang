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
		return (double) internalValue;
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

	/**
	 * Tries to parse a double value to a ValueInteger. If it's
	 * impossible, returns a ValueRational.
	 * 
	 * @read https://stackoverflow.com/a/9898528/10620237
	 * @returns {ValueInteger/ValueRational} cast attempt result
	 * @author amrwc
	 */
	private Value tryInt(double v) {
		if ((v == Math.floor(v)) && !Double.isInfinite(v))
		    return new ValueInteger((long) Math.floor(v));
		else
			return new ValueRational(v);
	}

	public Value add(Value v) {
		return tryInt(internalValue + v.doubleValue());
	}

	public Value subtract(Value v) {
		return tryInt(internalValue - v.doubleValue());
	}

	public Value mult(Value v) {
		return tryInt(internalValue * v.doubleValue());
	}

	public Value div(Value v) {
		return tryInt(internalValue / v.doubleValue());
	}

	public Value mod(Value v) {
		return tryInt(internalValue % v.doubleValue());
	}

	public Value unary_plus() {
		return new ValueInteger(internalValue);
	}

	public Value unary_minus() {
		return new ValueInteger(-internalValue);
	}

	public String toString() {
		return stringValue();
	}
}
