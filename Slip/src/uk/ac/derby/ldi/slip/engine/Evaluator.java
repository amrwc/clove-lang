package uk.ac.derby.ldi.slip.engine;

import java.io.*;

import uk.ac.derby.ldi.slip.operators.*;
import uk.ac.derby.ldi.slip.values.*;

public class Evaluator {

	private static Resolver resolver;
	
	static {
		resolver = new RootResolver();
		resolver.put(new Quote());
		resolver.put(new Or());
		resolver.put(new And());
		resolver.put(new Not());
		resolver.put(new LessThan());
		resolver.put(new LessThanOrEquals());
		resolver.put(new GreaterThan());
		resolver.put(new GreaterThanOrEquals());
		resolver.put(new Equals());
		resolver.put(new NotEquals());
		resolver.put(new Plus());
		resolver.put(new Subtract());
		resolver.put(new Mult());
		resolver.put(new Div());
		resolver.put(new If());
		resolver.put(new Put());
		resolver.put(new Sput());
		resolver.put(new Fun());
		resolver.put(new Prog());
		resolver.put(new Cond());
		resolver.put(new Let());
		resolver.put(new Set());				
	};
	
	public static Value eval(Bunch l) throws IOException {
		return l.evaluate(resolver);
	}
	
	public static Value eval(String s) throws IOException {
		return eval((new Parser(new Lexer(new StringReader(s)))).parse());
	}
	
	public static void eval(Reader input, PrintStream output) throws IOException {
		Parser p = new Parser(new Lexer(input));
		while (true)
			eval(p.parse());
	}
	
}
