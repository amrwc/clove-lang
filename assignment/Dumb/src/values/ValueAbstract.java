package values;

import interpreter.ExceptionSemantic;

public abstract class ValueAbstract implements Value {

	public abstract String getName();

	public abstract int compare(Value v);
	
	public Value or(Value v) {
		throw new ExceptionSemantic("Cannot perform OR on " + getName() + " and " + v.getName());
	}

	public Value and(Value v) {
		throw new ExceptionSemantic("Cannot perform AND on " + getName() + " and " + v.getName());
	}

	public Value not() {
		throw new ExceptionSemantic("Cannot perform NOT on " + getName());
	}

	public Value add(Value v) {
		throw new ExceptionSemantic("Cannot perform + on " + getName() + " and " + v.getName());
	}

	public Value subtract(Value v) {
		throw new ExceptionSemantic("Cannot perform - on " + getName() + " and " + v.getName());
	}

	public Value mult(Value v) {
		throw new ExceptionSemantic("Cannot perform * on " + getName() + " and " + v.getName());
	}

	public Value div(Value v) {
		throw new ExceptionSemantic("Cannot perform / on " + getName() + " and " + v.getName());
	}

	public Value unary_plus() {
		throw new ExceptionSemantic("Cannot perform + on " + getName());
	}

	public Value unary_minus() {
		throw new ExceptionSemantic("Cannot perform - on " + getName());
	}
		
	/** Convert this to a primitive boolean. */
	public boolean booleanValue() {
		throw new ExceptionSemantic("Cannot convert " + getName() + " to boolean.");
	}

	/** Convert this to a primitive long. */
	public long longValue() {
		throw new ExceptionSemantic("Cannot convert " + getName() + " to integer.");
	}

	/** Convert this to a primitive double. */
	public double doubleValue() {
		throw new ExceptionSemantic("Cannot convert " + getName() + " to rational.");
	}

	/** Convert this to a primitive string. */
	public String stringValue() {
		throw new ExceptionSemantic("Cannot convert " + getName() + " to string.");
	}

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
