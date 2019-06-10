package values;

import utils.NumberUtils;

/**
 * @author amrwc
 */
public class ValueDouble extends ValueAbstract {
	private final double internalValue;

	public ValueDouble(double v) {
		internalValue = v;
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

	/** Convert this to a primitive String. */
	@Override
	public String stringValue() {
		return "" + internalValue;
	}

	@Override
	public String toString() {
		return stringValue();
	}

	@Override
	public double doubleValue() {
		return internalValue;
	}

	@Override
	public int compare(Value v) {
		return NumberUtils.compareNumberValues(this, v);
	}

	@Override
	public Value add(Value v) {
		return NumberUtils.doOperation(this, "add", v);
	}

	@Override
	public Value subtract(Value v) {
		return NumberUtils.doOperation(this, "subtract", v);
	}

	@Override
	public Value mult(Value v) {
		return NumberUtils.doOperation(this, "mult", v);
	}

	@Override
	public Value div(Value v) {
		return NumberUtils.doOperation(this, "div", v);
	}

	@Override
	public Value mod(Value v) {
		return NumberUtils.doOperation(this, "mod", v);
	}

	@Override
	public Value unary_plus() {
		return new ValueDouble(internalValue);
	}

	@Override
	public Value unary_minus() {
		return new ValueDouble(-internalValue);
	}
}
