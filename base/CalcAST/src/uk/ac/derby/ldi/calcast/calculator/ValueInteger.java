package uk.ac.derby.ldi.calcast.calculator;

public class ValueInteger extends ValueAbstract {

	private int internalValue;
	
	public ValueInteger(int b) {
		internalValue = b;
	}
	
	/** Convert this to a primitive boolean. */
	public boolean booleanValue() {
		throw new ExceptionSemantic("Cannot convert integer to boolean.");
	}
	
	/** Convert this to a primitive integer. */
	public int intValue() {
		return internalValue;
	}
	
	/** Convert this to a primitive double. */
	public double doubleValue() {
		return (double)internalValue;
	}
	
	public Value or(Value v) {
		throw new ExceptionSemantic("Cannot perform logical OR on integer.");
	}

	public Value and(Value v) {
		throw new ExceptionSemantic("Cannot perform logical AND on integer.");
	}

	public Value not() {
		throw new ExceptionSemantic("Cannot perform logical NOT on integer.");
	}

	public int compare(Value v) {
		if (internalValue == v.intValue())
			return 0;
		else if (internalValue > v.intValue())
			return 1;
		else
			return -1;
	}
	
	public Value add(Value v) {
		return new ValueInteger(internalValue + v.intValue());
	}

	public Value subtract(Value v) {
		return new ValueInteger(internalValue - v.intValue());
	}

	public Value mult(Value v) {
		return new ValueInteger(internalValue * v.intValue());
	}

	public Value div(Value v) {
		return new ValueInteger(internalValue / v.intValue());
	}

	public Value unary_plus() {
		return new ValueInteger(internalValue);
	}

	public Value unary_minus() {
		return new ValueInteger(-internalValue);
	}
	
	public String toString() {
		return "" + internalValue;
	}
}
