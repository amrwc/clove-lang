package values;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;

import interpreter.ExceptionSemantic;

/**
 * @read https://www.sitepoint.com/java-reflection-api-tutorial/
 * @read https://www.geeksforgeeks.org/reflection-in-java/
 * @read http://tutorials.jenkov.com/java-reflection/index.html
 * @author amrwc
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
			final HashMap<String, Object[]> args = parseArgs(ctorArgs);

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
	 * Parses constructor arguments and returns them in the correct form.
	 * 
	 * @param {Value[]} ctorArgs
	 * @returns {HashMap<String, Object[]>} map containing the parameter types
	 *          and the arguments in their 'raw type'
	 * @throws ClassNotFoundException
	 */
	private HashMap<String, Object[]> parseArgs(Value[] ctorArgs) throws ClassNotFoundException {
		final HashMap<String, Object[]> result = new HashMap<String, Object[]>();
		final Class<?>[] paramTypes = new Class[ctorArgs.length];
		final Object[] args = new Object[ctorArgs.length];

		for (int i = 0; i < ctorArgs.length; i++) {
			final String canonClass = ctorArgs[i].getRawValue().getClass().getCanonicalName();
			paramTypes[i] = Class.forName(canonClass);
			args[i] = ctorArgs[i].getRawValue();
		}

		result.put("paramTypes", paramTypes);
		result.put("args", args);

		return result;
	}

//	public ValueReflection(String className, String[] ctorParamTypes, String[] ctorArgs) {
//		try {
//			theClass = Class.forName(className);
//
//			final Class<?>[] paramTypes = new Class[ctorParamTypes.length];
//			for (int i = 0; i < ctorParamTypes.length; i++)
//				paramTypes[i] = Class.forName(ctorParamTypes[i]);
//
//			constructor = theClass.getConstructor(paramTypes);
//System.out.println("CTOR: " + constructor);
//
//			final Class<?>[] args = new Class[ctorArgs.length];
//			for (int i = 0; i < ctorArgs.length; i++)
//				args[i] = Class.forName(ctorArgs[i]);
////			instance = constructor.newInstance(initargs);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	@Override
	public String getName() {
		return "ValueReflection";
	}

	@Override
	public int compare(Value v) {
		// TODO: TEST IT
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
//	@Override
//	public Value dereference(SimpleNode node, Value v, int currChild, Parser p) {
//		// TODO:
//		final ValueReflection valueObject = (ValueReflection) v;
//		final String keyName = (node.jjtGetChild(currChild) instanceof ASTIdentifier)
//			? Parser.getTokenOfChild(node, currChild)
//			: p.doChild(node, currChild).toString();
//		return valueObject.get(keyName);
//	}

	/**
	 * Execute a prototype function.
	 * 
	 * @param {String} protoFunc -- prototype function name
	 * @param {ArrayList<Value>} protoArgs -- arguments for the function
	 * @returns {Value} result of the prototype function
	 */
	@Override
	public Value execProto(String protoFunc, ArrayList<Value> protoArgs) {
		//TODO:
		switch (protoFunc) {
//			case "getClass":
//				return new ValueString(getName());
//			case "keys":
//				return keys();
//			case "remove":
//				protoArgs.forEach(arg -> remove(arg.stringValue()));
//				break;
//			case "size":
//			case "length":
//				return new ValueInteger(size());
//			case "tryRemove":
//				protoArgs.forEach(arg -> tryRemove(arg.stringValue()));
//				break;
			default:
				throw new ExceptionSemantic("There is no prototype function \""
					+ protoFunc + "\" in " + getName() + " class.");
		}

//		return null;
	}

	// Returns the key-value pairs in '{key: value}' notation.
	@Override
	public String toString() {
		return "{\n  class: " + theClass.getCanonicalName()
			+ ",\n  constructor: " + constructor.toGenericString()
			+ ",\n  instance: " + instance.toString() + "\n}";
	}

	@Override
	public String stringValue() {
		return toString();
	}
}
