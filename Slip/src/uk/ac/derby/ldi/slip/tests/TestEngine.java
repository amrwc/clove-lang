package uk.ac.derby.ldi.slip.tests;

import junit.framework.TestCase;
import uk.ac.derby.ldi.slip.engine.*;
import uk.ac.derby.ldi.slip.values.*;

import java.io.*;

public class TestEngine extends TestCase {
	
	public void testLexer() throws IOException {
		String s = "This is  a test   of  the tokenizer\nLine two\nLine three";
		Lexer p = new Lexer(new StringReader(s));
		assertEquals("This", p.getToken());
		assertEquals("is", p.getToken());
		assertEquals("a", p.getToken());
		assertEquals("test", p.getToken());
		assertEquals("of", p.getToken());
		assertEquals("the", p.getToken());
		assertEquals("tokenizer", p.getToken());
		assertEquals("Line", p.getToken());
		assertEquals("two", p.getToken());
		assertEquals("Line", p.getToken());
		assertEquals("three", p.getToken());
		assertEquals(null, p.getToken());
	}
	
	public void testListIterator() throws IOException {
		String s = "(blah 5 10 -1)";
		Parser p = new Parser(new Lexer(new StringReader(s)));
		Walker l = ((Bunch)p.parse().getHead().getItem()).getWalker();
		assertEquals(l.hasNext(), true);
		assertEquals("blah", l.next().toString());
		assertEquals(l.hasNext(), true);
		assertEquals("5", l.next().toString());
		assertEquals(l.hasNext(), true);
		assertEquals("10", l.next().toString());
		assertEquals(l.hasNext(), true);
		assertEquals("-1", l.next().toString());
		assertEquals(l.hasNext(), false);
	}
	
	public void testCode() throws IOException {
		String s = "(prog (put 'prog1\\n') (put 'prog2\\n') (put 'prog3\\n'))";
		Evaluator.eval(s);
	}
	
	public void testParser0() throws IOException {
		String s = "(+ 3 4)";
		Parser p = new Parser(new Lexer(new StringReader(s)));
		assertEquals("(" + s + ")", p.parse().toString());
	}
	
	public void testParser1() throws IOException {
		String s = "(+ 3 4 (5 (7 8) 6)) // End of line comment";
		Parser p = new Parser(new Lexer(new StringReader(s)));
		assertEquals("((+ 3 4 (5 (7 8) 6)))", p.parse().toString());
	}

	public void testParser2() throws IOException {
		String s = "(+ 3 4 /* inline comment */ \"this " + "\\" + "\"" + " is \\065\\064 string\" (5 6))";
		Parser p = new Parser(new Lexer(new StringReader(s)));
		assertEquals("((+ 3 4 this \" is A@ string (5 6)))", p.parse().toString());
	}

	public void testEngine0() throws IOException {
		String s = "((+ 3.0 4.0 (+ 5.2 6.0)))";
		Value v = Evaluator.eval(s);
		assertEquals("18.2", v.toString());
	}
	
	public void testEngine1() throws IOException {
		String s = "(if (< 1 2 3 5) 1 2)";
		Value v = Evaluator.eval(s);
		assertEquals("1", v.toString());		
	}

	public void testEngine2() throws IOException {
		String s = "(if (< 2 1 10) 1 2)";
		Value v = Evaluator.eval(s);
		assertEquals("2", v.toString());		
	}
	
	public void testEngine3() throws IOException {
		String s = "(put 1 2 3 \"fish\" (quote 5 6 (7 8)) \"\\n\" \"Next Line?\")";
		Evaluator.eval(s);
	}

	public void testEngine4() throws IOException {
		String s = "(- 4.0 3.0 (- 6.0 2.0))";
		Value v = Evaluator.eval(s);
		assertEquals("-3.0", v.toString());
	}
	
	public void testEngine5() throws IOException {
		String s = "(sput 1 2 3 \"fish\" (quote 5 6 (7 8)) \"\\n\" \"Next Line?\")";
		Value v = Evaluator.eval(s);
		assertEquals("123fish(5 6 (7 8))\nNext Line?", v.toString());
	}

	public void testEngine6() throws IOException {
		String s = "(* 4.0 3.0 (* 6.0 2.0))";
		Value v = Evaluator.eval(s);
		assertEquals("144.0", v.toString());
	}

	public void testEngine7() throws IOException {
		String s = "(/ 12.0 2.0 (/ 6.0 2.0))";
		Value v = Evaluator.eval(s);
		assertEquals("2.0", v.toString());
	}

	public void testEngine8() throws IOException {
		String s = "(> 10 5 -1)";
		Value v = Evaluator.eval(s);
		assertEquals("true", v.toString());
	}
	
	public void testEngine9() throws IOException {
		String s = "(> 5 10 -1)";
		Value v = Evaluator.eval(s);
		assertEquals("false", v.toString());		
	}

	public void testEngine10() throws IOException {
		String s = "(>= 10 10 5 -1)";
		Value v = Evaluator.eval(s);
		assertEquals("true", v.toString());		
	}
	
