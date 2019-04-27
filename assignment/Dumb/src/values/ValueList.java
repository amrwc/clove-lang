package values;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * @see https://docs.oracle.com/javase/8/docs/api/index.html?java/util/ArrayList.html
 * @author amrwc
 */
public class ValueList extends ValueAbstract {

	private ArrayList<Value> internalValue = new ArrayList<Value>();
	
	public ValueList() {}
	
	public String getName() {
		return "List";
	}

	public int compare(Value v) {
		ArrayList<Value> arr = ((ValueList) v).internalValue;

		if (internalValue.equals(arr))
			return 0;
		else
			return 1;
	}

	public void append(Value v) {
		internalValue.add(v);
	}
	
	public Value get(int i) {
		return internalValue.get(i);
	}

	public void set(int i, Value v) {
		internalValue.set(i, v);
	}

	// To be used in Dumb-lang because it resolves to a Value.
	public Value length() {
		return new ValueInteger((long) internalValue.size());
	}

	// To be used internally, by the Parser implementation.
	public int size() {
		return internalValue.size();
	}

	public String toString() {
		// https://stackoverflow.com/a/23183963/10620237
		String strVal = internalValue
							.stream()
							.map(Object::toString)
							.collect(Collectors.joining(", "));
		return "[" + strVal + "]";
	}
}
