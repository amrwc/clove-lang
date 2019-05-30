package values;

import interpreter.NumberOperations;

/**
 * @author dave
 * @author amrwc
 */
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

	/** Convert this to a primitive String. */
	@Override
	public String stringValue() {
		return "" + internalValue;
	}

	@Override
	public int compare(Value v) {
		final double vDouble = Double.parseDouble(v.stringValue());
		if (internalValue == vDouble)
			return 0;
		else if (internalValue > vDouble)
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
