package values;

public class ValueInteger extends ValueAbstract {
	private final int internalValue;

	public ValueInteger(int v) {
		internalValue = v;
	}

	@Override
	public String getName() {
		return "integer";
	}

	@SuppressWarnings("unchecked")
	@Override
	public Integer getRawValue() {
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
		if (internalValue == (int) v.getRawValue())
			return 0;
		else if (internalValue > (int) v.getRawValue())
			return 1;
		else
			return -1;
	}

	/**
	 * Tries to parse a double value to a ValueInteger. If it's impossible, returns
	 * a ValueRational.
	 * 
	 * @read https://stackoverflow.com/a/9898528/10620237
	 * @returns {ValueInteger/ValueRational} cast attempt result
	 * @author amrwc
	 */
	private Value tryInt(double v) {
		if ((v == Math.floor(v)) && !Double.isInfinite(v))
			return new ValueInteger((int) Math.floor(v));
		else
			return new ValueRational(v);
	}

	@Override
	public Value add(Value v) {
		return tryInt(internalValue + v.doubleValue());
	}

	@Override
	public Value subtract(Value v) {
		return tryInt(internalValue - v.doubleValue());
	}

	@Override
	public Value mult(Value v) {
		return tryInt(internalValue * v.doubleValue());
	}

	@Override
	public Value div(Value v) {
		return tryInt(internalValue / v.doubleValue());
	}

	@Override
	public Value mod(Value v) {
		return tryInt(internalValue % v.doubleValue());
	}

	@Override
	public Value unary_plus() {
		return new ValueInteger(internalValue);
	}

	@Override
	public Value unary_minus() {
		return new ValueInteger(-internalValue);
	}

	@Override
	public String toString() {
		return stringValue();
	}
}
