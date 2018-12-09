package uk.ac.derby.ldi.calcast.calculator;

public class ValueBoolean extends ValueAbstract {

	private boolean internalValue;
	
	public ValueBoolean(boolean b) {
		internalValue = b;
	}
	
	/** Convert this to a primitive boolean. */
	public boolean booleanValue() {
		return internalValue;
	}
	
	/** Convert this to a primitive integer. */
	public int intValue() {
		throw new ExceptionSemantic("Cannot convert boolean to integer.");
	}
	
	/** Convert this to a primitive double. */
	public double doubleValue() {
		throw new ExceptionSemantic("Cannot convert boolean to floating point.");
	}
	
	public Value or(Value v) {
		return new ValueBoolean(internalValue || v.booleanValue());
	}

	public Value and(Value v) {
		return new ValueBoolean(internalValue && v.booleanValue());
	}

	public Value not() {
		return new ValueBoolean(!internalValue);
	}

	public int compare(Value v) {
		if (internalValue == v.booleanValue())
			return 0;
		else if (internalValue)
			return 1;
		else
			return -1;
	}

	private Value invalid() {
		throw new ExceptionSemantic("Cannot perform arithmetic on boolean values.");		
	}
	
	public Value add(Value v) {
		return invalid();
	}

	public Value subtract(Value v) {
		return invalid();
	}

	public Value mult(Value v) {
		return invalid();
	}

	public Value div(Value v) {
		return invalid();
	}

	public Value unary_plus() {
		return invalid();
	}

	public Value unary_minus() {
		return invalid();
	}
	
	public String toString() {
		return "" + internalValue;
	}
}
