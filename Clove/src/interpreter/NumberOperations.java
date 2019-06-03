package interpreter;

import values.*;

/**
 * This class implements all supported operations on two numbers.
 * 
 * The doOperation() method is overloaded with the 4 supported primitive types
 * (int, long, float, and double), which are the left operands in an operation.
 * Right value is turned into a Double and used in the operation. The tryInt
 * utility method returns the correct Value-type to the caller.
 * 
 * @author amrwc
 */
public class NumberOperations {
	/**
	 * Does an arithmetic operation chosen by the 'operation' parameter on a
	 * primitive and a Value, and returns the correct Value.
	 * 
	 * @param {int}    v1
	 * @param {String} operation
	 * @param {Value}  v2
	 * @returns {Value} outcome in the correct Value-type
	 */
	public static Value doOperation(int v1, String operation, Value v2) {
		if (!NumberUtils.isNumber(v2.getRawValue()))
			throw new ExceptionSemantic("Cannot do operation '" + operation + "' on " + v1
					+ " and '" + v2.getName() + "' (" + v2.getRawValue() + ").");

		final double v2Double = Double.parseDouble(v2.stringValue());

		switch (operation) {
		case "add":
			return NumberUtils.tryInt(v1 + v2Double);

		case "subtract":
			return NumberUtils.tryInt(v1 - v2Double);

		case "mult":
			return NumberUtils.tryInt(v1 * v2Double);

		case "div":
			return NumberUtils.tryInt(v1 / v2Double);

		case "mod":
			return NumberUtils.tryInt(v1 % v2Double);
		}

		throw new ExceptionSemantic(
				"Cannot do operation '" + operation + "' on " + v1 + " and " + v2);
	}

	/**
	 * Does an arithmetic operation chosen by the 'operation' parameter on a
	 * primitive and a Value, and returns the correct Value.
	 * 
	 * @param {long}   v1
	 * @param {String} operation
	 * @param {Value}  v2
	 * @returns {Value} outcome in the correct Value-type
	 */
	public static Value doOperation(long v1, String operation, Value v2) {
		if (!NumberUtils.isNumber(v2.getRawValue()))
			throw new ExceptionSemantic("Cannot do operation '" + operation + "' on " + v1
					+ " and '" + v2.getName() + "' (" + v2.getRawValue() + ").");

		final double v2Double = Double.parseDouble(v2.stringValue());

		switch (operation) {
		case "add":
			return NumberUtils.tryInt(v1 + v2Double);

		case "subtract":
			return NumberUtils.tryInt(v1 - v2Double);

		case "mult":
			return NumberUtils.tryInt(v1 * v2Double);

		case "div":
			return NumberUtils.tryInt(v1 / v2Double);

		case "mod":
			return NumberUtils.tryInt(v1 % v2Double);
		}

		throw new ExceptionSemantic(
				"Cannot do operation '" + operation + "' on " + v1 + " and " + v2);
	}

	/**
	 * Does an arithmetic operation chosen by the 'operation' parameter on a
	 * primitive and a Value, and returns the correct Value.
	 * 
	 * @param {float}  v1
	 * @param {String} operation
	 * @param {Value}  v2
	 * @returns {Value} outcome in the correct Value-type
	 */
	public static Value doOperation(float v1, String operation, Value v2) {
		if (!NumberUtils.isNumber(v2.getRawValue()))
			throw new ExceptionSemantic("Cannot do operation '" + operation + "' on " + v1
					+ " and '" + v2.getName() + "' (" + v2.getRawValue() + ").");

		final double v2Double = Double.parseDouble(v2.stringValue());

		switch (operation) {
		case "add":
			return NumberUtils.tryInt(v1 + v2Double);

		case "subtract":
			return NumberUtils.tryInt(v1 - v2Double);

		case "mult":
			return NumberUtils.tryInt(v1 * v2Double);

		case "div":
			return NumberUtils.tryInt(v1 / v2Double);

		case "mod":
			return NumberUtils.tryInt(v1 % v2Double);
		}

		throw new ExceptionSemantic(
				"Cannot do operation '" + operation + "' on " + v1 + " and " + v2);
	}

	/**
	 * Does an arithmetic operation chosen by the 'operation' parameter on a
	 * primitive and a Value, and returns the correct Value.
	 * 
	 * @param {double} v1
	 * @param {String} operation
	 * @param {Value}  v2
	 * @returns {Value} outcome in the correct Value-type
	 */
	public static Value doOperation(double v1, String operation, Value v2) {
		if (!NumberUtils.isNumber(v2.getRawValue()))
			throw new ExceptionSemantic("Cannot do operation '" + operation + "' on " + v1
					+ " and '" + v2.getName() + "' (" + v2.getRawValue() + ").");

		final double v2Double = Double.parseDouble(v2.stringValue());

		switch (operation) {
		case "add":
			return NumberUtils.tryInt(v1 + v2Double);

		case "subtract":
			return NumberUtils.tryInt(v1 - v2Double);

		case "mult":
			return NumberUtils.tryInt(v1 * v2Double);

		case "div":
			return NumberUtils.tryInt(v1 / v2Double);

		case "mod":
			return NumberUtils.tryInt(v1 % v2Double);
		}

		throw new ExceptionSemantic(
				"Cannot do operation '" + operation + "' on " + v1 + " and " + v2);
	}
}
