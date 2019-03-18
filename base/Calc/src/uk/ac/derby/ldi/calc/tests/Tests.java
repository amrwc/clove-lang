package uk.ac.derby.ldi.calc.tests;

import uk.ac.derby.ldi.calc.calculator.*;

public class Tests {

	private static boolean testValueBoolean() {
		ValueBoolean t = new ValueBoolean(true);
		ValueBoolean f = new ValueBoolean(false);
		if (t.booleanValue() != true) {
			System.out.println("ValueBoolean(true) is not true!");
			return false;
		}
		if (f.booleanValue() != false) {
			System.out.println("ValueBoolean(false) is not false!");
			return false;
		}
		if (!t.eq(t).booleanValue()) {
			System.out.println("ValueBoolean is not equal to itself!");
			return false;
		}
		if (!t.neq(f).booleanValue()) {
			System.out.println("ValueBoolean(true) equals ValueBoolean(false)!");
			return false;
		}
		if (!t.gt(f).booleanValue()) {
			System.out.println("ValueBoolean(true) < ValueBoolean(false)!");
			return false;
		}
		return true;
	}
	
	private static boolean testValueInteger() {
		ValueInteger one = new ValueInteger(1);
		ValueInteger negfive = new ValueInteger(-5);
		ValueInteger ten = new ValueInteger(10);
		if (one.intValue() != 1) {
			System.out.println("ValueInteger(1) is not equal to 1!");
			return false;
		}
		if (ten.subtract(one).intValue() != 9) {
			System.out.println("ValueInteger(10) - ValueInteger(1) != 9!");
			return false;
		}
		if (ten.div(negfive).intValue() != -2) {
			System.out.println("ValueInteger(10) / ValueInteger(-5) != -2!");
			return false;
		}
		if (!ten.gt(one).booleanValue()) {
			System.out.println("ValueInteger(10) <= ValueInteger(1)!");
			return false;
		}
		return true;
	}
	
	private static boolean testCalculator() {
		Calculator calc = new Calculator();
		calc.push_integer("10");
		calc.push_integer("2");
		calc.add();
		if (calc.pop().intValue() != 12) {
			System.out.println("Calculator can't do 10 + 2!");
			return false;
		}
		calc.push_integer("10");
		calc.push_integer("2");
		calc.subtract();
		if (calc.pop().intValue() != 8) {
			System.out.println("Calculator can't do 10 - 2!");
			return false;
		}
		calc.push_integer("10");
		calc.push_integer("2");
		calc.div();
		if (calc.pop().intValue() != 5) {
			System.out.println("Calculator can't do 10 / 2!");
			return false;
		}
		return true;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (!testValueBoolean()) {
			System.out.println("ValueBoolean failed.");
			System.exit(1);
		}
		if (!testValueInteger()) {
			System.out.println("ValueInteger failed.");
			System.exit(1);
		}
		if (!testCalculator()) {
			System.out.println("Calculator failed.");
			System.exit(1);
		}
		System.out.println("Tests Pass.");
		System.exit(0);
	}

}
