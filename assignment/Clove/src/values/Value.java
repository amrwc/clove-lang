package values;

import java.util.ArrayList;

import interpreter.Parser;
import parser.ast.SimpleNode;

/** An abstract Value, that defines all possible operations on abstract ValueS.
 * 
 *  If an operation is not supported, throw SemanticException.
 */
public interface Value {

	/**
	 * Dereferences a value in a nested expression.
	 * 
	 * @param {SimpleNode} node -- node in question
	 * @param {Value} v -- value to be dereferenced
	 * @param {int} currChild -- current child of the node being parsed
	 * @param {Parser} p -- the instance of Parser currently running
	 * @returns {Value} the dereferenced value
	 */
	public Value dereference(SimpleNode node, Value v, int currChild, Parser p);

	/**
	 * Execute a prototype function.
	 * 
	 * @param {String} protoFunc -- prototype function name
	 * @param {ArrayList<Value>} protoArgs -- arguments for the function
	 * @returns {Value} result of the prototype function
	 * @author amrwc
	 */
	public Value execProto(String protoFunc, ArrayList<Value> protoArgs);

	/** Get name of this Value type. */
	public String getName();
	
	/** Perform logical OR on this value and another. */
	public Value or(Value v);
	
	/** Perform logical AND on this value and another. */
	public Value and(Value v);
	
	/** Perform logical NOT on this value. */  
	public Value not();
	
	/** Compare this value and another. */
	public int compare(Value v);
	
	/** Add this value to another. */
	public Value add(Value v);
	
	/** Subtract another value from this. */
	public Value subtract(Value v);
	
	/** Multiply this value with another. */
	public Value mult(Value v);
	
	/** Divide another value by this. */
	public Value div(Value v);

	/** Modulo operation. */
	public Value mod(Value v);
	
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

	/** Convert this to a primitive string. */
	public String stringValue();
	
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
}
