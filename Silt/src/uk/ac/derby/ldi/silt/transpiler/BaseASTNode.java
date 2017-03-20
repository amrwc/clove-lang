package uk.ac.derby.ldi.silt.transpiler;

import uk.ac.derby.ldi.silt.parser.ast.Token;

/** This is the base class for every AST node.  
 * 
 * tokenValue contains the actual value from which the token was constructed.
 * 
 * ifHasElse is set at parse-time to indicate to the compiler whether or not an IF clause has an ELSE.
 * 
 * @author dave
 *
 */
public class BaseASTNode {
	public String tokenValue = null;
	public boolean ifHasElse = false;
	public Token first_token;
	public Token last_token;	
}
