package values;

import interpreter.FunctionDefinition;

public class ValueFn extends ValueAbstract {
	private FunctionDefinition functionDefinition;

	public ValueFn() {}

	public ValueFn(FunctionDefinition fndef) {
		functionDefinition = fndef;
	}

	@Override
	public String getName() {
		return "ValueFn";
	}

	public int getLevel() {
		return functionDefinition.getLevel();
	}

	public FunctionDefinition get() {
		return functionDefinition;
	}

	@Override
	public int compare(Value v) {
		// TODO Auto-generated method stub
		return 0;
	}
}
