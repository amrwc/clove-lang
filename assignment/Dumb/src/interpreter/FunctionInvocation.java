package interpreter;

import java.util.Vector;

import values.Value;

/** Function invocation context. */
class FunctionInvocation {

	private FunctionDefinition function;
	private int argumentCount = 0;
	private Vector<Value> slots;
	
	private final void setSlot(int n, Value v) {
		if (n >= slots.size())
			slots.setSize(n + 1);
		slots.set(n, v);
	}
	
	/** Ctor for user-defined function. */
	FunctionInvocation(FunctionDefinition fndef) {
		function = fndef;
		slots = new Vector<Value>(function.getLocalCount());
	}
	
	/** Get the level of the associated function. */
	int getLevel() {
		return function.getLevel();
	}
	
	/** Set an argument value. */
	void setArgument(Value v) {
		if (argumentCount >= function.getParameterCount())
			throw new ExceptionSemantic("Function " + function.getSignature() + " expected " + function.getParameterCount() + " arguments but got " + (argumentCount + 1) + ".");
		// First slots are always arguments
		setSlot(argumentCount++, v);
	}
	
	/** Check argument count. */
	void checkArgumentCount() {
		if (argumentCount < function.getParameterCount())
			throw new ExceptionSemantic("Function " + function.getSignature() + " expected " + function.getParameterCount() + " arguments but got " + (argumentCount + 1) + ".");		
	}
	
	/** Execute this invocation. */
	Value execute(Parser parser) {
		parser.doChildren(function.getFunctionBody(), null);
		if (function.hasReturn())
			return parser.doChild(function.getFunctionReturnExpression(), 0);
		return null;
	}

	/** Get the slot number of a given variable or parameter name.  Return -1 if not found. */
	int findSlotNumber(String name) {
		return function.getLocalSlotNumber(name);
	}
	
	/** Get a variable or parameter value given a slot number. */
	Value getValue(int slotNumber) {
		return slots.get(slotNumber);
	}

	/** Given a slot number, set its value. */
	void setValue(int slotNumber, Value value) {
		setSlot(slotNumber, value);
	}

	/** Define a variable in the function definition.  Return its slot number. */
	int defineVariable(String name) {
		return function.defineVariable(name);
	}
	
	/** Add a function definition. */
	void addFunction(FunctionDefinition definition) {
		function.addFunction(definition);
	}

	/** Find a function definition.  Return null if it doesn't exist. */
	FunctionDefinition findFunction(String name) {
		return function.findFunction(name);
	}

	/**
	 * Remove previously defined variable.
	 * 
	 * NOTE: It should only be used for scopes that are not functions,
	 * 		 i.e. loops, if-statements.
	 * 
	 * @param name
	 * @author amrwc
	 */
	void removeVariable(String name) {
		function.removeVariable(name);
	}

	/**
	 * Remove previously defined function.
	 * 
	 * NOTE: It should only be used for scopes that are not functions,
	 * 		 i.e. loops, if-statements.
	 * 
	 * @param name
	 * @author amrwc
	 */
	void removeFunction(String fnName) {
		function.removeFunction(fnName);
	}
}
