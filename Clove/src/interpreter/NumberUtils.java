package interpreter;

import values.*;

/**
 * Utilities for operations and conversions on number-types.
 * 
 * @author amrwc
 */
public class NumberUtils {
	/**
	 * Checks whether the argument is of any number type.
	 * 
	 * @param {Object} perhapsNumber
	 * @returns {boolean}
	 */
	public static boolean isNumber(Object perhapsNumber) {
		if (perhapsNumber instanceof Short)
			return true;
		if (perhapsNumber instanceof Integer)
			return true;
		if (perhapsNumber instanceof Long)
			return true;
		if (perhapsNumber instanceof Float)
			return true;
		if (perhapsNumber instanceof Double)
			return true;

		return false;
	}

	public static Class<?> getPrimitiveNumberClass(Object n) {
		Class<?> perhapsPrimitive = null;
		try {
			perhapsPrimitive = Class.forName(n.getClass().getName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		if (Byte.class == perhapsPrimitive)
			return byte.class;
		if (Short.class == perhapsPrimitive)
			return short.class;
		if (Integer.class == perhapsPrimitive)
			return int.class;
		if (Long.class == perhapsPrimitive)
			return long.class;
		if (Float.class == perhapsPrimitive)
			return float.class;
		if (Double.class == perhapsPrimitive)
			return double.class;

		throw new ExceptionSemantic(
				"Couldn't get the primitive number class of " + n + ".");
	}

	/**
	 * Tries to parse a double value to a ValueInteger. If it's impossible, returns
	 * a ValueFloat or ValueDouble.
	 * 
	 * @read https://stackoverflow.com/a/9898528/10620237
	 * @returns {ValueInteger/ValueFloat/ValueDouble} cast attempt result
	 * @author amrwc
	 */
	public static Value tryInt(double v) {
		if ((v == Math.floor(v)) && !Double.isInfinite(v))
			return new ValueInteger((int) Math.floor(v));
		else
			return new ValueDouble(v);
	}

	public static Value tryInt(float v) {
		if ((v == Math.floor(v)) && !Float.isInfinite(v))
			return new ValueInteger((int) Math.floor(v));
		else
			return new ValueFloat(v);
	}

	/**
	 * Tries to parse a value-string to a ValueFloat. If it's impossible, returns a
	 * ValueDouble.
	 * 
	 * @returns {ValueFloat/ValueDouble} cast attempt result
	 */
	public static Value tryFloat(String v) {
		final float vFloat = Float.parseFloat(v);
		final double vDouble = Double.parseDouble(v);

		// If parsed float's string value is equal to double's,
		// it's safe to return a float without precision loss.
		return (("" + vFloat).equals("" + vDouble)) ? new ValueFloat(vFloat)
				: new ValueDouble(vDouble);
	}
}
