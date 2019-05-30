package interpreter;

/**
 * This is the base class for every AST node.
 * 
 * @author dave
 * @author amrwc
 */
public class BaseASTNode {
	// The actual source code from which the token was constructed. Only set on
	// literals, etc.
	public String tokenValue = null;

	// Set at parse-time in an IF ... ELSE construct to indicate to the compiler
	// or interpreter whether or not an IF clause has an ELSE.
	public boolean ifHasElse = false;

	// Set at parse-time in a function definition to indicate whether or not the
	// function
	// has a return value.
	public boolean fnHasReturn = false;

	// References an object that optimises execution of the node. For example, it
	// might
	// reference a compiled function definition, so that the function needn't be
	// redefined
	// on every execution.
	public Object optimised = null;

	/**
	 * Stores the short operator used in the node. E.g. '+=', '++', etc.
	 */
	public String shorthandOperator = null;

	/**
	 * Stores definition type ("variable"|"constant")
	 */
	public String defType = null;

	/**
	 * Tells whether it's a declaration of an empty array with 0 capacity.
	 */
	public boolean isArrayDeclaration = false;

	/**
	 * Tells whether the declaration/definition is an array with an explicitly
	 * stated capacity.
	 */
	public boolean isArrayWithCap = false;
}
