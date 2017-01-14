package uk.ac.derby.ldi.calcast.calculator;

public abstract class ValueAbstract implements Value {

	public abstract Value or(Value v);

	public abstract Value and(Value v);

	public abstract Value not();

	public abstract int compare(Value v);

	public abstract Value add(Value v);

	public abstract Value subtract(Value v);

	public abstract Value mult(Value v);

	public abstract Value div(Value v);

	public abstract Value unary_plus();

	public abstract Value unary_minus();
		
	/** Convert this to a primitive boolean. */
	public abstract boolean booleanValue();
	
	/** Convert this to a primitive integer. */
	public abstract int intValue();
	
	/** Convert this to a primitive double. */
	public abstract double doubleValue();

	/** Test this value and another for equality. */
	public Value eq(Value v) {
		return new ValueBoolean(compare(v) == 0);
	}
	
	/** Test this value and another for non-equality. */
	public Value neq(Value v) {
		return new ValueBoolean(compare(v) != 0);
	}
	
	/** Test this value and another for >= */
	public Value gte(Value v) {
		return new ValueBoolean(compare(v) >= 0);
	}
	
	/** Test this value and another for <= */
	public Value lte(Value v) {
		return new ValueBoolean(compare(v) <= 0);
	}
	
	/** Test this value and another for > */
	public Value gt(Value v) {
		return new ValueBoolean(compare(v) > 0);
	}
	
	/** Test this value and another for < */	
	public Value lt(Value v) {
		return new ValueBoolean(compare(v) < 0);
	}
}
