package uk.ac.derby.ldi.slip.values;

import java.util.Vector;

import uk.ac.derby.ldi.slip.exceptions.Fatal;

public abstract class Operator extends AbstractValue {

	private static final long serialVersionUID = 0;
	
	/** Evaluate this Value.  For primitive types, return this. */
	public abstract Value evaluate(Resolver resolver, Walker arguments);
	
	/** Given an argument list, throw an exception if the list is null. */
	public void throwIfNullArguments(Walker arguments) {
		check(arguments != null, "No argument list was supplied");
	}

	/** Given a list, return an un-evaluated array of list items.
	 * 
	 * @param args - argument list
	 * @param count - number of arguments to obtain
	 * @return - Value[] of un-evaluated arguments.
	 */
	public Value[] toRawArgumentsArray(Walker args, int count) {
		throwIfNullArguments(args);
		Vector<Value> parms = new Vector<Value>();
		for (int c = 0; c < count; c++) {
			check(args.hasNext(), count + " arguments expected but only " + c + " were supplied");
			parms.add(args.next());
		}
		return parms.toArray(new Value[0]);
	}
	
	/** Given a list, return an evaluated array of arguments.
	 * 
	 * @param args - argument list
	 * @param count - number of arguments to obtain
	 * @return - Value[] of evaluated arguments.
	 */
	public Value[] toArgumentsArray(Resolver resolver, Walker args, int count) {
		throwIfNullArguments(args);
		Vector<Value> parms = new Vector<Value>();
		for (int c = 0; c < count; c++) {
			check(args.hasNext(), count + " arguments expected but only " + c + " were supplied");
			parms.add(args.next().evaluate(resolver));
		}
		return parms.toArray(new Value[0]);		
	}

	/** Get the next argument in an argument list.  If it is a Bunch, return it.
	 * If it is not a Bunch, evaluate it and examine the result.  If the result
	 * is a Bunch, return it.  Otherwise, throw an exception.
	 * 
	 * If there are no items in the list, throw an exception.
	 * 
	 * @param resolver - Resolver
	 * @param args - Walker, typically arguments
	 * @param argumentName - name or description of argument for error messages
	 * @return - Bunch
	 */
	public Bunch toBunch(Resolver resolver, Walker args, String argumentName) {
		Value argV = args.next();
		check(argV != null, "Expected " + argumentName + " is missing");
		if (argV instanceof Bunch)
			return (Bunch)argV;
		argV = argV.evaluate(resolver);
		check(argV instanceof Bunch, "Expected list for " + argumentName + " but got " + argV.getTypeName());
		return (Bunch)argV;
	}
	
	/** Evaluate body1 body2 ... bodyn
	 * 
	 * @param resolver - Resolver to use with this evaluateBody()
	 * @param args - body1 body2 ... bodyn
	 * @return result of bodyn
	 */
	public static Value evaluateBody(Resolver resolver, Walker args) {
		Value rValue = Nil.getInstance();
		while (args.hasNext())
			rValue = args.next().evaluate(resolver);
		return rValue;
	}
	
	/** If flag is false, throw an exception.
	 * 
	 * @param f - boolean flag
	 * @param msg - exception message
	 */
	public void check(boolean f, String msg) {
		if (!f)
			throw new Fatal(msg + " in " + getOperatorName());
	}	
	
	public String getTypeName() {
		return "Operator";
	}
	
	/** Obtain operator name. */
	public abstract String getOperatorName();
	
	public String toString() {
		return getOperatorName();
	}

}
