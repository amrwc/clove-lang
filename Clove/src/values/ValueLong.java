package values;

public class ValueLong extends ValueAbstract {
	private final long internalValue;

	public ValueLong(long v) {
		internalValue = v;
	}

	@Override
	public String getName() {
		return "long";
	}

	@SuppressWarnings("unchecked")
	@Override
	public Long getRawValue() {
		return internalValue;
	}

	/** Convert this to a primitive double. */
	@Override
	public double doubleValue() {
		return internalValue;
	}

	/** Convert this to a primitive String. */
	@Override
	public String stringValue() {
		return "" + internalValue;
	}

	@Override
	public int compare(Value v) {
		if (internalValue == (long) v.getRawValue())
			return 0;
		else if (internalValue > (long) v.getRawValue())
			return 1;
		else
			return -1;
	}

	/**
	 * Tries to parse a double value to a ValueLong. If it's impossible, returns
	 * a ValueRational.
	 * 
	 * @read https://stackoverflow.com/a/9898528/10620237
	 * @returns {ValueInteger/ValueRational} cast attempt result
	 * @author amrwc
	 */
	private Value tryLong(double v) {
		if ((v == Math.floor(v)) && !Double.isInfinite(v))
			return new ValueLong((long) Math.floor(v));
		else
			return new ValueDouble(v);
	}

	@Override
	public Value add(Value v) {
		return tryLong(internalValue + v.doubleValue());
	}

	@Override
	public Value subtract(Value v) {
		return tryLong(internalValue - v.doubleValue());
	}

	@Override
	public Value mult(Value v) {
		return tryLong(internalValue * v.doubleValue());
	}

	@Override
	public Value div(Value v) {
		return tryLong(internalValue / v.doubleValue());
	}

	@Override
	public Value mod(Value v) {
		return tryLong(internalValue % v.doubleValue());
	}

	@Override
	public Value unary_plus() {
		return new ValueLong(internalValue);
	}

	@Override
	public Value unary_minus() {
		return new ValueLong(-internalValue);
	}

	@Override
	public String toString() {
		return stringValue();
	}
}
