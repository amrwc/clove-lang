package values;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import interpreter.ExceptionSemantic;
import interpreter.Parser;
import parser.ast.ASTIdentifier;
import parser.ast.SimpleNode;

/**
 * @read https://www.sitepoint.com/java-reflection-api-tutorial/
 * @read https://www.geeksforgeeks.org/reflection-in-java/
 * @author amrwc
 */
public class ValueReflection extends ValueAbstract {
	
//	private HashMap<String, Value> internalValue = new HashMap<String, Value>();
	private Class<?> internalValue;

//	public ValueReflection() {}

	public ValueReflection(Value v) throws ReflectiveOperationException {
		// TODO:
		internalValue = Class.forName(v.stringValue());
		System.out.println("Class: " + internalValue.getCanonicalName());
	}

	public ValueReflection(Value v, ArrayList<String> args) {
		// TODO:
	}

//	public ValueReflection(HashMap<String, Value> valueObject) {
//		internalValue = valueObject;
//	}

	@Override
	public String getName() {
		return "ValueReflection";
	}

	@Override
	public int compare(Value v) {
		// TODO: TEST IT
		final Object obj = ((ValueReflection) v).internalValue;
		return internalValue.equals(obj) ? 0 : 1;
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
	 * @author amrwc
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

//	public void add(String name, Value v) {
//		internalValue.putIfAbsent(name, v);
//	}

//	public Value get(String name) {
//		final Value value = internalValue.get(name);
//		if (value != null) return value;
//		throw new ExceptionSemantic("Object key \"" + name + "\" is undefined or equal to null.");
//	}

//	public void set(String name, Value v) {
//		if (name == null || name == "null" || v == null)
//			throw new ExceptionSemantic("Neither key nor value of an object can be null.");
//		internalValue.put(name, v);
//	}

//	private void remove(String name) {
//		if (internalValue.containsKey(name))
//			internalValue.remove(name);
//		else
//			throw new ExceptionSemantic("This ValueObject does not contain the \"" + name + "\" key.");
//	}
//
//	private void tryRemove(String name) {
//		internalValue.remove(name);
//	}

	// Returns the key-value pairs in '{key: value}' notation.
//	@Override
//	public String toString() {
//		// TODO:
//		if (internalValue.size() == 0) return "{}";
//		String result = "{";
//		for (final HashMap.Entry<String, Value> entry : internalValue.entrySet())
//			result += entry.getKey() + ": " + entry.getValue() + ", ";
//		return result.substring(0, result.length() - 2) + "}";
//	}

//	@Override
//	public String stringValue() {
//		return toString();
//	}

//	public int size() {
//		// TODO:
//		return internalValue.size();
//	}

//	/**
//	 * Returns a list of the anonymous object's keys.
//	 * 
//	 * @returns {ValueList} list of ValueString's of the object's keys
//	 */
//	private ValueList keys() {
//		// TODO:
//		final ValueList keys = new ValueList();
//		internalValue.keySet().forEach(key -> keys.append(new ValueString(key)));
//		return keys;
//	}
}
