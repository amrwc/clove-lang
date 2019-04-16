package values;

public class ValueFn extends ValueAbstract {
	private String fnname;
	private int scope;

	public ValueFn() {}

	public ValueFn(String fnname, int scope) {
		this.fnname = fnname;
		this.scope = scope;
	}

	@Override
	public String getName() {
		return fnname;
	}
	
	public int getScope() {
		return scope;
	}

	@Override
	public int compare(Value v) {
		// TODO Auto-generated method stub
		return 0;
	}
}
