package values;

import interpreter.ExceptionSemantic;
import interpreter.NumberOperations;
import interpreter.NumberUtils;

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
		// If one of the values is not a number-type...
		if (!NumberUtils.isNumber(internalValue)
				|| !NumberUtils.isNumber(v.getRawValue())) {
			throw new ExceptionSemantic(
					"Cannot compare '" + getName() + " (" + internalValue + ")' and '"
							+ v.getName() + " (" + v.getRawValue() + ")'.");
		}

		final double inDouble = Double.parseDouble(stringValue());
		final double vDouble = Double.parseDouble(v.stringValue());
		return Double.compare(inDouble, vDouble);
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
