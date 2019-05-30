package values;

import interpreter.NumberOperations;

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
