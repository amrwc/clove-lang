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
		if (perhapsNumber instanceof Number)
			return true;

		if (perhapsNumber instanceof ValueReflection)
			return isNumber(((ValueReflection) perhapsNumber).getRawValue());

		return false;
	}

	/**
	 * Compares the argument with number-classes and returns its primitive.
	 * 
	 * @param {Object} n
	 * @returns {Class<?>} primitive type
	 */
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
	 * a ValueLong or ValueDouble.
	 * 
	 * @read https://stackoverflow.com/a/9898528/10620237
	 * @param {double} v
	 * @returns {ValueInteger/ValueLong/ValueDouble} parsed Value-type
	 */
	public static Value tryInt(double v) {
		final double floored = Math.floor(v);

		if ((v == floored) && !Double.isInfinite(v))
			return tryInt((long) floored);
		else
			return new ValueDouble(v);
	}

	/**
	 * Tries to parse a float value to a ValueInteger. If it's impossible, returns a
	 * ValueFloat.
	 * 
	 * @read https://stackoverflow.com/a/9898528/10620237
	 * @param {float} v
	 * @returns {ValueInteger/ValueFloat} parsed Value-type
	 */
	public static Value tryInt(float v) {
		final double floored = Math.floor(v);

		if ((v == floored) && !Float.isInfinite(v))
			return new ValueInteger((int) floored);
		else
			return new ValueFloat(v);
	}

	/**
	 * Compares long-value and down-casted int-value and if they're equal, returns a
	 * ValueInteger.
	 * 
	 * @param {long} v
	 * @returns {ValueInteger/ValueLong} parsed Value-type
	 */
	public static Value tryInt(long v) {
		if ((int) v == v)
			return new ValueInteger((int) v);
		else
			return new ValueLong(v);
	}

	/**
	 * Returns a ValueInteger.
	 * 
	 * @param {int} v
	 * @returns {ValueInteger}
	 */
	public static Value tryInt(int v) {
		return new ValueInteger(v);
	}

	/**
	 * Tries to parse a value-string to a ValueInteger. If it's impossible, returns
	 * a ValueLong.
	 * 
	 * Used by ASTInteger to parse a string to Value-type.
	 * 
	 * @param {String} v
	 * @returns {ValueInteger/ValueLong} parsed Value-type
	 */
	public static Value tryInt(String s) {
		try {
			return new ValueInteger(Integer.parseInt(s));
		} catch (final NumberFormatException e) {
			return new ValueLong(Long.parseLong(s));
		}
	}

	/**
	 * Tries to parse a value-string to a ValueFloat. If it's impossible, returns a
	 * ValueDouble.
	 * 
	 * Used by ASTRational to parse a string to Value-type.
	 * 
	 * @param {String} v
	 * @returns {ValueFloat/ValueDouble} parsed Value-type
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
