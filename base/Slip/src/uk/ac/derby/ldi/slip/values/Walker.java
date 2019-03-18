package uk.ac.derby.ldi.slip.values;

/** A Walker is an iterator over a Bunch. */
public class Walker extends AbstractValue {

	private static final long serialVersionUID = 0;
	private Item next;
	private Item tail;
	
	public Walker(Bunch l) {
		next = l.getHead();
		tail = l.getTail();
	}
	
	public Value next() {
		Value v = next.getItem();
		next = next.getNext();
		return v;
	}
	
	/** Terminate the iterator, i.e., effectively move it to the end of the Bunch. */
	public void terminate() {
		next = null;
	}
	
	/** Return current position to end of the iterated Bunch as a new Bunch, 
	 * and end this iterator. */
	public Bunch rest() {
		Bunch v = new Bunch(next, tail);
		terminate();
		return v;
	}
	
	public boolean hasNext() {
		return (next != null);
	}
	
	public String getTypeName() {
		return "ListIterator";
	}
}
