package values;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ValueList extends ValueAbstract {

	private ArrayList<Value> internalValue = new ArrayList<Value>();
	
	public ValueList() {}
	
	public String getName() {
		return "List";
	}

	public int compare(Value v) {
		// TODO Auto-generated method stub
//		return internalValue.compareTo(v);
		return 0;
	}

	public void append(Value v) {
//		System.out.print("DEBUG, ValueList.append: "); // DEBUG:
//		System.out.println(internalValue.get(i)); // DEBUG:
		internalValue.add(v);
	}
	
	public Value get(int i) {
//		System.out.print("DEBUG, ValueList.get: "); // DEBUG:
//		System.out.println(internalValue.get(i)); // DEBUG:
//		if (internalValue.)
		return internalValue.get(i);
	}

	public int length() {
		return internalValue.size();
	}

	public String toString() {
		// https://stackoverflow.com/a/23183963/10620237
		return internalValue
				.stream()
				.map(Object::toString)
				.collect(Collectors.joining(", "));
	}
}
