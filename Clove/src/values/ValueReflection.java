package values;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;

import interpreter.Parser;
import parser.ast.SimpleNode;

/**
 * @read https://www.sitepoint.com/java-reflection-api-tutorial/
 * @read https://www.geeksforgeeks.org/reflection-in-java/
 * @read http://tutorials.jenkov.com/java-reflection/index.html
 * @author amrwc
 * 
 * TODO:
 * - Refactor the parseArgs() methods and perhaps merge them,
 *   or rename appropriately.
 * - Implement execProto() method, or remove it if it is
 *   for no use.
 * - Add a constructor to Value that will take Object and do
 *   all the casting that is done in getCorrespondingValue
 *   -- just move this method into the Value interface.
 */
public class ValueReflection extends ValueAbstract {
	private Class<?> theClass;
	private Constructor<?> constructor;
	private Object instance;

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
			instance = constructor.newInstance(args.get("args"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create a new ValueReflection instance using
	 * the argument of Object type.
	 * 
	 * @param {Object} newObject
	 * @throws ClassNotFoundException 
	 */
	public ValueReflection(Object newObject) throws ClassNotFoundException {
		theClass = newObject.getClass();
		instance = newObject;
	}

	/**
	 * Parses constructor arguments and returns them in the correct form.
	 * 
	 * @param {Value[]} ctorArgs
	 * @returns {HashMap<String, Object[]>} map containing the parameter types
	 *          and the arguments in their 'raw type'
	 * @throws ClassNotFoundException
	 */
	private HashMap<String, Object[]> parseCtorArgs(Value[] ctorArgs) throws ClassNotFoundException {
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
	 * Invokes a Method from an instance. This method gets
	 * the Method's name from the ASTFunctionInvocation node,
	 * parses the arguments from its ASTArgumentList node,
	 * and returns the resulting Object.
	 * 
	 * @param {SimpleNode (ASTFunctionInvocation)} node
	 * @param {Parser} p -- the active Parser's instance
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
			var args = parseMethodArgs(argsNode, p);
			final Method method =
				theClass.getMethod(methodName, (Class<?>[]) args.get("paramTypes"));
			method.setAccessible(true);
			final Object result = method.invoke(instance, args.get("args"));
			return getCorrespondingValue(result);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Parses Method arguments and returns them in the correct form.
	 * 
	 * @param {SimpleNode (ASTArgumentList)} argsNode -- the Method's arguments
	 * @param {Parser} p -- active Parser's instance
	 * @returns {HashMap<String, Object[]>} map containing the parameter types
	 *          and the arguments in their 'raw type'
	 * @throws ClassNotFoundException
	 */
	private HashMap<String, Object[]> parseMethodArgs(SimpleNode argsNode, Parser p) throws ClassNotFoundException {
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

		if (Boolean.class == perhapsPrimitive) return boolean.class;
	    if (Byte.class == perhapsPrimitive)    return byte.class;
	    if (Short.class == perhapsPrimitive)   return short.class;
	    if (Integer.class == perhapsPrimitive) return int.class;
	    if (Long.class == perhapsPrimitive)    return long.class;
	    if (Float.class == perhapsPrimitive)   return float.class;
	    if (Double.class == perhapsPrimitive)  return double.class;

	    return perhapsPrimitive;
	}

	/**
	 * Creates correct Value-type from primitives, or gives
	 * a ValueReflection object for everything else.
	 * 
	 * @param {Object} methodResult
	 * @returns {Value} Value-type corresponding to the resulting primitive
	 * @throws ClassNotFoundException 
	 */
	private Value getCorrespondingValue(Object methodResult) throws ClassNotFoundException {
		if (methodResult instanceof Boolean) return new ValueBoolean((boolean) methodResult);
	    if (methodResult instanceof Short)   return new ValueInteger((short) methodResult);
	    if (methodResult instanceof Integer) return new ValueInteger((int) methodResult);
	    if (methodResult instanceof Long)    return new ValueInteger((long) methodResult);
	    if (methodResult instanceof Float)   return new ValueRational((float) methodResult);
	    if (methodResult instanceof Double)  return new ValueRational((double) methodResult);
	    if (methodResult instanceof String)  return new ValueString((String) methodResult);

	    return new ValueReflection(methodResult);
	}

	/**
	 * Casting ValueReflection to another class.
	 */
	public static ValueReflection cast(String targetClassName, ValueReflection objToCast) {
		try {
			final Object targetClass = Class.forName(targetClassName);
			final Object casted = ((Class<?>) targetClass).cast(objToCast.getRawValue());
			return new ValueReflection(casted);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public String getName() {
		return "ValueReflection";
	}

	@Override
	public int compare(Value v) {
		final Class<?> incomingClass = ((ValueReflection) v).theClass;
		final Object incomingInstance = ((ValueReflection) v).instance;
		if (instance != null)
			return instance.equals(incomingInstance) ? 0 : 1;
		else
			return theClass.equals(incomingClass) ? 0 : 1;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getRawValue() {
		return (instance != null) ? instance : theClass;
	}

	/**
	 * Dereferences a value in a nested expression.
	 * 
	 * @param {SimpleNode} node -- node in question
	 * @param {Value} v -- value to be dereferenced
	 * @param {int} currChild -- current child of the node being parsed
	 * @param {Parser} p -- the instance of Parser currently running
	 * @returns {Value} the dereferenced value
	 */
	@Override
	public Value dereference(SimpleNode node, Value v, int currChild, Parser p) {
		return this;
	}

	/**
	 * @returns {String} key-value pairs in '{key: value}' notation
	 */
	@Override
	public String toString() {
		if (constructor != null && instance != null)
			return "{\n  class: " + theClass.getCanonicalName()
				+ ",\n  constructor: " + constructor.toGenericString()
				+ ",\n  instance: " + instance.toString() + "\n}";
		else
			return "{class: " + theClass.getCanonicalName() + "}";
	}

	@Override
	public String stringValue() {
		return toString();
	}
}
