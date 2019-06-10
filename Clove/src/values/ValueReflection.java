package values;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
 */
public class ValueReflection extends ValueAbstract {
	private Class<?> theClass;
	private Constructor<?> constructor;
	private Object internalValue;

	/**
	 * Creates a new ValueReflection holding a class.
	 * 
	 * @param {String} className
	 */
	public ValueReflection(String className) {
		try {
			// Store the class.
			theClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new ExceptionSemantic("");
		}
	}

	/**
	 * Creates a new ValueReflection instance based on the class name and the array
	 * of arguments for the class's constructor.
	 * 
	 * @param {String}  className
	 * @param {Value[]} ctorArgs
	 */
	public ValueReflection(String className, Value[] ctorArgs) {
		try {
			// Store the class.
			theClass = Class.forName(className);

			// Instantiate the object.
			instantiateWithArguments(ctorArgs);
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
			throw new ExceptionSemantic("");
		}
	}

	/**
	 * Creates a new ValueReflection copy of a passed Object instance.
	 * 
	 * @param {Class<?>} clazz
	 * @param {Object}   newObject
	 */
	public ValueReflection(Class<?> clazz, Object newObject) {
		theClass = clazz;
		internalValue = newObject;
	}

	/**
	 * Instantiates the class with an empty constructor.
	 */
	private void instantiateEmpty() {
		try {
			// Get an empty constructor.
			constructor = theClass.getConstructor();

			// Instantiate the class with the empty constructor.
			internalValue = constructor.newInstance();
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
	private void instantiateWithArguments(ValueList ctorArgsValue) {
		final Value[] ctorArgs = new Value[ctorArgsValue.size()];
		for (int i = 0; i < ctorArgsValue.size(); i++)
			ctorArgs[i] = ctorArgsValue.get(i);

		instantiateWithArguments(ctorArgs);
	}

	/**
	 * Instantiates the object with the arguments passed in as a Value array.
	 * 
	 * @param {Value[]} ctorArgs
	 */
	private void instantiateWithArguments(Value[] ctorArgs) {
		// Store the parameters types to choose the right ctor,
		// and the arguments in their raw form.
		final HashMap<String, Object[]> args = parseCtorArgs(ctorArgs);

		try {
			// Get constructor matching the parameter classes.
			constructor = theClass.getConstructor((Class<?>[]) args.get("paramTypes"));

			// Create an instance of the class using the arguments
			// and their matching constructor.
			internalValue = constructor.newInstance(args.get("args"));
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
	private HashMap<String, Object[]> parseCtorArgs(Value[] ctorArgs) {
		final HashMap<String, Object[]> result = new HashMap<String, Object[]>();
		final Class<?>[] paramTypes = new Class[ctorArgs.length];
		final Object[] args = new Object[ctorArgs.length];

		for (int i = 0; i < ctorArgs.length; i++) {
			paramTypes[i] = (ctorArgs[i] instanceof ValueReflection)
					? ((ValueReflection) ctorArgs[i]).theClass
					: parsePrimitive(ctorArgs[i].getRawValue());
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

		final var args = parseMethodArgs(argsNode, p);

		Object result = null;
		try {
			// Get the method matching the parameter types.
			Method method = theClass.getMethod(methodName,
					(Class<?>[]) args.get("paramTypes"));
			method.setAccessible(true);

			// Invoke the method with the arguments.
			result = method.invoke(internalValue, args.get("args"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new ExceptionSemantic("");
		}

		// If the method returned anything, return the result in the correct Value-type.
		if (result != null)
			return getCorrespondingValue(result);

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
	private HashMap<String, Object[]> parseMethodArgs(SimpleNode argsNode, Parser p) {
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
	private Class<?> parsePrimitive(Object arg) {
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
	 * Casting ValueReflection to another class.
	 */
	public static ValueReflection cast(String targetClassName,
			ValueReflection objToCast) {
		Class<?> targetClass = null;

		try {
			targetClass = Class.forName(targetClassName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new ExceptionSemantic("");
		}

		return getInstanceWithClassTypeAsSuperclass(targetClass, objToCast);
	}

	/**
	 * Returns a new ValueReflection with a superclass of the previous 'theClass',
	 * if it matches the 'type' argument.
	 * 
	 * @param {Class<?>}        type
	 * @param {ValueReflection} objToCast
	 * @returns {ValueReflection} new ValueReflection with a new specified inner
	 *          class
	 */
	private static ValueReflection getInstanceWithClassTypeAsSuperclass(Class<?> type,
			ValueReflection objToCast) {
		Class<?> superclass = objToCast.theClass.getSuperclass();

		while (superclass != null) {
			if (superclass.equals(type))
				return new ValueReflection(type, objToCast.getRawValue());

			superclass = superclass.getSuperclass();
		}

		throw new ExceptionSemantic("Class '" + type + "' is not a superclass of '"
				+ objToCast.theClass + "', therefore it cannot be casted.");
	}

	@Override
	public Value execProto(String protoFunc, ArrayList<Value> protoArgs) {
		switch (protoFunc) {
		case "getClass":
			return new ValueString(getName());
		case "instantiate":
			// If there's no arguments, instantiate the object using an empty constructor.
			if (protoArgs == null) {
				instantiateEmpty();
				return this;
			}

			// If there are constructor arguments, instantiate the object with the
			// arguments.
			if (protoArgs.size() != 0)
				instantiateWithArguments((ValueList) protoArgs.get(0));

			return this;
		default:
			throw new ExceptionSemantic("There is no prototype function '" + protoFunc
					+ "' in the '" + getName() + "' class.");
		}
	}

	@Override
	public String getName() {
		return (internalValue == null) ? theClass.getCanonicalName()
				: internalValue.getClass().getCanonicalName();
	}

	@Override
	public int compare(Value v) {
		// If the internal value is a boolean...
		if (internalValue instanceof Boolean)
			return new ValueBoolean((boolean) internalValue).compare(v);

		return NumberUtils.compareNumberValues(this, v);
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
		checkIfInstantiated("unary_plus");
		return getCorrespondingValue(internalValue).unary_plus();
	}

	@Override
	public Value unary_minus() {
		checkIfInstantiated("unary_plus");
		return getCorrespondingValue(internalValue).unary_minus();
	}
}
