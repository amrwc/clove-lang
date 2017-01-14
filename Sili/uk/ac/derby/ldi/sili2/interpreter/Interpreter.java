package uk.ac.derby.ldi.sili2.interpreter;

import uk.ac.derby.ldi.sili2.parser.*;

public class Interpreter {
	
	private static void usage() {
		System.out.println("Usage: sili [-d1] < <source>");
		System.out.println("          -d1 -- output AST");
	}
	
	public static void main(String args[]) {
		boolean debugAST = false;
		if (args.length == 1) {
			if (args[0].equals("-d1"))
				debugAST = true;
			else {
				usage();
				return;
			}
		}
		Sili language = new Sili(System.in);
		try {
			ASTCode parser = language.code();
			SiliVisitor nodeVisitor;
			if (debugAST)
				nodeVisitor = new ParserDebugger();
			else
				nodeVisitor = new Parser();
			parser.jjtAccept(nodeVisitor, null);
		} catch (Throwable e) {
			System.out.println(e.getMessage());
		}
	}
}
