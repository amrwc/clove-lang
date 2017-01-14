package uk.ac.derby.ldi.slip.tests;

import junit.framework.TestCase;
import uk.ac.derby.ldi.slip.values.*;

public class TestValues extends TestCase {

	/* Test lists. */
	public void testValueList() {
		Bunch v = new Bunch();
		v.insert(new Str("union"));
		v.insert(new Int(3));
		v.insert(new Int(4));
		Walker i = v.getWalker();
		Value p = i.next();
		assertEquals("union", p.toString());
		p = i.next();
		assertEquals("3", p.toString());
		p = i.next();
		assertEquals("4", p.toString());		
		String s = v.toString();
		assertEquals("(union 3 4)", s);
	}
	
	public void testValueInteger() {
		Int one = new Int(1);
		Int five = new Int(5);
		Int nFive = new Int(-5);
		assertEquals("1 + 5", 6, one.add(five).longValue());
		assertEquals("1 + -5", -4, one.add(nFive).longValue());
		assertEquals("-5 * 5", -25, nFive.mult(five).longValue());
	}
	
	public void testValueBoolean() {
		Bool t = new Bool(true);
		Bool f = new Bool(false);
		assertEquals("true != false", true, t.neq(f).booleanValue());
		assertEquals("true or false", true, t.or(f).booleanValue());
		assertEquals("true and false", false, t.and(f).booleanValue());
	}
	
	public void testValueDouble() {
		Rational onePointTwoFive = new Rational(1.25);
		Rational fivePointThree = new Rational(5.3);
		Rational nFivePointFourEight = new Rational(-5.48);
		assertEquals("1.25 + 5.3", 6.55, onePointTwoFive.add(fivePointThree).doubleValue());
		assertEquals("1.25 + -5.48", -4.23, onePointTwoFive.add(nFivePointFourEight).doubleValue());
		assertEquals("-5.48 * 5.3", -29.044, nFivePointFourEight.mult(fivePointThree).doubleValue());
	}

}
