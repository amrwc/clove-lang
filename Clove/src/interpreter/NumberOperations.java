package interpreter;

import values.*;

/**
 * This class implements all supported operations on two numbers.
 * 
 * The doOperation() method is overloaded with the 4 supported primitive types
 * (int, long, float, and double), which are the left operands in an operation.
 * Inside of it, the right operand's Value-type is detected and the appropriate
 * cast is applied for each relevant type. The method returns the right
 * Value-type to the caller.
 * 
 * @author amrwc
 */
public class NumberOperations {
	/**
	 * Does an arithmetic operation chosen by the 'operation' parameter on a
	 * primitive and a Value, and returns the right Value.
	 * 
	 * @param {int}    v1
	 * @param {String} operation
	 * @param {Value}  v2
	 * @returns {Value} outcome in the right Value-type
	 */
	public static Value doOperation(int v1, String operation, Value v2) {
		switch (operation) {
		case "add":
			if (v2 instanceof ValueInteger)
				return NumberUtils.tryInt(v1 + (int) v2.getRawValue());
			if (v2 instanceof ValueLong)
				return NumberUtils.tryInt(v1 + (long) v2.getRawValue());
			if (v2 instanceof ValueFloat)
				return NumberUtils.tryInt(v1 + (float) v2.getRawValue());
			if (v2 instanceof ValueDouble)
				return NumberUtils.tryInt(v1 + (double) v2.getRawValue());
			if (v2 instanceof ValueReflection) {
				Value temp = null;
				try {
					temp = ValueReflection.getCorrespondingValue(v2.getRawValue());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return doOperation(v1, "add", temp);
			}
			break;

		case "subtract":
			if (v2 instanceof ValueInteger)
				return NumberUtils.tryInt(v1 - (int) v2.getRawValue());
			if (v2 instanceof ValueLong)
				return NumberUtils.tryInt(v1 - (long) v2.getRawValue());
			if (v2 instanceof ValueFloat)
				return NumberUtils.tryInt(v1 - (float) v2.getRawValue());
			if (v2 instanceof ValueDouble)
				return NumberUtils.tryInt(v1 - (double) v2.getRawValue());
			if (v2 instanceof ValueReflection) {
				Value temp = null;
				try {
					temp = ValueReflection.getCorrespondingValue(v2.getRawValue());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return doOperation(v1, "subtract", temp);
			}
			break;

		case "mult":
			if (v2 instanceof ValueInteger)
				return NumberUtils.tryInt(v1 * (int) v2.getRawValue());
			if (v2 instanceof ValueLong)
				return NumberUtils.tryInt(v1 * (long) v2.getRawValue());
			if (v2 instanceof ValueFloat)
				return NumberUtils.tryInt(v1 * (float) v2.getRawValue());
			if (v2 instanceof ValueDouble)
				return NumberUtils.tryInt(v1 * (double) v2.getRawValue());
			if (v2 instanceof ValueReflection) {
				Value temp = null;
				try {
					temp = ValueReflection.getCorrespondingValue(v2.getRawValue());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return doOperation(v1, "mult", temp);
			}
			break;

		case "div":
			if (v2 instanceof ValueInteger)
				return NumberUtils.tryInt((double) v1 / (int) v2.getRawValue());
			if (v2 instanceof ValueLong)
				return NumberUtils.tryInt((double) v1 / (long) v2.getRawValue());
			if (v2 instanceof ValueFloat)
				return NumberUtils.tryInt((double) v1 / (float) v2.getRawValue());
			if (v2 instanceof ValueDouble)
				return NumberUtils.tryInt((double) v1 / (double) v2.getRawValue());
			if (v2 instanceof ValueReflection) {
				Value temp = null;
				try {
					temp = ValueReflection.getCorrespondingValue(v2.getRawValue());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return doOperation(v1, "div", temp);
			}
			break;

		case "mod":
			if (v2 instanceof ValueInteger)
				return NumberUtils.tryInt(v1 % (int) v2.getRawValue());
			if (v2 instanceof ValueLong)
				return NumberUtils.tryInt(v1 % (long) v2.getRawValue());
			if (v2 instanceof ValueFloat)
				return NumberUtils.tryInt(v1 % (float) v2.getRawValue());
			if (v2 instanceof ValueDouble)
				return NumberUtils.tryInt(v1 % (double) v2.getRawValue());
			if (v2 instanceof ValueReflection) {
				Value temp = null;
				try {
					temp = ValueReflection.getCorrespondingValue(v2.getRawValue());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return doOperation(v1, "mod", temp);
			}
			break;
		}

		throw new ExceptionSemantic(
				"Couldn't do operation '" + operation + "' on " + v1 + " and " + v2);
	}

	/**
	 * Does an arithmetic operation chosen by the 'operation' parameter on a
	 * primitive and a Value, and returns the right Value.
	 * 
	 * @param {long}   v1
	 * @param {String} operation
	 * @param {Value}  v2
	 * @returns {Value} outcome in the right Value-type
	 */
	public static Value doOperation(long v1, String operation, Value v2) {
		switch (operation) {
		case "add":
			if (v2 instanceof ValueInteger)
				return NumberUtils.tryInt(v1 + (int) v2.getRawValue());
			if (v2 instanceof ValueLong)
				return NumberUtils.tryInt(v1 + (long) v2.getRawValue());
			if (v2 instanceof ValueFloat)
				return NumberUtils.tryInt(v1 + (float) v2.getRawValue());
			if (v2 instanceof ValueDouble)
				return NumberUtils.tryInt(v1 + (double) v2.getRawValue());
			if (v2 instanceof ValueReflection) {
				Value temp = null;
				try {
					temp = ValueReflection.getCorrespondingValue(v2.getRawValue());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return doOperation(v1, "add", temp);
			}
			break;

		case "subtract":
			if (v2 instanceof ValueInteger)
				return NumberUtils.tryInt(v1 - (int) v2.getRawValue());
			if (v2 instanceof ValueLong)
				return NumberUtils.tryInt(v1 - (long) v2.getRawValue());
			if (v2 instanceof ValueFloat)
				return NumberUtils.tryInt(v1 - (float) v2.getRawValue());
			if (v2 instanceof ValueDouble)
				return NumberUtils.tryInt(v1 - (double) v2.getRawValue());
			if (v2 instanceof ValueReflection) {
				Value temp = null;
				try {
					temp = ValueReflection.getCorrespondingValue(v2.getRawValue());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return doOperation(v1, "subtract", temp);
			}
			break;

		case "mult":
			if (v2 instanceof ValueInteger)
				return NumberUtils.tryInt(v1 * (int) v2.getRawValue());
			if (v2 instanceof ValueLong)
				return NumberUtils.tryInt(v1 * (long) v2.getRawValue());
			if (v2 instanceof ValueFloat)
				return NumberUtils.tryInt(v1 * (float) v2.getRawValue());
			if (v2 instanceof ValueDouble)
				return NumberUtils.tryInt(v1 * (double) v2.getRawValue());
			if (v2 instanceof ValueReflection) {
				Value temp = null;
				try {
					temp = ValueReflection.getCorrespondingValue(v2.getRawValue());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return doOperation(v1, "mult", temp);
			}
			break;

		case "div":
			if (v2 instanceof ValueInteger)
				return NumberUtils.tryInt((double) v1 / (int) v2.getRawValue());
			if (v2 instanceof ValueLong)
				return NumberUtils.tryInt((double) v1 / (long) v2.getRawValue());
			if (v2 instanceof ValueFloat)
				return NumberUtils.tryInt((double) v1 / (float) v2.getRawValue());
			if (v2 instanceof ValueDouble)
				return NumberUtils.tryInt((double) v1 / (double) v2.getRawValue());
			if (v2 instanceof ValueReflection) {
				Value temp = null;
				try {
					temp = ValueReflection.getCorrespondingValue(v2.getRawValue());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return doOperation(v1, "div", temp);
			}
			break;

		case "mod":
			if (v2 instanceof ValueInteger)
				return NumberUtils.tryInt(v1 % (int) v2.getRawValue());
			if (v2 instanceof ValueLong)
				return NumberUtils.tryInt(v1 % (long) v2.getRawValue());
			if (v2 instanceof ValueFloat)
				return NumberUtils.tryInt(v1 % (float) v2.getRawValue());
			if (v2 instanceof ValueDouble)
				return NumberUtils.tryInt(v1 % (double) v2.getRawValue());
			if (v2 instanceof ValueReflection) {
				Value temp = null;
				try {
					temp = ValueReflection.getCorrespondingValue(v2.getRawValue());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return doOperation(v1, "mod", temp);
			}
			break;
		}

		throw new ExceptionSemantic(
				"Couldn't do operation '" + operation + "' on " + v1 + " and " + v2);
	}

	/**
	 * Does an arithmetic operation chosen by the 'operation' parameter on a
	 * primitive and a Value, and returns the right Value.
	 * 
	 * @param {float}  v1
	 * @param {String} operation
	 * @param {Value}  v2
	 * @returns {Value} outcome in the right Value-type
	 */
	public static Value doOperation(float v1, String operation, Value v2) {
		switch (operation) {
		case "add":
			if (v2 instanceof ValueInteger)
				return NumberUtils.tryInt(v1 + (int) v2.getRawValue());
			if (v2 instanceof ValueLong)
				return NumberUtils.tryInt(v1 + (long) v2.getRawValue());
			if (v2 instanceof ValueFloat)
				return NumberUtils.tryInt(v1 + (float) v2.getRawValue());
			if (v2 instanceof ValueDouble)
				return NumberUtils.tryInt(v1 + (double) v2.getRawValue());
			if (v2 instanceof ValueReflection) {
				Value temp = null;
				try {
					temp = ValueReflection.getCorrespondingValue(v2.getRawValue());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return doOperation(v1, "add", temp);
			}
			break;

		case "subtract":
			if (v2 instanceof ValueInteger)
				return NumberUtils.tryInt(v1 - (int) v2.getRawValue());
			if (v2 instanceof ValueLong)
				return NumberUtils.tryInt(v1 - (long) v2.getRawValue());
			if (v2 instanceof ValueFloat)
				return NumberUtils.tryInt(v1 - (float) v2.getRawValue());
			if (v2 instanceof ValueDouble)
				return NumberUtils.tryInt(v1 - (double) v2.getRawValue());
			if (v2 instanceof ValueReflection) {
				Value temp = null;
				try {
					temp = ValueReflection.getCorrespondingValue(v2.getRawValue());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return doOperation(v1, "subtract", temp);
			}
			break;

		case "mult":
			if (v2 instanceof ValueInteger)
				return NumberUtils.tryInt(v1 * (int) v2.getRawValue());
			if (v2 instanceof ValueLong)
				return NumberUtils.tryInt(v1 * (long) v2.getRawValue());
			if (v2 instanceof ValueFloat)
				return NumberUtils.tryInt(v1 * (float) v2.getRawValue());
			if (v2 instanceof ValueDouble)
				return NumberUtils.tryInt(v1 * (double) v2.getRawValue());
			if (v2 instanceof ValueReflection) {
				Value temp = null;
				try {
					temp = ValueReflection.getCorrespondingValue(v2.getRawValue());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return doOperation(v1, "mult", temp);
			}
			break;

		case "div":
			if (v2 instanceof ValueInteger)
				return NumberUtils.tryInt(v1 / (int) v2.getRawValue());
			if (v2 instanceof ValueLong)
				return NumberUtils.tryInt(v1 / (long) v2.getRawValue());
			if (v2 instanceof ValueFloat)
				return NumberUtils.tryInt(v1 / (float) v2.getRawValue());
			if (v2 instanceof ValueDouble)
				return NumberUtils.tryInt(v1 / (double) v2.getRawValue());
			if (v2 instanceof ValueReflection) {
				Value temp = null;
				try {
					temp = ValueReflection.getCorrespondingValue(v2.getRawValue());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return doOperation(v1, "div", temp);
			}
			break;

		case "mod":
			if (v2 instanceof ValueInteger)
				return NumberUtils.tryInt(v1 % (int) v2.getRawValue());
			if (v2 instanceof ValueLong)
				return NumberUtils.tryInt(v1 % (long) v2.getRawValue());
			if (v2 instanceof ValueFloat)
				return NumberUtils.tryInt(v1 % (float) v2.getRawValue());
			if (v2 instanceof ValueDouble)
				return NumberUtils.tryInt(v1 % (double) v2.getRawValue());
			if (v2 instanceof ValueReflection) {
				Value temp = null;
				try {
					temp = ValueReflection.getCorrespondingValue(v2.getRawValue());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return doOperation(v1, "mod", temp);
			}
			break;
		}

		throw new ExceptionSemantic(
				"Couldn't do operation '" + operation + "' on " + v1 + " and " + v2);
	}

	/**
	 * Does an arithmetic operation chosen by the 'operation' parameter on a
	 * primitive and a Value, and returns the right Value.
	 * 
	 * @param {double} v1
	 * @param {String} operation
	 * @param {Value}  v2
	 * @returns {Value} outcome in the right Value-type
	 */
	public static Value doOperation(double v1, String operation, Value v2) {
		switch (operation) {
		case "add":
			if (v2 instanceof ValueInteger)
				return NumberUtils.tryInt(v1 + (int) v2.getRawValue());
			if (v2 instanceof ValueLong)
				return NumberUtils.tryInt(v1 + (long) v2.getRawValue());
			if (v2 instanceof ValueFloat)
				return NumberUtils.tryInt(v1 + (float) v2.getRawValue());
			if (v2 instanceof ValueDouble)
				return NumberUtils.tryInt(v1 + (double) v2.getRawValue());
			if (v2 instanceof ValueReflection) {
				Value temp = null;
				try {
					temp = ValueReflection.getCorrespondingValue(v2.getRawValue());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return doOperation(v1, "add", temp);
			}
			break;

		case "subtract":
			if (v2 instanceof ValueInteger)
				return NumberUtils.tryInt(v1 - (int) v2.getRawValue());
			if (v2 instanceof ValueLong)
				return NumberUtils.tryInt(v1 - (long) v2.getRawValue());
			if (v2 instanceof ValueFloat)
				return NumberUtils.tryInt(v1 - (float) v2.getRawValue());
			if (v2 instanceof ValueDouble)
				return NumberUtils.tryInt(v1 - (double) v2.getRawValue());
			if (v2 instanceof ValueReflection) {
				Value temp = null;
				try {
					temp = ValueReflection.getCorrespondingValue(v2.getRawValue());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return doOperation(v1, "subtract", temp);
			}
			break;

		case "mult":
			if (v2 instanceof ValueInteger)
				return NumberUtils.tryInt(v1 * (int) v2.getRawValue());
			if (v2 instanceof ValueLong)
				return NumberUtils.tryInt(v1 * (long) v2.getRawValue());
			if (v2 instanceof ValueFloat)
				return NumberUtils.tryInt(v1 * (float) v2.getRawValue());
			if (v2 instanceof ValueDouble)
				return NumberUtils.tryInt(v1 * (double) v2.getRawValue());
			if (v2 instanceof ValueReflection) {
				Value temp = null;
				try {
					temp = ValueReflection.getCorrespondingValue(v2.getRawValue());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return doOperation(v1, "mult", temp);
			}
			break;

		case "div":
			if (v2 instanceof ValueInteger)
				return NumberUtils.tryInt(v1 / (int) v2.getRawValue());
			if (v2 instanceof ValueLong)
				return NumberUtils.tryInt(v1 / (long) v2.getRawValue());
			if (v2 instanceof ValueFloat)
				return NumberUtils.tryInt(v1 / (float) v2.getRawValue());
			if (v2 instanceof ValueDouble)
				return NumberUtils.tryInt(v1 / (double) v2.getRawValue());
			if (v2 instanceof ValueReflection) {
				Value temp = null;
				try {
					temp = ValueReflection.getCorrespondingValue(v2.getRawValue());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return doOperation(v1, "div", temp);
			}
			break;

		case "mod":
			if (v2 instanceof ValueInteger)
				return NumberUtils.tryInt(v1 % (int) v2.getRawValue());
			if (v2 instanceof ValueLong)
				return NumberUtils.tryInt(v1 % (long) v2.getRawValue());
			if (v2 instanceof ValueFloat)
				return NumberUtils.tryInt(v1 % (float) v2.getRawValue());
			if (v2 instanceof ValueDouble)
				return NumberUtils.tryInt(v1 % (double) v2.getRawValue());
			if (v2 instanceof ValueReflection) {
				Value temp = null;
				try {
					temp = ValueReflection.getCorrespondingValue(v2.getRawValue());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return doOperation(v1, "mod", temp);
			}
			break;
		}

		throw new ExceptionSemantic(
				"Couldn't do operation '" + operation + "' on " + v1 + " and " + v2);
	}
}
