package uk.ac.derby.ldi.sili.values;

import java.io.PrintStream;

import uk.ac.derby.ldi.sili.exceptions.ExceptionFatal;

public abstract class ValueAbstract implements Value {

	private static final long serialVersionUID = 1L;

	/** Output this Value to a PrintStream. */
	public void toStream(PrintStream p, int depth) {
		if (depth > 0)
			p.print(toParsableString());
		else
			p.print(toString());
	}
	
	public String toString() {
		return toString();
	}
	
	public String toParsableString() {
		return toString();
	}
	
	public boolean equals(Object o) {
		return (compareTo((Value)o) == 0);
	}
	
	public Value xor(Value v) {
		throw new ExceptionFatal("Cannot perform XOR on " + getTypeName() + " and " + v.getTypeName());
	}
	
	public Value or(Value v) {
		throw new ExceptionFatal("Cannot perform OR on " + getTypeName() + " and " + v.getTypeName());
	}

	public Value and(Value v) {
		throw new ExceptionFatal("Cannot perform AND on " + getTypeName() + " and " + v.getTypeName());
	}

	public Value not() {
		throw new ExceptionFatal("Cannot perform NOT on " + getTypeName());
	}

	public Value add(Value v) {
		throw new ExceptionFatal("Cannot perform + on " + getTypeName() + " and " + v.getTypeName());
	}

	public Value subtract(Value v) {
		throw new ExceptionFatal("Cannot perform - on " + getTypeName() + " and " + v.getTypeName());
	}

	public Value mult(Value v) {
		throw new ExceptionFatal("Cannot perform * on " + getTypeName() + " and " + v.getTypeName());
	}

	public Value div(Value v) {
		throw new ExceptionFatal("Cannot perform / on " + getTypeName() + " and " + v.getTypeName());
	}

	public Value unary_plus() {
		throw new ExceptionFatal("Cannot perform + on " + getTypeName());
	}

	public Value unary_minus() {
		throw new ExceptionFatal("Cannot perform - on " + getTypeName());
	}
		
	/** Convert this to a primitive boolean. */
	public boolean booleanValue() {
		throw new ExceptionFatal("Cannot convert " + getTypeName() + " to boolean.");
	}

	/** Convert this to a primitive long. */
	public long longValue() {
		throw new ExceptionFatal("Cannot convert " + getTypeName() + " to integer.");
	}

	/** Convert this to a primitive double. */
	public double doubleValue() {
		throw new ExceptionFatal("Cannot convert " + getTypeName() + " to rational.");
	}

	/** Convert this to a primitive string. */
	public String stringValue() {
		throw new ExceptionFatal("Cannot convert " + getTypeName() + " to character.");
	}

	/** Test this value and another for equality. */
	public Value eq(Value v) {
		return new ValueBoolean(compareTo(v) == 0);
	}
	
	/** Test this value and another for non-equality. */
	public Value neq(Value v) {
		return new ValueBoolean(compareTo(v) != 0);
	}
	
	/** Test this value and another for >= */
	public Value gte(Value v) {
		return new ValueBoolean(compareTo(v) >= 0);
	}
	
	/** Test this value and another for <= */
	public Value lte(Value v) {
		return new ValueBoolean(compareTo(v) <= 0);
	}
	
	/** Test this value and another for > */
	public Value gt(Value v) {
		return new ValueBoolean(compareTo(v) > 0);
	}
	
	/** Test this value and another for < */	
	public Value lt(Value v) {
		return new ValueBoolean(compareTo(v) < 0);
	}
}
