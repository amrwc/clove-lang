package values;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;

import interpreter.ExceptionSemantic;
import interpreter.NumberUtils;
import interpreter.Parser;
import parser.ast.SimpleNode;

/**
 * @read https://www.sitepoint.com/java-reflection-api-tutorial/
 * @read https://www.geeksforgeeks.org/reflection-in-java/
 * @read http://tutorials.jenkov.com/java-reflection/index.html
 * @author amrwc
 * 
 *         TODO: - Add a way to instantiate the reflected class later on (create
 *         instantiate() method). - Consider implementing the execProto()
 *         method.
 */
public class ValueReflection extends ValueAbstract {
	private Class<?> theClass;
	private Constructor<?> constructor;
	private Object internalValue;

	public ValueReflection(String className) throws ClassNotFoundException {
		theClass = Class.forName(className);
	}

	public ValueReflection(String className, Value[] ctorArgs) {
		try {
			// Store the parameters types to choose the right ctor,
			// and the arguments in their raw form.
			final HashMap<String, Object[]> args = parseCtorArgs(ctorArgs);

			// Get class using its canonical name.
			theClass = Class.forName(className);

			// Get constructor matching the parameter classes.
			constructor = theClass.getConstructor((Class<?>[]) args.get("paramTypes"));

			// Create an instance of the class using the arguments
			// and their matching constructor.
			internalValue = constructor.newInstance(args.get("args"));
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create a new ValueReflection instance using the argument of Object type.
	 * 
	 * @param {Object} newObject
	 * @throws ClassNotFoundException
	 */
	public ValueReflection(Object newObject) throws ClassNotFoundException {
		theClass = newObject.getClass();
		internalValue = newObject;
	}

	/**
	 * Parses constructor arguments and returns them in the correct form.
	 * 
	 * @param {Value[]} ctorArgs
	 * @returns {HashMap<String, Object[]>} map containing the parameter types and
	 *          the arguments in their 'raw type'
	 * @throws ClassNotFoundException
	 */
	private HashMap<String, Object[]> parseCtorArgs(Value[] ctorArgs)
			throws ClassNotFoundException {
		final HashMap<String, Object[]> result = new HashMap<String, Object[]>();
		final Class<?>[] paramTypes = new Class[ctorArgs.length];
		final Object[] args = new Object[ctorArgs.length];

		for (int i = 0; i < ctorArgs.length; i++) {
			paramTypes[i] = parsePrimitive(ctorArgs[i].getRawValue());
			args[i] = ctorArgs[i].getRawValue();
		}

		result.put("paramTypes", paramTypes);
		result.put("args", args);

		return result;
	}

	/**
	 * Invokes a Method from an instance. This method gets the Method's name from
	 * the ASTFunctionInvocation node, parses the arguments from its ASTArgumentList
	 * node, and returns the resulting Object.
	 * 
	 * @param {SimpleNode (ASTFunctionInvocation)} node
	 * @param {Parser}    p -- the active Parser's instance
	 * @returns {Object} the Method's result
	 */
	public Object invoke(SimpleNode node, Parser p) {
		// ASTDereference
		final SimpleNode derefNode = (SimpleNode) node.jjtGetChild(0);

		// Get the Method's name.
		final String methodName = Parser.getTokenOfChild(derefNode, 0);

		// ASTArgumentList
		final SimpleNode argsNode = (SimpleNode) node.jjtGetChild(1);

		try {
			final var args = parseMethodArgs(argsNode, p);
			final Method method = theClass.getMethod(methodName,
					(Class<?>[]) args.get("paramTypes"));
			method.setAccessible(true);
			final Object result = method.invoke(internalValue, args.get("args"));
			return getCorrespondingValue(result);
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return null;
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
	private HashMap<String, Object[]> parseMethodArgs(SimpleNode argsNode, Parser p)
			throws ClassNotFoundException {
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

	/**
	 * Adjusts class to match Method.getMethod() arguments.
	 * 
	 * @read https://stackoverflow.com/a/13943623/10620237
	 * @param {Object} arg -- source argument whose class is to be returned
	 * @returns {Class<?>} adjusted class that will match Method.getMethod()
	 * @throws ClassNotFoundException
	 */
	private Class<?> parsePrimitive(Object arg) throws ClassNotFoundException {
		final Class<?> perhapsPrimitive = Class.forName(arg.getClass().getName());

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
	 * Casting ValueReflection to another class.
	 */
	public static ValueReflection cast(String targetClassName,
			ValueReflection objToCast) {
		try {
			final Object targetClass = Class.forName(targetClassName);
			final Object casted = ((Class<?>) targetClass).cast(objToCast.getRawValue());
			return new ValueReflection(casted);
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public String getName() {
		return (internalValue == null) ? theClass.getName()
				: internalValue.getClass().toString();
	}

	@Override
	public int compare(Value v) {
		// If the internal value is a boolean...
		if (internalValue instanceof Boolean)
			return new ValueBoolean((boolean) internalValue).compare(v);

		// If one of the values is not a number-type...
		if (!NumberUtils.isNumber(internalValue)
				|| !NumberUtils.isNumber(v.getRawValue())) {
			if (v instanceof ValueReflection)
				return compareInstances(v);
			throw new ExceptionSemantic(
					"Cannot compare '" + theClass + " (" + internalValue + ")' and '"
							+ v.getName() + " (" + v.getRawValue() + ")'.");
		}

		final double inDouble = Double.parseDouble(stringValue());
		final double vDouble = Double.parseDouble(v.stringValue());
		return Double.compare(inDouble, vDouble);
	}

	public int compareInstances(Value v) {
		final Class<?> incomingClass = ((ValueReflection) v).theClass;
		final Object incomingInstance = ((ValueReflection) v).internalValue;
		if (internalValue != null)
			return internalValue.equals(incomingInstance) ? 0 : 1;
		else
			return theClass.equals(incomingClass) ? 0 : 1;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getRawValue() {
		return (internalValue != null) ? internalValue : theClass;
	}

	/**
	 * Dereferences a value in a nested expression.
	 * 
	 * @param {SimpleNode} node -- node in question
	 * @param {Value}      v -- value to be dereferenced
	 * @param {int}        currChild -- current child of the node being parsed
	 * @param {Parser}     p -- the instance of Parser currently running
	 * @returns {Value} the dereferenced value
	 */
	@Override
	public Value dereference(SimpleNode node, Value v, int currChild, Parser p) {
		return this;
	}

	/**
	 * Returns the whole ValueReflection instance, including class name, constructor
	 * and the internal value.
	 * 
	 * @returns {String} key-value pairs in '{key: value}' notation
	 */
	public String toObjectString() {
		if (constructor != null && internalValue != null)
			return "{\n  class: " + theClass.getCanonicalName() + ",\n  constructor: "
					+ constructor.toGenericString() + ",\n  value: "
					+ internalValue.toString() + "\n}";
		else
			return "{class: " + theClass.getCanonicalName() + "}";
	}

	@Override
	public String stringValue() {
		return toString();
	}

	@Override
	public String toString() {
		return (internalValue == null) ? theClass.toString() : internalValue.toString();
	}

	@Override
	public double doubleValue() {
		checkIfInstantiated("doubleValue");
		try {
			return Double.parseDouble(toString());
		} catch (NumberFormatException e) {
			throw new ExceptionSemantic("The '" + theClass + "' Reflection class "
					+ "cannot be parsed to Double.");
		}
	}

	private void checkIfInstantiated(String caller) {
		if (internalValue == null)
			throw new ExceptionSemantic("The '" + theClass + "' Reflection class "
					+ "is not instantiated, therefore '" + caller
					+ "' method cannot be executed.");
	}

	/*********************************
	 * Operations on the instance(s) *
	 *********************************/

	@Override
	public Value or(Value v) {
		checkIfInstantiated("OR");
		final Value v1 = getCorrespondingValue(internalValue);
		final Value v2 = getCorrespondingValue(v.getRawValue());
		return v1.or(v2);
	}

	@Override
	public Value and(Value v) {
		checkIfInstantiated("AND");
		final Value v1 = getCorrespondingValue(internalValue);
		final Value v2 = getCorrespondingValue(v.getRawValue());
		return v1.and(v2);
	}

	@Override
	public Value not() {
		checkIfInstantiated("NOT");
		return getCorrespondingValue(internalValue).not();
	}

	@Override
	public Value add(Value v) {
		checkIfInstantiated("add");
		final Value v1 = getCorrespondingValue(internalValue);
		final Value v2 = getCorrespondingValue(v.getRawValue());
		return v1.add(v2);
	}

	@Override
	public Value subtract(Value v) {
		checkIfInstantiated("subtract");
		final Value v1 = getCorrespondingValue(internalValue);
		final Value v2 = getCorrespondingValue(v.getRawValue());
		return v1.subtract(v2);
	}

	@Override
	public Value mult(Value v) {
		checkIfInstantiated("mult");
		final Value v1 = getCorrespondingValue(internalValue);
		final Value v2 = getCorrespondingValue(v.getRawValue());
		return v1.mult(v2);
	}

	@Override
	public Value div(Value v) {
		checkIfInstantiated("div");
		final Value v1 = getCorrespondingValue(internalValue);
		final Value v2 = getCorrespondingValue(v.getRawValue());
		return v1.div(v2);
	}

	@Override
	public Value mod(Value v) {
		checkIfInstantiated("mod");
		final Value v1 = getCorrespondingValue(internalValue);
		final Value v2 = getCorrespondingValue(v.getRawValue());
		return v1.mod(v2);
	}

	@Override
	public Value unary_plus() {
		throw new ExceptionSemantic("Cannot perform + on " + getName());
	}

	@Override
	public Value unary_minus() {
		throw new ExceptionSemantic("Cannot perform - on " + getName());
	}
}
