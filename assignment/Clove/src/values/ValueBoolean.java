package values;

public class ValueBoolean extends ValueAbstract {

	private final boolean internalValue;
	
	public ValueBoolean(boolean b) {
		internalValue = b;
	}
	
	@Override
	public String getName() {
		return "boolean";
	}
	
	/** Convert this to a primitive boolean. */
	@Override
	public boolean booleanValue() {
		return internalValue;
	}
	
	/** Convert this to a primitive string. */
	@Override
	public String stringValue() {
		return (internalValue) ? "true" : "false";
	}
	
	@Override
	public Value or(Value v) {
		return new ValueBoolean(internalValue || v.booleanValue());
	}

	@Override
	public Value and(Value v) {
		return new ValueBoolean(internalValue && v.booleanValue());
	}

	@Override
	public Value not() {
		return new ValueBoolean(!internalValue);
	}

	@Override
	public int compare(Value v) {
		if (internalValue == v.booleanValue())
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
