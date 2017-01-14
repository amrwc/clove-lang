package uk.ac.derby.ldi.calcast.calculator;
/**
 * This exception is thrown when semantic errors are encountered.
 */
public class ExceptionSemantic extends Error {

	static final long serialVersionUID = 0;
	
	public ExceptionSemantic(String message) {
		super(message);
	}

}
