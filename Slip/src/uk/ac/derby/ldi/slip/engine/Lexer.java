package uk.ac.derby.ldi.slip.engine;

import java.io.*;

import uk.ac.derby.ldi.slip.exceptions.*;

public class Lexer {
	
	private final int TAB = 9;
	private final int LF = 10;
	private final int CR = 13;

	private Reader stream;
	private char pushbackChar;
	private char[] buffer;
	private long lineNumber;
	private long columnNumber;
	private String specialTokens;

	/** Create a Lexer given the Reader to read and a string of special
	 * character tokens.
	 * 
	 * @param inputStream
	 * @param charTokens
	 */
	public Lexer(Reader inputStream, String charTokens) {
		stream = inputStream;
		pushbackChar = 0;
		buffer = new char[1];
		lineNumber = 0;
		columnNumber = 0;
		specialTokens = charTokens;
	}
	
	/** Create a Lexer given the Reader to read, and use the standard
	 * set of special character tokens.
	 * 
	 * @param inputStream
	 */
	public Lexer(Reader inputStream) {
		this(inputStream, "(),");
	}

	public void setSpecialTokens(String s) {
		specialTokens = s;
	}
	
	public String getSpecialTokens() {
		return specialTokens;
	}
	
	/** Get current line number. */
	public long getLineNumber() {
		return lineNumber;
	}
	
	/** Get current column number. */
	public long getColumnNumber() {
		return columnNumber;
	}
	
	/** Obtain next character in stream.  Return 0 if stream is terminated. */
	public char getNextCharRaw() throws IOException {
		if (pushbackChar > 0)
			try {
				return pushbackChar;
			} finally {
				pushbackChar = 0;
			}
		if (stream.read(buffer) != 1)
			return 0;
		char currentChar = buffer[0];
		if (currentChar == Character.LINE_SEPARATOR) {
			lineNumber++;
			columnNumber = 0;
		} else
			columnNumber++;
		return currentChar;
	}

	/** Push a character back into the stream. */
	public void pushback(char c) {
		pushbackChar = c;
	}
	
	/** Obtain the next character in the stream, skipping any canonical Java-style comments.
	 * Return 0 if the stream is terminated. */
	public char getNextChar() throws IOException {
		char firstChar = getNextCharRaw();
		if (firstChar != '/')
			return firstChar;
		char secondChar = getNextCharRaw();
		if (secondChar == '/') {
			// Line comment
			while (true) {
				firstChar = getNextCharRaw();
				if (firstChar == Character.LINE_SEPARATOR || firstChar == 0)
					return firstChar;
			}
		} else if (secondChar == '*') {
			// Block comment
			long startLine = getLineNumber();
			long startColumn = getColumnNumber();
			while (true) {
				firstChar = getNextCharRaw();
				if (firstChar == '*') {
					firstChar = getNextCharRaw();
					if (firstChar == '/')
						return getNextCharRaw();
				}
				if (firstChar == 0)
					throw new Fatal("Comment started at line " + startLine + " column " + startColumn + " is unterminated.", getLineNumber(), getColumnNumber());
			}
		} else {
			pushback(secondChar);
			return firstChar;
		}
	}
	
	// If there's a string, read it, given its first character, the buffer to write it to, 
	// and the specified delimiter.
	// Return true if a string was read, false if no string was found.
	private final boolean maybeReadString(char currentChar, StringBuffer buffer, char delimiter) throws IOException {
		if (!(currentChar == delimiter))
			return false;
		buffer.append('"');
		long startLine = getLineNumber();
		long startColumn = getColumnNumber();
		while (true) {
			if ((currentChar = getNextChar()) == 0)
				throw new Fatal("String started at line " + startLine + " column " + startColumn + " is unterminated.", getLineNumber(), getColumnNumber());
			if (currentChar == '\\') { 
				// escape
				if ((currentChar = getNextChar()) == 0)
					throw new Fatal("String started at line " + startLine + " column " + startColumn + " is unterminated.", getLineNumber(), getColumnNumber());
				switch (currentChar) {
				case 't':
					buffer.append((char)TAB);
					break;
				case 'n':
					buffer.append((char)LF);
					break;
				case 'r':
					buffer.append((char)CR);
					break;
				case '\\':
					buffer.append('\\');
					break;
				default:
					// Escaped 3-digit number to directly represent ASCII value
					if (Character.isDigit(currentChar)) {
						char[] numberBuffer = new char[3];
						numberBuffer[0] = currentChar;
						for (int i=1; i<3; i++) {
							if ((currentChar = getNextChar()) == 0 || !Character.isDigit(currentChar))
								throw new Fatal("Invalid numeric escape.", getLineNumber(), getColumnNumber());
							numberBuffer[i] = currentChar;
						}
						buffer.append(Character.toChars(Integer.parseInt(new String(numberBuffer))));
					} else
						buffer.append(currentChar);
				}
			} else {
				if (currentChar == delimiter) {
					buffer.append('"');
					break;
				}
				buffer.append(currentChar);
			}
		}
		return true;
	}
		
	/** Fetch the next token from the input stream.
	    Return null if the input stream is empty. */
	public String getToken() throws IOException {
		char currentChar;
		// Pass over leading whitespace
		while (true) {
			if ((currentChar = getNextChar()) == 0)
				return null;
			if (!Character.isWhitespace(currentChar))
				break;
		}
		// Special token?
		int specialTokenPosition = specialTokens.indexOf(currentChar);
		if (specialTokenPosition != -1)
			return (new Character(specialTokens.charAt(specialTokenPosition))).toString();
		// Append current character to the token being built
		StringBuffer buffer = new StringBuffer();
		// Append remaining characters to the token being built
		if (!(maybeReadString(currentChar, buffer, '"') || 
			  maybeReadString(currentChar, buffer, '\''))) {
			while (true) { 
				buffer.append(currentChar);
				if ((currentChar = getNextChar()) == 0 || Character.isWhitespace(currentChar))
					break;
				// Special token?
				specialTokenPosition = specialTokens.indexOf(currentChar);
				if (specialTokenPosition != -1) {
					pushback(specialTokens.charAt(specialTokenPosition));
					break;
				}
			}
		}
		return buffer.toString();
	}
	
}
