package interpreter;

import parser.ast.ASTCode;
import parser.ast.Dumb;
import parser.ast.DumbVisitor;

public class Interpreter {

	private static void usage() {
		System.out.println("\nUsage: Dumb [flags] < <file_name>\n"
			+ "\nFlags:\n"
			+ "\t-d1: debug; print the full AST.\n"
			+ "\t-h, --help: print this message.\n");
	}

	public static void main(String args[]) {
		boolean debugAST = args.length == 1 && args[0].equals("-d1");

		// Print the help message.
		if (args.length == 1 && (args[0].equals("-h") || args[0].equals("--help"))) {
			usage();
			return;
		}

		Dumb language = new Dumb(System.in);
		try {
			ASTCode parser = language.code();
			DumbVisitor nodeVisitor;
			if (debugAST)
				nodeVisitor = new ParserDebugger();
			else
				nodeVisitor = new Parser(args);
			parser.jjtAccept(nodeVisitor, null);
		} catch (Throwable e) {
			System.out.println(e.getMessage());
		}
	}
}
