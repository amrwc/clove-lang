package uk.ac.derby.ldi.slip.values;

public class Rational extends AbstractValue {
	
	private static final long serialVersionUID = 0;

	private double internalValue;
	
	public Rational(double b) {
		internalValue = b;
	}
	
	public String getTypeName() {
		return "float";
	}
	
	/** Convert this to a primitive integer. */
	public long longValue() {
		return (long)internalValue;
	}
	
	/** Convert this to a primitive double. */
	public double doubleValue() {
		return (double)internalValue;
	}

	public int compareTo(Value v) {
		if (internalValue == v.doubleValue())
			return 0;
		else if (internalValue > v.doubleValue())
			return 1;
		else
			return -1;
	}
	
	// Based on Sun's Double class
	public int hashCode() {
		long bits = Double.doubleToLongBits(internalValue);
		return (int)(bits ^ (bits >>> 32));	
	}
	
	public Value add(Value v) {
		return new Rational(internalValue + v.doubleValue());
	}

	public Value subtract(Value v) {
		return new Rational(internalValue - v.doubleValue());
	}

	public Value mult(Value v) {
		return new Rational(internalValue * v.doubleValue());
	}

	public Value div(Value v) {
		return new Rational(internalValue / v.doubleValue());
	}

	public Value unary_plus() {
		return new Rational(internalValue);
	}

	public Value unary_minus() {
		return new Rational(-internalValue);
	}
	
	public String toString() {
		return "" + internalValue;
	}
}
