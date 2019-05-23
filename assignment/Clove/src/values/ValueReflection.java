package values;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import interpreter.ExceptionSemantic;
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

//			URL url = new URL("https://jsonplaceholder.typicode.com/posts/1");
//			Object conn = Class.forName("java.net.HttpURLConnection");
//			conn = ((Class<?>) conn).cast(url.openConnection());
//System.out.println("INSANITY: " + conn.getClass());
// TODO: Change Class<?> for theClass to Object and then cast it whenever necessary.
//       This way, it's possible to cast shit like the above.
//       OR: only do this when casting is necessary (make a 'cast' token?).
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
//		theClass = Class.forName(newObject.getClass().getCanonicalName());
		instance = newObject;
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
	@Override
	public Value dereference(SimpleNode node, Value v, int currChild, Parser p) {
		// TODO:
//		final ValueReflection valueReflection = (ValueReflection) v;
//		final String keyName = (node.jjtGetChild(currChild) instanceof ASTIdentifier)
//			? Parser.getTokenOfChild(node, currChild)
//			: p.doChild(node, currChild).toString();

		return this;
//		return null;
//		return valueReflection.get(keyName);
	}

	// Invoke a Method from an instance.
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
			var args = parseArgs(argsNode, p);
			final Method method =
				theClass.getMethod(methodName, (Class<?>[]) args.get("paramTypes"));
			method.setAccessible(true);
			return new ValueReflection(method.invoke(instance, args.get("args")));
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
	private HashMap<String, Object[]> parseArgs(SimpleNode argsNode, Parser p) throws ClassNotFoundException {
		final HashMap<String, Object[]> result = new HashMap<String, Object[]>();

		// Collect classes of the arguments to later find a matching Method.
		Class<?>[] paramTypes = new Class<?>[0];
		Object[] args = new Object[0];

		final int numArgs = argsNode.jjtGetNumChildren();
		if (numArgs > 0) {
			// Re-initialise the arrays with the right length.
			paramTypes = new Class<?>[numArgs];
			args = new Object[numArgs];

			for (int i = 0; i < numArgs; i++) {
				final Object arg = p.doChild(argsNode, i).getRawValue();
				paramTypes[i] = arg.getClass();
				args[i] = arg;
			}
		}

		result.put("paramTypes", paramTypes);
		result.put("args", args);

		return result;
	}

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
