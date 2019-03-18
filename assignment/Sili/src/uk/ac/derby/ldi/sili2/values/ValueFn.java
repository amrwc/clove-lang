package uk.ac.derby.ldi.sili2.values;

import java.util.HashMap;
import java.util.Vector;

import uk.ac.derby.ldi.sili2.interpreter.ExceptionSemantic;
import uk.ac.derby.ldi.sili2.parser.ast.SimpleNode;

public class ValueFn extends ValueAbstract {
//	private static final long serialVersionUID = 0;

	private String name;
	private String parmSignature = "";
	private Vector<String> parameters = new Vector<String>();
	private HashMap<String, Integer> slots = new HashMap<String, Integer>();
	private HashMap<String, ValueFn> functions = new HashMap<String, ValueFn>();
	private SimpleNode ASTFunctionBody = null;
	private SimpleNode ASTFunctionReturnExpression = null;
	private int depth;
	
	/** Ctor for function definition. */
	ValueFn(String functionName, int level) {
		name = functionName;
		depth = level;
	}
	
	public ValueFn(String tokenValue) {
		// TODO Auto-generated constructor stub
		//TODO: Parser.java line 366????
	}

	/** Get the depth of this definition.
	 * 0 - root or main scope
	 * 1 - definition inside root or main scope
	 * 2 - definition inside 1
	 * n - etc.
	 */
	int getLevel() {
		return depth;
	}
	
	/** Get the name of this function. */
	public String getName() {
		return name;
	}
	
	public int compare(Value v) {
		// Auto-generated method stub
		return 0;
	}

	/** Set the function body of this function. */
	void setFunctionBody(SimpleNode node) {
		ASTFunctionBody = node;
	}
	
	/** Get the function body of this function. */
	SimpleNode getFunctionBody() {
		return ASTFunctionBody;
	}
	
	/** Set the return expression of this function. */
	void setFunctionReturnExpression(SimpleNode node) {
		ASTFunctionReturnExpression = node;
	}
	
	/** Get the return expression of this function. */
	SimpleNode getFunctionReturnExpression() {
		return ASTFunctionReturnExpression;
	}
	
	/** Get the signature of this function. */
	String getSignature() {
		return (hasReturn() ? "value " : "") + getName() + "(" + parmSignature + ")";
	}
	
	/** True if this function has a return value. */
	boolean hasReturn() {
		return (ASTFunctionReturnExpression != null);
	}
	
	/** Comparison operator.  Functions of the same name are the same. */
	public int compareTo(Object o) {
		return name.compareTo(((ValueFn)o).name);
	}
	
	/** Get count of parameters. */
	int getParameterCount() {
		return parameters.size();
	}
	
	/** Get the name of the ith parameter. */
	String getParameterName(int i) {
		return parameters.get(i);
	}
	
	/** Define a parameter. */
	void defineParameter(String name) {
		if (parameters.contains(name))
			throw new ExceptionSemantic("Parameter " + name + " already exists in function " + getName());
		parameters.add(name);
		parmSignature += ((parmSignature.length()==0) ? name : (", " + name));
		defineVariable(name);
	}
	
	/** Get count of local variables and parameters. */
	int getLocalCount() {
		return slots.size();
	}
	
	/** Get the storage slot number of a given variable or parm.  Return -1 if it doesn't exist. */
	int getLocalSlotNumber(String name) {
		Integer slot = slots.get(name);
		if (slot == null)
			return -1;
		return slot.intValue();
	}
	
	/** Define a variable.  Return its slot number. */
	int defineVariable(String name) {
		Integer slot = slots.get(name);
		if (slot != null)
			return slot.intValue();
		int slotNumber = slots.size();
		slots.put(name, Integer.valueOf(slotNumber));
		return slotNumber;
	}	
	
	/** Add an inner function definition. */
	void addFunction(ValueFn definition) {
		functions.put(definition.getName(), definition);
	}
	
	/** Find an inner function definition.  Return null if it doesn't exist. */
	ValueFn findFunction(String name) {
		return functions.get(name);
	}
}
