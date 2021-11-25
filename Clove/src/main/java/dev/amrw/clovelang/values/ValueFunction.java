package dev.amrw.clovelang.values;

import dev.amrw.clovelang.interpreter.FunctionDefinition;

/**
 * Stores a FunctionDefinition instance in a Value form.
 * 
 * @author amrwc
 */
public class ValueFunction extends ValueAbstract {
	private FunctionDefinition functionDefinition;

	public ValueFunction() {
	}

	public ValueFunction(FunctionDefinition fndef) {
		functionDefinition = fndef;
	}

	@Override
	public int compare(Value v) {
		return functionDefinition.compareTo(v);
	}

	@Override
	public String getName() {
		return "ValueFn";
	}

	@SuppressWarnings("unchecked")
	@Override
	public FunctionDefinition getRawValue() {
		return get();
	}

	public int getLevel() {
		return functionDefinition.getLevel();
	}

	public FunctionDefinition get() {
		return functionDefinition;
	}
}
