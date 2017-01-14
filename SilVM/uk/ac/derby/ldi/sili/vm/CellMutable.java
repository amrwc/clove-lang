package uk.ac.derby.ldi.sili.vm;


import uk.ac.derby.ldi.sili.values.Value;

public class CellMutable implements Cell {

	private Value value;
	
	public Value getValue() {
		return value;
	}

	public void setValue(Value v) {
		value = v;
	}
}
