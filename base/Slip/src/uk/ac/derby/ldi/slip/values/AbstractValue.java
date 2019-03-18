package uk.ac.derby.ldi.slip.values;

import java.io.*;

import uk.ac.derby.ldi.slip.exceptions.Fatal;

/** AbstractValue provides default abstract Value behaviour. */
public abstract class AbstractValue implements Value {

	private static final long serialVersionUID = 1L;

	public int compareTo(Value v) {
		throw new Fatal("Comparison not supported by " + getTypeName());
	}

	/** Evaluate this Value as an operator. */
	public Value evaluate(Resolver resolver, Walker arguments) {
		if (arguments.hasNext())
			throw new Fatal("Ignored list elements after " + getTypeName());
		return this;
	}
	
	/** Evaluate this Value as a non-operator. */
	public Value evaluate(Resolver resolver) {
		return this;
	}
	
	/** Convert this to a primitive boolean. */
	public boolean booleanValue() {
		throw new Fatal("Cannot convert " + getTypeName() + " to boolean.");
	}
		
	/** Convert this to a primitive integer. */
	public long longValue() {
		throw new Fatal("Cannot convert " + getTypeName() + " to integer.");
	}
	
	/** Convert this to a primitive double. */
	public double doubleValue() {
		throw new Fatal("Cannot convert " + getTypeName() + " to float.");
	}
	
	/** Convert this to a String. */
	public String stringValue() {
		return toString();		
	}

	/** Output this to a stream. */
	public void toStream(PrintStream p) {
		p.print(stringValue());
	}
	
	public Value or(Value v) {
		throw new Fatal("Cannot perform logical OR on " + getTypeName() + ".");
	}

	public Value and(Value v) {
		throw new Fatal("Cannot perform logical AND on " + getTypeName() + ".");
	}

	public Value not() {
		throw new Fatal("Cannot perform logical NOT on " + getTypeName() + ".");
	}

	private Value invalid() {
		throw new Fatal("Cannot perform arithmetic on " + getTypeName() + ".");
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
	
	/** Test this value and another for equality in a Java context. */
	public boolean equals(Object o) {
		return compareTo((Value)o) == 0;
	}
	
	/** Test this value and another for equality. */
	public Value eq(Value v) {
		return new Bool(compareTo(v) == 0);
	}
	
	/** Test this value and another for non-equality. */
	public Value neq(Value v) {
		return new Bool(compareTo(v) != 0);
	}
	
	/** Test this value and another for >= */
	public Value gte(Value v) {
		return new Bool(compareTo(v) >= 0);
	}
	
	/** Test this value and another for <= */
	public Value lte(Value v) {
		return new Bool(compareTo(v) <= 0);
	}
	
	/** Test this value and another for > */
	public Value gt(Value v) {
		return new Bool(compareTo(v) > 0);
	}
	
	/** Test this value and another for < */	
	public Value lt(Value v) {
		return new Bool(compareTo(v) < 0);
	}
		
	/** True if this is null.  Only Nil should return true. */
	public boolean isNil() {
		return false;
	}

}
