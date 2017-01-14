package uk.ac.derby.ldi.slip.values;

import java.io.*;

/** An abstract Value, that defines all possible operations on abstract ValueS.
 * 
 *  If an operation is not supported, throw exception.
 */
public interface Value extends Serializable, Comparable<Value> {
	
	/** Evaluate this Value as an operator invocation. 
	 * 
	 * @param resolver - Resolver context
	 * @param arguments - Walker list of arguments
	 * @return - Value
	 */
	public Value evaluate(Resolver resolver, Walker arguments);
	
	/** Evaluate this Value as a non-operator.
	 * 
	 * @param resolver - Resolver context
	 * @return - Value
	 */
	public Value evaluate(Resolver resolver);
	
	/** Obtain the type name for this kind of value. */
	public String getTypeName();
	
	/** Perform logical OR on this value and another. */
	public Value or(Value v);
	
	/** Perform logical AND on this value and another. */
	public Value and(Value v);
	
	/** Perform logical NOT on this value. */  
	public Value not();
	
	/** Compare this value and another. */
	public int compareTo(Value v);
	
	/** Add this value to another. */
	public Value add(Value v);
	
	/** Subtract another value from this. */
	public Value subtract(Value v);
	
	/** Multiply this value with another. */
	public Value mult(Value v);
	
	/** Divide another value by this. */
	public Value div(Value v);
	
	/** Return unary plus of this value. */
	public Value unary_plus();
	
	/** Return unary minus of this value. */
	public Value unary_minus();
	
	/** Convert this to a primitive boolean. */
	public boolean booleanValue();
	
	/** Convert this to a primitive long. */
	public long longValue();
	
	/** Convert this to a primitive double. */
	public double doubleValue();

	/** Convert this to a String. */
	public String stringValue();
	
	/** Output this to a stream. */
	public void toStream(PrintStream p);
	
	/** Test this value and another for equality. */
	public Value eq(Value v);
	
	/** Test this value and another for non-equality. */
	public Value neq(Value v);
	
	/** Test this value and another for >= */
	public Value gte(Value v);
	
	/** Test this value and another for <= */
	public Value lte(Value v);
	
	/** Test this value and another for > */
	public Value gt(Value v);
	
	/** Test this value and another for < */	
	public Value lt(Value v);
	
	/** True if this is nil.  Only Nil should return true. */
	public boolean isNil();
}
