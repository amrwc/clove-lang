package uk.ac.derby.ldi.slip.values;

import java.io.*;

import uk.ac.derby.ldi.slip.exceptions.*;

/** A Bunch is a collection of ItemS. */
public class Bunch extends AbstractValue {

	private static final long serialVersionUID = 0;

	private Item head;
	private Item tail;
	private boolean locked = false;
		
	/** Constructor. */
	public Bunch() {
		head = null;
		tail = null;
		locked = false;
	}
	
	/** Constructor with explicit head and tail.  Used by Quote, getRest(), etc. to construct a sub-list. */
	Bunch(Item h, Item t) {
		head = h;
		tail = t;
		locked = true;
	}
	
	/** Evaluate this Value as an operator. */
	public Value evaluate(Resolver resolver, Walker arguments) {
		if (!arguments.hasNext())
			return Nil.getInstance();
		Value head = arguments.next();
		if (head instanceof Bunch)
			head = head.evaluate(resolver);
		if (head instanceof Operator)
			head = head.evaluate(resolver, arguments);
		return head;
	}

	/** Evaluate this Value as a non-operator. */
	public Value evaluate(Resolver resolver) {
		return evaluate(resolver, getWalker());
	}
	
	public String getTypeName() {
		return "List";
	}
	
	public void insert(Item item) {
		if (locked)
			throw new Fatal("List is locked.");
		if (head == null)
			head = item;
		else
			tail.setNext(item);
		tail = item;		
	}

	public void insert(Value item) {
		insert(new Item(item));
	}
	
	/** Return the item at the head of a Bunch. */
	public Item getHead() {
		return head;
	}
	
	/** Return the item at the tail of a Bunch. */
	public Item getTail() {
		return tail;
	}
	
	/** Return all but the head. */
	public Bunch getRest() {
		return new Bunch(head.getNext(), tail);
	}
	
	public Walker getWalker() {
		return new Walker(this);
	}
	
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append('(');
		Walker iterator = getWalker();
		boolean isFirst = true;
		while (iterator.hasNext()) {
			if (!isFirst)
				s.append(" ");
			else
				isFirst = false;
			s.append(iterator.next());
		}
		s.append(')');
		return s.toString();
	}
	
	public void toStream(PrintStream p) {
		p.print('(');
		Walker iterator = getWalker();
		boolean isFirst = true;
		while (iterator.hasNext()) {
			if (!isFirst)
				p.print(" ");
			else
				isFirst = false;
			p.print(iterator.next());
		}
		p.print(')');
	}
}
