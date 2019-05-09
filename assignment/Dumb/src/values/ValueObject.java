package values;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import interpreter.ExceptionSemantic;

/**
 * @see https://docs.oracle.com/javase/8/docs/api/java/util/HashMap.html
 * @author amrwc
 */
public class ValueObject extends ValueAbstract {
	
	private HashMap<String, Value> internalValue = new HashMap<String, Value>();

	public ValueObject() {}

//	public ValueObject(String json) {
//		HashMap<String, Value> result =
//			new ObjectMapper().readValue(json, HashMap.class);
//		
//		Map<String, MyPojo> typedMap =
//			mapper.readValue(jsonStream, new TypeReference<Map<String, MyPojo>>() {});
//	}

	public ValueObject(JSONObject json) {
		try {
			internalValue = jsonToMap(json);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

//	public ValueObject(String jsonString) {
//		System.out.println("??? ");
//		JSONObject json;
//		try {
//			json = new JSONObject(jsonString);
//			internalValue = jsonToMap(json);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//	}

	public ValueObject(HashMap<String, Value> valueObject) {
		internalValue = valueObject;
	}

	public String getName() {
		return "ValueObject";
	}

	public int compare(Value v) {
		HashMap<String, Value> map = ((ValueObject) v).internalValue;
		return internalValue.equals(map) ? 0 : 1;
	}

	/**
	 * Create a new ValueObject from a JSON string.
	 * 
	 * @source https://stackoverflow.com/a/10514534/10620237
	 * @param {String} s -- JSON string
	 * @returns HashMap<String, Value>
	 */
//	private HashMap<String, Value> parseString(String str) {
//		HashMap<String, Value> map = new HashMap<String, Value>();
//		for (final String entry : str.split(",")) {
//		    final String[] parts = entry.split(":");
//		    assert(parts.length == 2) : "Invalid entry: " + entry;
//		    map.put(parts[0], new ValueString(parts[1]));
//		}
//		return map;
//	}

	/**
	 * @read https://stackoverflow.com/a/24012023/10620237
	 * @author https://stackoverflow.com/users/2915208/vikas-gupta
	 */
	public static HashMap<String, Value> jsonToMap(JSONObject json) throws JSONException {
	    HashMap<String, Value> retMap = new HashMap<String, Value>();

	    if(json != JSONObject.NULL)
	        retMap = toMap(json);

	    return retMap;
	}

	/**
	 * @read https://stackoverflow.com/a/24012023/10620237
	 * @author https://stackoverflow.com/users/2915208/vikas-gupta
	 */
	public static HashMap<String, Value> toMap(JSONObject object) throws JSONException {
		HashMap<String, Value> map = new HashMap<String, Value>();
		Iterator<String> keysItr = object.keys();

		while(keysItr.hasNext()) {
			String key = keysItr.next();
			Object jsonValue = object.get(key);
			Value value = null;

			if(jsonValue instanceof JSONArray) {
				value = new ValueList(toList((JSONArray) jsonValue));
			}

			else if(jsonValue instanceof JSONObject) {
				value = new ValueObject(toMap((JSONObject) jsonValue));
			}

			else {
				value = new ValueString(jsonValue.toString());
			}

			map.put(key, value);
		}

		return map;
	}

	/**
	 * @read https://stackoverflow.com/a/24012023/10620237
	 * @author https://stackoverflow.com/users/2915208/vikas-gupta
	 */
	public static ArrayList<Value> toList(JSONArray array) throws JSONException {
		ArrayList<Value> list = new ArrayList<Value>();

		for(int i = 0; i < array.length(); i++) {
			Object jsonValue = array.get(i);
			Value value = null;

			if(jsonValue instanceof JSONArray) {
				value = new ValueList(toList((JSONArray) jsonValue));
			}

			else if(jsonValue instanceof JSONObject) {
				value = new ValueObject(toMap((JSONObject) jsonValue));
			}

			else {
				value = new ValueString(jsonValue.toString());
			}

			list.add(value);
		}

		return list;
	}

	/**
	 * Execute a prototype function.
	 * 
	 * @param protoFunc
	 * @param protoArgs
	 * @return Value
	 * @author amrwc
	 */
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
		Value value = internalValue.get(name);
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
	public String toString() {
		if (internalValue.size() == 0) return "{}";
		String result = "{";
		for (HashMap.Entry<String, Value> entry : internalValue.entrySet())
			result += entry.getKey() + ": " + entry.getValue() + ", ";
		return result.substring(0, result.length() - 2) + "}";
	}
}
