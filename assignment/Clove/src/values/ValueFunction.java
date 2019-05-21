package values;

import interpreter.FunctionDefinition;

/**
 * Stores a FunctionDefinition instance in a Value form.
 * 
 * @author amrwc
 */
public class ValueFunction extends ValueAbstract {
	private FunctionDefinition functionDefinition;

	public ValueFunction() {}

	public ValueFunction(FunctionDefinition fndef) {
		functionDefinition = fndef;
	}

	public int compare(Value v) {
		return functionDefinition.compareTo(v);
	}

	public String getName() {
		return "ValueFn";
	}

	public int getLevel() {
		return functionDefinition.getLevel();
	}

	public FunctionDefinition get() {
		return functionDefinition;
	}
}
