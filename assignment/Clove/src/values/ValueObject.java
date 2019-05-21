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
 * @see https://docs.oracle.com/javase/8/docs/api/java/util/HashMap.html
 * @author amrwc
 */
public class ValueObject extends ValueAbstract {
	
	private HashMap<String, Value> internalValue = new HashMap<String, Value>();

	public ValueObject() {}

	public ValueObject(HashMap<String, Value> valueObject) {
		internalValue = valueObject;
	}

	@Override
	public String getName() {
		return "ValueObject";
	}

	@Override
	public int compare(Value v) {
		final HashMap<String, Value> map = ((ValueObject) v).internalValue;
		return internalValue.equals(map) ? 0 : 1;
	}

	/**
	 * Dereferences a value in a nested expression.
	 * 
	 * @param node -- node in question
	 * @param v -- value to be dereferenced
	 * @param currChild -- current child of the node being parsed
	 * @returns {Value} the dereferenced value
	 */
	@Override
	public Value dereference(SimpleNode node, Value v, int currChild) {
		final Parser par = new Parser();
		final ValueObject valueObject = (ValueObject) v;
		final var keyName = node.jjtGetChild(currChild) instanceof ASTIdentifier
			? Parser.getTokenOfChild(node, currChild)
			: par.doChild(node, currChild).toString();
		return valueObject.get(keyName);
	}

	/**
	 * Execute a prototype function.
	 * 
	 * @param protoFunc
	 * @param protoArgs
	 * @return Value
	 * @author amrwc
	 */
	@Override
	public Value execProto(String protoFunc, ArrayList<Value> protoArgs) {
		switch (protoFunc) {
			case "getClass":
				return new ValueString(getName());
			case "remove":
				protoArgs.forEach(arg -> remove(arg.stringValue()));
				break;
			case "tryRemove":
				protoArgs.forEach(arg -> tryRemove(arg.stringValue()));
				break;
			default:
				throw new ExceptionSemantic("There is no prototype function \"" + protoFunc + "\" in ValueObject class.");
		}

		return null;
	}

	public void add(String name, Value v) {
		internalValue.putIfAbsent(name, v);
	}

	public Value get(String name) {
		final Value value = internalValue.get(name);
		if (value != null) return value;
		throw new ExceptionSemantic("Object key \"" + name + "\" is undefined or equal to null.");
	}

	public void set(String name, Value v) {
		if (name == null || name == "null" || v == null)
			throw new ExceptionSemantic("Neither key nor value of an object can be null.");
		internalValue.put(name, v);
	}

	private void remove(String name) {
		if (internalValue.containsKey(name))
			internalValue.remove(name);
		else
			throw new ExceptionSemantic("This ValueObject does not contain the \"" + name + "\" key.");
	}

	private void tryRemove(String name) {
		internalValue.remove(name);
	}

	// Returns the key-value pairs in '{key: value}' notation.
	@Override
	public String toString() {
		if (internalValue.size() == 0) return "{}";
		String result = "{";
		for (final HashMap.Entry<String, Value> entry : internalValue.entrySet())
			result += entry.getKey() + ": " + entry.getValue() + ", ";
		return result.substring(0, result.length() - 2) + "}";
	}

	/**
	 * Turns the ValueObject into an url-encoded string.
	 * 
	 * @read https://stackoverflow.com/a/2810102/10620237
	 * @read https://stackoverflow.com/a/29213105/10620237
	 * @returns url-encoded string
	 */
	public String toUrlString() {
		return internalValue.entrySet().stream()
			.map(p -> urlEncUTF8(p.getKey().toString()) + "=" + urlEncUTF8(p.getValue().toString()))
			.reduce((p1, p2) -> p1 + "&" + p2)
			.orElse("");
	}
	
	private String urlEncUTF8(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new UnsupportedOperationException(e);
        }
    }
}
