package dev.amrw.clovelang.interpreter;

import dev.amrw.clovelang.values.Value;

/**
 * This exception is thrown when semantic errors are encountered.
 * 
 * @author dave
 */
public class ExceptionSemantic extends Error {
	static final long serialVersionUID = 0;

	public ExceptionSemantic(String message) {
		super(message);
	}

	/**
	 * Throws an ExceptionSemantic regarding a binary operation.
	 * 
	 * @param {Value}  v1
	 * @param {String} operation
	 * @param {Value}  v2
	 * @author amrwc
	 */
	public static ExceptionSemantic binaryOperationError(Value v1, String operation,
			Value v2) {
		return new ExceptionSemantic("Cannot perform '" + operation + "' on '"
				+ v1.getName() + " (" + v1.getRawValue() + ")' and '" + v2.getName()
				+ " (" + v2.getRawValue() + ")'.");
	}

	/**
	 * Throws an ExceptionSemantic regarding an unary operation.
	 * 
	 * @param {Value}  v
	 * @param {String} operation
	 * @author amrwc
	 */
	public static ExceptionSemantic unaryOperationError(Value v, String operation) {
		return new ExceptionSemantic("Cannot perform '" + operation + "' on '"
				+ v.getName() + " (" + v.getRawValue() + ")'.");
	}
}
