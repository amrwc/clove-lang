package uk.ac.derby.ldi.silc.compiler;

/** This is the base class for every AST node.  
 * 
 * tokenValue contains the actual value from which the token was constructed.
 * 
 * ifHasElse is set at parse-time to indicate to the compiler whether or not an IF clause has an ELSE.
 * 
 * fnHasReturn is set at parse-time to indicate to the compiler whether or not a function definition 
 * has a RETURN.
 * 
 * @author dave
 *
 */
public class BaseASTNode {
	public String tokenValue = null;
	public boolean ifHasElse = false;
	public boolean fnHasReturn = false;
}
