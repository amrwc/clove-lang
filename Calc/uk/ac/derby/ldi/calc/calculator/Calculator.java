package uk.ac.derby.ldi.calc.calculator;

import java.util.*;

/** A crude stack-based calculator engine, rather like those old RPN-based HPs
 * but without any keys or display.  It's just the processor.  Eg., to calculate
 * 10 - 2:
 * 
 *  push_integer("10");
 *  push_integer("2");
 *  subtract();
 *  System.out.println(pop());
 */
public class Calculator {

	private Stack<Value> stack = new Stack<Value>();
	
	public void push(Value v) {
		stack.push(v);
	}
	
	public Value pop() {
		return (Value)stack.pop();
	}
	
	public void or() {
		push(pop().or(pop()));
	}
	
	public void and() {
		push(pop().and(pop()));
	}
	
	public void eq() {
		push(pop().eq(pop()));
	}
	
	public void neq() {
		push(pop().neq(pop()));		
	}
	
	public void gte() {
		Value v2 = pop();
		push(pop().gte(v2));
	}
	
	public void lte() {
		Value v2 = pop();
		push(pop().lte(v2));		
	}
	
	public void gt() {
		Value v2 = pop();
		push(pop().gt(v2));		
	}
	
	public void lt() {
		Value v2 = pop();
		push(pop().lt(v2));		
	}
	
	public void add() {
		push(pop().add(pop()));
	}
	
	public void subtract() {
		Value v2 = pop();
		push(pop().subtract(v2));
	}
	
	public void mult() {
		push(pop().mult(pop()));
	}
	
	public void div() {
		Value v2 = pop();
		push(pop().div(v2));		
	}
	
	public void not() {
		push(pop().not());
	}
	
	public void unary_plus() {
		push(pop().unary_plus());
	}
	
	public void unary_minus() {
		push(pop().unary_minus());
	}
	
	public void push_integer(String s) {
		push(new ValueInteger(Integer.parseInt(s)));
	}
	
	public void push_floating(String s) {
		throw new ExceptionSemantic("Floating point values are not implemented yet.");
	}
	
	public void push_boolean(boolean b) {
		push(new ValueBoolean(b));
	}
}
