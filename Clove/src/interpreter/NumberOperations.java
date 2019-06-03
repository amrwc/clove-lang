package interpreter;

import values.*;

/**
 * This class implements all supported operations on two numbers.
 * 
 * Both values are turned into Double type and used in the operation. The tryInt
 * utility method returns the correct Value-type to the caller.
 * 
 * @author amrwc
 */
public class NumberOperations {
	/**
	 * Does an arithmetic operation chosen by the 'operation' parameter on two
	 * Values, and returns the correct Value.
	 * 
	 * @param {Value}  v1
	 * @param {String} operation
	 * @param {Value}  v2
	 * @returns {Value} outcome in the correct Value-type
	 */
	public static Value doOperation(Value v1, String operation, Value v2) {
		if (!NumberUtils.isNumber(v2.getRawValue()))
			throw new ExceptionSemantic(
					"Cannot do operation '" + operation + "' on '" + v1.getName() + " ("
							+ v1 + ")' and '" + v2.getName() + " (" + v2 + ")'.");

		switch (operation) {
		case "add":
			return NumberUtils.tryInt(v1.doubleValue() + v2.doubleValue());

		case "subtract":
			return NumberUtils.tryInt(v1.doubleValue() - v2.doubleValue());

		case "mult":
			return NumberUtils.tryInt(v1.doubleValue() * v2.doubleValue());

		case "div":
			return NumberUtils.tryInt(v1.doubleValue() / v2.doubleValue());

		case "mod":
			return NumberUtils.tryInt(v1.doubleValue() % v2.doubleValue());

		default:
			throw new ExceptionSemantic(
					"Cannot do operation '" + operation + "' on '" + v1.getName() + " ("
							+ v1 + ")' and '" + v2.getName() + " (" + v2 + ")'.");
		}
	}
}
