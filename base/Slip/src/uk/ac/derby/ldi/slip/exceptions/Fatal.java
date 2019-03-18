package uk.ac.derby.ldi.slip.exceptions;

/**
 * This exception is thrown when fatal errors are encountered.
 */
public class Fatal extends Error {

	static final long serialVersionUID = 0;
	static final long line = -1;
	static final long column = -1;
	
	public Fatal(String message) {
		super(message);
	}

	public Fatal(String message, long lineNumber, long columnNumber) {
		super("Line " + lineNumber + " column " + columnNumber + ": " + message);
	}
	
	public long getLineNumber() {
		return line;
	}
	
	public long getColumnNumber() {
		return column;
	}
	
}
