package uk.ac.derby.ldi.slip.values;

import java.io.PrintStream;

/** An Item is a Value in a Bunch. */
public class Item implements Value {

	private static final long serialVersionUID = 0;
	
	private Value item;
	private Item next;
	
	public Item(Value v) {
		item = v;
		next = null;
	}
	
	public Value getItem() {
		return item;
	}
	
	void setNext(Item nextItem) {
		next = nextItem;
	}
	
	Item getNext() {
		return next;
	}
	
	public Value add(Value v) {
		return item.add(v);
	}

	public Value and(Value v) {
		return item.and(v);
	}

	public boolean booleanValue() {
		return item.booleanValue();
	}

	public int compareTo(Value v) {
		return item.compareTo(v);
	}

	public int hashCode() {
		return item.hashCode();
	}
	
	public Value div(Value v) {
		return item.div(v);
	}

	public double doubleValue() {
		return item.doubleValue();
	}

	public Value eq(Value v) {
		return item.eq(v);
	}

	public Value evaluate(Resolver resolver, Walker arguments) {
		return item.evaluate(resolver, arguments);
	}

	public Value evaluate(Resolver resolver) {
		return item.evaluate(resolver);
	}
	
	public String getTypeName() {
		return item.getTypeName();
	}

	public Value gt(Value v) {
		return item.gt(v);
	}

	public Value gte(Value v) {
		return item.gte(v);
	}

	public long longValue() {
		return item.longValue();
	}

	public Value lt(Value v) {
		return item.lt(v);
	}

	public Value lte(Value v) {
		return item.lte(v);
	}

	public Value mult(Value v) {
		return item.mult(v);
	}

	public Value neq(Value v) {
		return item.neq(v);
	}

	public Value not() {
		return item.not();
	}

	public Value or(Value v) {
		return item.or(v);
	}

	public String stringValue() {
		return item.stringValue();
	}

	public void toStream(PrintStream p) {
		item.toStream(p);
	}

	public String toString() {
		return item.toString();
	}
	
	public Value subtract(Value v) {
		return item.subtract(v);
	}

	public Value unary_minus() {
		return item.unary_minus();
	}

	public Value unary_plus() {
		return item.unary_plus();
	}
	
	/** True if this is null.  Only Nil should return true. */
	public boolean isNil() {
		return item.isNil();
	}

}
