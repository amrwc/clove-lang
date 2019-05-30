package values;

import interpreter.NumberOperations;

public class ValueFloat extends ValueAbstract {
	private final float internalValue;

	public ValueFloat(float v) {
		internalValue = v;
	}

	@Override
	public String getName() {
		return "float";
	}

	@SuppressWarnings("unchecked")
	@Override
	public Float getRawValue() {
		return internalValue;
	}

	/** Convert this to a primitive String. */
	@Override
	public String stringValue() {
		return "" + internalValue;
	}

	@Override
	public int compare(Value v) {
		if (internalValue == (float) v.getRawValue())
			return 0;
		else if (internalValue > (float) v.getRawValue())
			return 1;
		else
			return -1;
	}

	@Override
	public Value add(Value v) {
		return NumberOperations.doOperation(internalValue, "add", v);
	}

	@Override
	public Value subtract(Value v) {
		return NumberOperations.doOperation(internalValue, "subtract", v);
	}

	@Override
	public Value mult(Value v) {
		return NumberOperations.doOperation(internalValue, "mult", v);
	}

	@Override
	public Value div(Value v) {
		return NumberOperations.doOperation(internalValue, "div", v);
	}

	@Override
	public Value mod(Value v) {
		return NumberOperations.doOperation(internalValue, "mod", v);
	}

	@Override
	public Value unary_plus() {
		return new ValueFloat(internalValue);
	}

	@Override
	public Value unary_minus() {
		return new ValueFloat(-internalValue);
	}

	@Override
	public String toString() {
		return stringValue();
	}
}
