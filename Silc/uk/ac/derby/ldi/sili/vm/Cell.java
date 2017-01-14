package uk.ac.derby.ldi.sili.vm;


import uk.ac.derby.ldi.sili.values.Value;

/** A Value container, used to implement variables at run-time. */
public interface Cell {
	public void setValue(Value v);
	public Value getValue();
}
