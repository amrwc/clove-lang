package values;

import interpreter.NumberUtils;

public class ValueDouble extends ValueAbstract {
	private final double internalValue;

	public ValueDouble(double b) {
		internalValue = b;
	}

	@Override
	public String getName() {
		return "double";
	}

	@SuppressWarnings("unchecked")
	@Override
	public Double getRawValue() {
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
		if (internalValue == v.doubleValue())
			return 0;
		else if (internalValue > v.doubleValue())
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
		return new ValueDouble(internalValue);
	}

	@Override
	public Value unary_minus() {
		return new ValueDouble(-internalValue);
	}

	@Override
	public String toString() {
		return stringValue();
	}
}
