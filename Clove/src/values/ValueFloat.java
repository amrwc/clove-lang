package values;

import interpreter.ExceptionSemantic;
import interpreter.NumberUtils;

public class ValueFloat extends ValueAbstract {
	private final float internalValue;

	public ValueFloat(float v) {
		internalValue = v;
	}

	@Override
	public String getName() {
		return "float";
	}

	@SuppressWarnings("unchecked")
	@Override
	public Float getRawValue() {
		return internalValue;
	}

	/** Convert this to a primitive double. */
	@Override
	public double doubleValue() {
		return internalValue;
	}

	/** Convert this to a primitive String. */
	@Override
	public String stringValue() {
		return "" + internalValue;
	}

	@Override
	public int compare(Value v) {
		if (internalValue == v.doubleValue())
			return 0;
		else if (internalValue > v.doubleValue())
			return 1;
		else
			return -1;
	}

	@Override
	public Value add(Value v) {
//		return NumberUtils.tryInt(internalValue + v.doubleValue());
		return doOperation("add", v);
	}

	@Override
	public Value subtract(Value v) {
//		return NumberUtils.tryInt(internalValue - v.doubleValue());
		return doOperation("subtract", v);
	}

	@Override
	public Value mult(Value v) {
//		return NumberUtils.tryInt(internalValue * v.doubleValue());
		return doOperation("mult", v);
	}

	@Override
	public Value div(Value v) {
//		return NumberUtils.tryInt(internalValue / v.doubleValue());
		return doOperation("div", v);
	}

	@Override
	public Value mod(Value v) {
//		return NumberUtils.tryInt(internalValue % v.doubleValue());
		return doOperation("mod", v);
	}

	private Value doOperation(String operation, Value v) {
		switch (operation) {
		case "add":
			if (v instanceof ValueInteger)
				return NumberUtils.tryInt(internalValue + (int) v.getRawValue());
			if (v instanceof ValueLong)
				return NumberUtils.tryInt(internalValue + (long) v.getRawValue());
			if (v instanceof ValueFloat)
				return NumberUtils.tryInt(internalValue + (float) v.getRawValue());
			if (v instanceof ValueDouble)
				return NumberUtils.tryInt(internalValue + (double) v.getRawValue());
			if (v instanceof ValueReflection) {
				Value temp = null;
				try {
					temp = ValueReflection.getCorrespondingValue(v.getRawValue());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return unary_plus().add(temp);
			}
			break;

		case "subtract":
			if (v instanceof ValueInteger)
				return NumberUtils.tryInt(internalValue - (int) v.getRawValue());
			if (v instanceof ValueLong)
				return NumberUtils.tryInt(internalValue - (long) v.getRawValue());
			if (v instanceof ValueFloat)
				return NumberUtils.tryInt(internalValue - (float) v.getRawValue());
			if (v instanceof ValueDouble)
				return NumberUtils.tryInt(internalValue - (double) v.getRawValue());
			if (v instanceof ValueReflection) {
				Value temp = null;
				try {
					temp = ValueReflection.getCorrespondingValue(v.getRawValue());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return unary_plus().subtract(temp);
			}
			break;

		case "mult":
			if (v instanceof ValueInteger)
				return NumberUtils.tryInt(internalValue * (int) v.getRawValue());
			if (v instanceof ValueLong)
				return NumberUtils.tryInt(internalValue * (long) v.getRawValue());
			if (v instanceof ValueFloat)
				return NumberUtils.tryInt(internalValue * (float) v.getRawValue());
			if (v instanceof ValueDouble)
				return NumberUtils.tryInt(internalValue * (double) v.getRawValue());
			if (v instanceof ValueReflection) {
				Value temp = null;
				try {
					temp = ValueReflection.getCorrespondingValue(v.getRawValue());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return unary_plus().mult(temp);
			}
			break;

		case "div":
			if (v instanceof ValueInteger)
				return NumberUtils.tryInt(internalValue / (int) v.getRawValue());
			if (v instanceof ValueLong)
				return NumberUtils.tryInt(internalValue / (long) v.getRawValue());
			if (v instanceof ValueFloat)
				return NumberUtils.tryInt(internalValue / (float) v.getRawValue());
			if (v instanceof ValueDouble)
				return NumberUtils.tryInt(internalValue / (double) v.getRawValue());
			if (v instanceof ValueReflection) {
				Value temp = null;
				try {
					temp = ValueReflection.getCorrespondingValue(v.getRawValue());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return unary_plus().div(temp);
			}
			break;

		case "mod":
			if (v instanceof ValueInteger)
				return NumberUtils.tryInt(internalValue % (int) v.getRawValue());
			if (v instanceof ValueLong)
				return NumberUtils.tryInt(internalValue % (long) v.getRawValue());
			if (v instanceof ValueFloat)
				return NumberUtils.tryInt(internalValue % (float) v.getRawValue());
			if (v instanceof ValueDouble)
				return NumberUtils.tryInt(internalValue % (double) v.getRawValue());
			if (v instanceof ValueReflection) {
				Value temp = null;
				try {
					temp = ValueReflection.getCorrespondingValue(v.getRawValue());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				return unary_plus().mod(temp);
			}
			break;
		}

		throw new ExceptionSemantic("Couldn't do operation '" + operation + "' on "
				+ internalValue + " and " + v);
	}

	@Override
	public Value unary_plus() {
		return new ValueFloat(internalValue);
	}

	@Override
	public Value unary_minus() {
		return new ValueFloat(-internalValue);
	}

	@Override
	public String toString() {
		return stringValue();
	}
}
