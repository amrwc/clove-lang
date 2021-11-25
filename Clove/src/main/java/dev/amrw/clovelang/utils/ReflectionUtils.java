package dev.amrw.clovelang.utils;

import java.util.HashMap;

import dev.amrw.clovelang.interpreter.ExceptionSemantic;
import dev.amrw.clovelang.interpreter.Parser;
import dev.amrw.clovelang.parser.ast.SimpleNode;
import dev.amrw.clovelang.values.Value;
import dev.amrw.clovelang.values.ValueList;
import dev.amrw.clovelang.values.ValueReflection;

public class ReflectionUtils {
	/**
	 * Instantiates the class with an empty constructor.
	 */
	public static void instantiateEmpty(ValueReflection r) {
		try {
			// Get an empty constructor.
			r.setCtor(r.getTheClass().getConstructor());

			// Instantiate the class with the empty constructor.
			r.setInternalValue(r.getCtor().newInstance());
		} catch (Exception e) {
			e.printStackTrace();
			throw new ExceptionSemantic("");
		}
	}

	/**
	 * Passes the constructor arguments into a Value array and calls the right
	 * method to instantiate the object.
	 * 
	 * @param {ValueList} ctorArgsValue -- ValueList of arguments passed into
	 *                    instantiate() prototype function
	 */
	public static void instantiateWithArguments(ValueReflection r,
			ValueList ctorArgsValue) {
		final Value[] ctorArgs = new Value[ctorArgsValue.size()];
		for (int i = 0; i < ctorArgsValue.size(); i++)
			ctorArgs[i] = ctorArgsValue.get(i);

		instantiateWithArguments(r, ctorArgs);
	}

	/**
	 * Instantiates the object with the arguments passed in as a Value array.
	 * 
	 * @param {Value[]} ctorArgs
	 */
	public static void instantiateWithArguments(ValueReflection r, Value[] ctorArgs) {
		// Store the parameters types to choose the right ctor,
		// and the arguments in their raw form.
		final HashMap<String, Object[]> args = parseCtorArgs(ctorArgs);

		try {
			// Get constructor matching the parameter classes.
			r.setCtor(
					r.getTheClass().getConstructor((Class<?>[]) args.get("paramTypes")));

			// Create an instance of the class using the arguments
			// and their matching constructor.
			r.setInternalValue(r.getCtor().newInstance(args.get("args")));
		} catch (final Exception e) {
			e.printStackTrace();
			throw new ExceptionSemantic("");
		}
	}

	/**
	 * Parses constructor arguments and returns them in the correct form.
	 * 
	 * @param {Value[]} ctorArgs
	 * @returns {HashMap<String, Object[]>} map containing the parameter types and
	 *          the arguments in their 'raw type'
	 */
	private static HashMap<String, Object[]> parseCtorArgs(Value[] ctorArgs) {
		final HashMap<String, Object[]> result = new HashMap<String, Object[]>();
		final Class<?>[] paramTypes = new Class[ctorArgs.length];
		final Object[] args = new Object[ctorArgs.length];

		for (int i = 0; i < ctorArgs.length; i++) {
			paramTypes[i] = (ctorArgs[i] instanceof ValueReflection)
					? ((ValueReflection) ctorArgs[i]).getTheClass()
					: parsePrimitive(ctorArgs[i].getRawValue());
			args[i] = ctorArgs[i].getRawValue();
		}

		result.put("paramTypes", paramTypes);
		result.put("args", args);

		return result;
	}

	/**
	 * Adjusts class to match Method.getMethod() arguments.
	 * 
	 * @read https://stackoverflow.com/a/13943623/10620237
	 * @param {Object} arg -- source argument whose class is to be returned
	 * @returns {Class<?>} adjusted class that will match Method.getMethod()
	 * @throws ClassNotFoundException
	 */
	private static Class<?> parsePrimitive(Object arg) {
		Class<?> perhapsPrimitive = null;

		try {
			perhapsPrimitive = Class.forName(arg.getClass().getName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new ExceptionSemantic("");
		}

		if (Boolean.class == perhapsPrimitive)
			return boolean.class;
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

		return perhapsPrimitive;
	}

	/**
	 * Parses Method arguments and returns them in the correct form.
	 * 
	 * @param {SimpleNode (ASTArgumentList)} argsNode -- the Method's arguments
	 * @param {Parser}    p -- active Parser's instance
	 * @returns {HashMap<String, Object[]>} map containing the parameter types and
	 *          the arguments in their 'raw type'
	 * @throws ClassNotFoundException
	 */
	public static HashMap<String, Object[]> parseMethodArgs(SimpleNode argsNode,
			Parser p) {
		final HashMap<String, Object[]> result = new HashMap<String, Object[]>();

		// Collect classes of the arguments to later find a matching Method.
		final int numArgs = argsNode.jjtGetNumChildren();
		final Class<?>[] paramTypes = new Class<?>[numArgs];
		final Object[] args = new Object[numArgs];

		for (int i = 0; i < numArgs; i++) {
			final Object arg = p.doChild(argsNode, i).getRawValue();
			paramTypes[i] = parsePrimitive(arg);
			args[i] = arg;
		}

		result.put("paramTypes", paramTypes);
		result.put("args", args);

		return result;
	}
}