	public void testEngine11() throws IOException {
		String s = "(>= 9 10 -1)";
		Value v = Evaluator.eval(s);
		assertEquals("false", v.toString());		
	}

	public void testEngine12() throws IOException {
		String s = "(<= -1 5 10 10)";
		Value v = Evaluator.eval(s);
		assertEquals("true", v.toString());		
	}
	
	public void testEngine13() throws IOException {
		String s = "(<= 9 10 -1)";
		Value v = Evaluator.eval(s);
		assertEquals("false", v.toString());		
	}

	public void testEngine14() throws IOException {
		String s = "(= 10 10 10)";
		Value v = Evaluator.eval(s);
		assertEquals("true", v.toString());		
	}
	
	public void testEngine15() throws IOException {
		String s = "(= 9 10 -1)";
		Value v = Evaluator.eval(s);
		assertEquals("false", v.toString());		
	}

	public void testEngine16() throws IOException {
		String s = "(!= 10 10 10)";
		Value v = Evaluator.eval(s);
		assertEquals("false", v.toString());		
	}
	
	public void testEngine17() throws IOException {
		String s = "(!= 9 10 -1)";
		Value v = Evaluator.eval(s);
		assertEquals("true", v.toString());		
	}

	public void testEngine18() throws IOException {
		String s = "(or false false true)";
		Value v = Evaluator.eval(s);
		assertEquals("true", v.toString());		
	}
	
	public void testEngine19() throws IOException {
		String s = "(or false false false)";
		Value v = Evaluator.eval(s);
		assertEquals("false", v.toString());		
	}
	
	public void testEngine20() throws IOException {
		String s = "(and true true true)";
		Value v = Evaluator.eval(s);
		assertEquals("true", v.toString());		
	}
	
	public void testEngine21() throws IOException {
		String s = "(and true true false)";
		Value v = Evaluator.eval(s);
		assertEquals("false", v.toString());		
	}
	
	public void testEngine22() throws IOException {
		String s = "(quote blah blat zorg)";
		Value v = Evaluator.eval(s);
		assertEquals("(blah blat zorg)", v.toString());
	}
	
	public void testEngine23() throws IOException {
		String s = "(not true)";
		Value v = Evaluator.eval(s);
		assertEquals("false", v.toString());
	}
	
	public void testEngine24() throws IOException {
		String s = "(not false)";
		Value v = Evaluator.eval(s);
		assertEquals("true", v.toString());
	}
	
	public void testEngine25() throws IOException {
		String s = "(not false true false)";
		Value v = Evaluator.eval(s);
		assertEquals("(true false true)", v.toString());
	}
	
	public void testEngine26() throws IOException {
		String s = "((sput 'blah'))";
		Value v = Evaluator.eval(s);
		assertEquals("blah", v.toString());
	}
	
	public void testEngine27() throws IOException {
		String s = "((fun (p1 p2 p3) " +
						"(sput (+ p2 p1 p3))) 'blah' 'blat' 'zot')";
		Value v = Evaluator.eval(s);
		assertEquals("blatblahzot", v.toString());
	}
	
	public void testEngine28() throws IOException {
		String s = "(cond " +
						"((> 3 4) 'First') " +
						"((< 3 4) 'Second'))";
		Value v = Evaluator.eval(s);
		assertEquals("Second", v.toString());		
	}
	
	public void testEngine29() throws IOException {
		String s = "(cond " +
						"((> 5 4) 'First') " +
						"((< 3 4) 'Second'))";
		Value v = Evaluator.eval(s);
		assertEquals("First", v.toString());		
	}
	
	public void testEngine30() throws IOException {
		String s = "(let (" +
						"(a 1) " +
						"(b 2) " +
						"(c)) " +
							"(sput (+ '' a b c)))";
		Value v = Evaluator.eval(s);
		assertEquals("12nil", v.toString());		
	}
	
	public void testEngine31() throws IOException {
		String s = "(prog " +
						"(set x (+ 2 3)) " +
						"(put x) " +
						"(x))";
		Value v = Evaluator.eval(s);
		assertEquals("5", v.toString());
	}
		
	public void testEngine32() throws IOException {
		String s = "(prog " +
						"(set (+ 'x' 'y') (+ 2 3) " +
						"     z 3) " +
						"(sput xy z))";
		Value v = Evaluator.eval(s);
		assertEquals("53", v.toString());
	}

	public void testEngine33() throws IOException {
		String s = "(let (" +
							"(a 1) " +
							"(b 2) " +
							"(c)) " +
								"(set b 3) " +
								"(sput (+ '' a b c)))";
		Value v = Evaluator.eval(s);
		assertEquals("13nil", v.toString());
	}

	public void testEngine34() throws IOException {
		String s = "(prog " +
						"(set a (fun (n) (+ 1 n))) " +
						"(a 3))";
		Value v = Evaluator.eval(s);
		assertEquals("4", v.toString());
	}
	
}
