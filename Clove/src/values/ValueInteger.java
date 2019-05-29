package values;

import interpreter.NumberUtils;

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

	@Override
	public Value add(Value v) {
		return NumberUtils.tryInt(internalValue + v.doubleValue());
	}

	@Override
	public Value subtract(Value v) {
		return NumberUtils.tryInt(internalValue - v.doubleValue());
	}

	@Override
	public Value mult(Value v) {
		return NumberUtils.tryInt(internalValue * v.doubleValue());
	}

	@Override
	public Value div(Value v) {
		return NumberUtils.tryInt(internalValue / v.doubleValue());
	}

	@Override
	public Value mod(Value v) {
		return NumberUtils.tryInt(internalValue % v.doubleValue());
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
