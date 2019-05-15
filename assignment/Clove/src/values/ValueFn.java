package values;

import interpreter.FunctionDefinition;

/**
 * Stores a FunctionDefinition instance in a Value form.
 * 
 * @author amrwc
 */
public class ValueFn extends ValueAbstract {
	private FunctionDefinition functionDefinition;

	public ValueFn() {}

	public ValueFn(FunctionDefinition fndef) {
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
