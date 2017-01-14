package uk.ac.derby.ldi.slip.engine;

import java.io.IOException;
import java.util.Stack;

import uk.ac.derby.ldi.slip.exceptions.*;
import uk.ac.derby.ldi.slip.values.*;

public class Parser {
	
	private class ParseList {
		private Bunch list;
		private long startLine;
		private long startColumn;
		public ParseList(long line, long column) {
			startLine = line;
			startColumn = column;
			list = new Bunch();
		}
		public long getStartLine() {
			return startLine;
		}
		public long getStartColumn() {
			return startColumn;
		}
		public Bunch getList() {
			return list;
		}
		public void insert(Value o) {
			list.insert(o);
		}
		public String toString() {
			return "<" + startLine + ":" + startColumn + "> " + list.toString();
		}
	}
	
	private Stack<ParseList> stack;
	private Lexer lexer;

	public Parser(Lexer lex) {
		lexer = lex;
		stack = new Stack<ParseList>();		
	}
	
	public void beginList() {
		stack.push(new ParseList(lexer.getLineNumber(), lexer.getColumnNumber()));
	}
	
	public void addToList(Value v) {
		stack.peek().insert(v);
	}
	
	public void endList() {
		addToList(stack.pop().getList());
	}
	
	/** Given a token, convert it to a Value equivalent. */
	public Value tokenToValue(String token) {
		if (token.charAt(0) == '"')
			return new Str(token.substring(1, token.length() - 1));
		try {
			return new Int(Long.parseLong(token));
		} catch (NumberFormatException nfeLong) {
			try {
				return new Rational(Double.parseDouble(token));
			} catch (NumberFormatException nfeDouble) {
				if (token.equalsIgnoreCase("true"))
					return new Bool(true);
				else if (token.equalsIgnoreCase("false"))
					return new Bool(false);
				else
					return new Identifier(token);
			}
		}
	}
	
	/** Parse the stream, converting it to an evaluatable list. */
	public Bunch parse() throws IOException {
		String token;
		beginList();
		while ((token = lexer.getToken()) != null) {
			if (token.equals("("))
				beginList();
			else if (token.equals(")")) {
				if (stack.size() == 1)
					throw new Fatal("Too many ')'s.", lexer.getLineNumber(), lexer.getColumnNumber());
				endList();
			} else {
				if (stack.size() == 1)
					throw new Fatal("You need to start a list with '(' first.", lexer.getLineNumber(), lexer.getColumnNumber());
				addToList(tokenToValue(token));
			}
		}
		// Unterminated list?
		if (stack.size() > 1) {
			StringBuffer unterminatedListMessage = new StringBuffer();
			if (stack.size() == 2) {
				ParseList plist = stack.pop();
				unterminatedListMessage.append("Unterminated list started at line " + plist.getStartLine() + " column " + plist.getStartColumn());
			} else {
				unterminatedListMessage.append("Unterminated lists:");
				while (stack.size() > 1) {
					ParseList plist = stack.pop();
					unterminatedListMessage.append((char)Character.LINE_SEPARATOR);
					unterminatedListMessage.append(" Started at line " + plist.getStartLine() + " column " + plist.getStartColumn());
				}
			}
			stack.pop();
			throw new Fatal(unterminatedListMessage.toString());
		}
		return stack.pop().getList();
	}

}
