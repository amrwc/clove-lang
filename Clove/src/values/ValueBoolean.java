package values;

import interpreter.ExceptionSemantic;

/**
 * @author dave
 */
public class ValueBoolean extends ValueAbstract {
	private final boolean internalValue;

	public ValueBoolean(boolean b) {
		internalValue = b;
	}

	@Override
	public String getName() {
		return "boolean";
	}

	@SuppressWarnings("unchecked")
	@Override
	public Boolean getRawValue() {
		return internalValue;
	}

	/** Convert this to a primitive string. */
	@Override
	public String stringValue() {
		return (internalValue) ? "true" : "false";
	}

	@Override
	public Value or(Value v) {
		return new ValueBoolean(internalValue || (boolean) v.getRawValue());
	}

	@Override
	public Value and(Value v) {
		return new ValueBoolean(internalValue && (boolean) v.getRawValue());
	}

	@Override
	public Value not() {
		return new ValueBoolean(!internalValue);
	}

	@Override
	public int compare(Value v) {
		if (!(v.getRawValue() instanceof Boolean)) {
			throw new ExceptionSemantic(
					"Cannot compare '" + getName() + " (" + internalValue + ")' and '"
							+ v.getName() + " (" + v.getRawValue() + ")'.");
		}

		if (internalValue == (boolean) v.getRawValue())
			return 0;
		else if (internalValue)
			return 1;
		else
			return -1;
	}

	@Override
	public String toString() {
		return "" + internalValue;
	}
}
