package interpreter;

import interpreter.Display.Reference;
import parser.ast.*;
import values.*;

public class Parser implements DumbVisitor {
	
	// Scope display handler
	private Display scope = new Display();
	
	// Get the ith child of a given node.
	private static SimpleNode getChild(SimpleNode node, int childIndex) {
		return (SimpleNode)node.jjtGetChild(childIndex);
	}
	
	// Get the token value of the ith child of a given node.
	private static String getTokenOfChild(SimpleNode node, int childIndex) {
		return getChild(node, childIndex).tokenValue;
	}
	
	// Execute a given child of the given node
	private Object doChild(SimpleNode node, int childIndex, Object data) {
		return node.jjtGetChild(childIndex).jjtAccept(this, data);
	}
	
	// Execute a given child of a given node, and return its value as a Value.
	// This is used by the expression evaluation nodes.
	Value doChild(SimpleNode node, int childIndex) {
		return (Value)doChild(node, childIndex, null);
	}
	
	// Execute all children of the given node
	Object doChildren(SimpleNode node, Object data) {
		return node.childrenAccept(this, data);
	}
	
	// Called if one of the following methods is missing...
	public Object visit(SimpleNode node, Object data) {
		System.out.println(node + ": acceptor not implemented in subclass?");
		return data;
	}
	
	// Execute a Dumb program
	public Object visit(ASTCode node, Object data) {
		return doChildren(node, data);	
	}
	
	// Execute a statement
	public Object visit(ASTStatement node, Object data) {
		return doChildren(node, data);	
	}

	// Execute a block
	public Object visit(ASTBlock node, Object data) {
		return doChildren(node, data);	
	}

	/**
	 * Anonymous object declaration.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTValueObject node, Object data) {
		// Already defined?
		if (node.optimised != null)
			return data;

		ValueObject valueObject = new ValueObject();

		// Add all the key-value pairs to the anonymous object.
		int keyCount = node.jjtGetNumChildren();
		SimpleNode currentKey;
		String keyName;
		Value value;
		for (int i = 0; i < keyCount; i++) {
			currentKey = (SimpleNode) node.jjtGetChild(i);
			keyName = getTokenOfChild(currentKey, 0);
			value = doChild(currentKey, 1);
			valueObject.add(keyName, value);
		}

		node.optimised = valueObject;
		return node.optimised;
	}
	
	/**
	 * List declaration.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTValueList node, Object data) {
		// Already defined?
		if (node.optimised != null)
			return data;
		
		ValueList valueList = new ValueList();

		// Add all the values to the list.
		int keyCount = node.jjtGetNumChildren();
		Value currentValue;
		for (int i = 0; i < keyCount; i++) {
			currentValue = doChild(node, i);
			valueList.append(currentValue);
		}

		node.optimised = valueList;
		return node.optimised;
	}

	/**
	 * Anonymous function declaration.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTFnVal node, Object data) {
		// Already defined?
		if (node.optimised != null)
			return data;

		FunctionDefinition currentFnDef = new FunctionDefinition(scope.getLevel() + 1);

		// Child 0 -- function definition parameter list
		doChild(node, 0, currentFnDef);
		// Child 1 -- function body
		currentFnDef.setFunctionBody(getChild(node, 1));
		// Child 2 -- optional return expression
		if (node.fnHasReturn)
			currentFnDef.setFunctionReturnExpression(getChild(node, 2));

		ValueFn valueFunction = new ValueFn(currentFnDef);

		// Preserve this definition for future reference, and so we don't define
		// it every time this node is processed.
		node.optimised = valueFunction;

		return node.optimised;
	}

	// Function definition
	public Object visit(ASTFnDef node, Object data) {
		// Already defined?
		if (node.optimised != null)
			return data;
		// Child 0 - identifier (fn name)
		String fnname = getTokenOfChild(node, 0);
		if (scope.findFunctionInCurrentLevel(fnname) != null)
			throw new ExceptionSemantic("Function " + fnname + " already exists.");
		FunctionDefinition currentFunctionDefinition = new FunctionDefinition(fnname, scope.getLevel() + 1);
		// Child 1 - function definition parameter list
		doChild(node, 1, currentFunctionDefinition);
		// Add to available functions
		scope.addFunction(currentFunctionDefinition);
		// Child 2 - function body
		currentFunctionDefinition.setFunctionBody(getChild(node, 2));
		// Child 3 - optional return expression
		if (node.fnHasReturn)
			currentFunctionDefinition.setFunctionReturnExpression(getChild(node, 3));
		// Preserve this definition for future reference, and so we don't define
		// it every time this node is processed.
		node.optimised = currentFunctionDefinition;
		return data;
	}
	
	// Function definition parameter list
	public Object visit(ASTParmlist node, Object data) {
		FunctionDefinition currentDefinition = (FunctionDefinition)data;
		for (int i=0; i<node.jjtGetNumChildren(); i++)
			currentDefinition.defineParameter(getTokenOfChild(node, i));
		return data;
	}
	
	// Function body
	public Object visit(ASTFnBody node, Object data) {
		return doChildren(node, data);
	}
	
	// Function return expression
	public Object visit(ASTReturnExpression node, Object data) {
		return doChildren(node, data);
	}
	
	/**
	 * Try finding the ValueFn inside the scope and extract its FunctionDefinition.
	 * 
	 * @author amrwc
	 */
	public FunctionDefinition findValueFn(String fnname) {
		Reference value = scope.findReference(fnname);
		if (value == null)
			throw new ExceptionSemantic("Function " + fnname + " is undefined.");

		ValueFn valueFunction = (ValueFn) value.getValue();
		return valueFunction.get(); // Extract the FunctionDefinition stored in ValueFn.
	}

	/**
	 * Function call.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTCall node, Object data) {
		FunctionDefinition fndef;
		if (node.optimised == null) { 
			// Child 0 - identifier (fn name)
			String fnname = getTokenOfChild(node, 0);
			fndef = scope.findFunction(fnname);

			if (fndef == null) fndef = findValueFn(fnname);

			// Save it for next time
			node.optimised = fndef;
		} else
			fndef = (FunctionDefinition)node.optimised;

		FunctionInvocation newInvocation = new FunctionInvocation(fndef);
		// Child 1 - arglist
		doChild(node, 1, newInvocation);
		// Execute
		scope.execute(newInvocation, this);
		return data;
	}

	/**
	 * Function invocation in an expression.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTFnInvoke node, Object data) {
		FunctionDefinition fndef;
		int leftNumChildren = node.jjtGetChild(0).jjtGetNumChildren();

		// NOTE: This locates ValueFn inside of ValueObject.
		// 		 Only works after modifying grammar to dereference() arglist(). 
		// If there's more than 1 child in the left child, it's an object.
		// NOTE: It needs to be added to FnCall as well when it works properly.
		if (leftNumChildren > 0) {
			Value value = doChild(node, 0); // Do the dereference.

			ValueFn valueFunction = (ValueFn) value;
			if (valueFunction == null)
				throw new ExceptionSemantic("The value function you are trying to invoke is undefined.");

			fndef = scope.findFunctionInCurrentLevel(valueFunction.getName());
			if (fndef == null)
				throw new ExceptionSemantic("Function " + valueFunction.getName() + " is undefined.");

			node.optimised = fndef;
		}

		if (node.optimised == null) {
			String fnname = getTokenOfChild(node, 0);
			fndef = scope.findFunction(fnname);

			if (fndef == null) fndef = findValueFn(fnname);

			if (!fndef.hasReturn())
				throw new ExceptionSemantic("Function " + fnname + " is being invoked in an expression but does not have a return value.");

			// Save it for next time
			node.optimised = fndef;
		} else
			fndef = (FunctionDefinition)node.optimised;

		FunctionInvocation newInvocation = new FunctionInvocation(fndef);
		// Child 1 - arglist
		doChild(node, 1, newInvocation);
		// Execute
		return scope.execute(newInvocation, this);
	}
	
	// Function invocation argument list.
	public Object visit(ASTArgList node, Object data) {
		FunctionInvocation newInvocation = (FunctionInvocation)data;
		for (int i=0; i<node.jjtGetNumChildren(); i++)
			newInvocation.setArgument(doChild(node, i));
		newInvocation.checkArgumentCount();
		return data;
	}
	
	// Execute an IF 
	public Object visit(ASTIfStatement node, Object data) {
		// evaluate boolean expression
		Value hopefullyValueBoolean = doChild(node, 0);
		if (!(hopefullyValueBoolean instanceof ValueBoolean))
			throw new ExceptionSemantic("The test expression of an if statement must be boolean.");
		if (((ValueBoolean)hopefullyValueBoolean).booleanValue())
			doChild(node, 1);							// if(true), therefore do 'if' statement
		else if (node.ifHasElse)						// does it have an else statement?
			doChild(node, 2);							// if(false), therefore do 'else' statement
		return data;
	}
	
	// Execute a FOR loop
	public Object visit(ASTForLoop node, Object data) {
		// loop initialisation
		doChild(node, 0);
		while (true) {
			// evaluate loop test
			Value hopefullyValueBoolean = doChild(node, 1);
			if (!(hopefullyValueBoolean instanceof ValueBoolean))
				throw new ExceptionSemantic("The test expression of a for loop must be boolean.");
			if (!((ValueBoolean)hopefullyValueBoolean).booleanValue())
				break;
			// do loop statement
			doChild(node, 3);
			// assign loop increment
			doChild(node, 2);
		}
		return data;
	}
	
	/**
	 * Execute while loop.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTWhileLoop node, Object data) {
		while (true) {
			// evaluate loop test
			Value hopefullyValueBoolean = doChild(node, 0);

			if (!(hopefullyValueBoolean instanceof ValueBoolean))
				throw new ExceptionSemantic("The test expression of a while loop must be boolean.");
			if (!((ValueBoolean)hopefullyValueBoolean).booleanValue())
				break;

			// do loop statement
			doChild(node, 1);
		}

		return data;
	}
	
	// Process an identifier
	// This doesn't do anything, but needs to be here because we need an ASTIdentifier node.
	public Object visit(ASTIdentifier node, Object data) {
		return data;
	}

	/**
	 * Execute the WRITE statement. Print out all given arguments.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTWrite node, Object data) {
		int numChildren = node.jjtGetNumChildren();
		for (int i = 0; i < numChildren; i++) {
			System.out.print(doChild(node, i));
		}
		System.out.println();
		return data;
	}

	/**
	 * Dereference a variable or parameter, and return its value.
	 * TODO: It's just an anchor, remove later.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTDereference node, Object data) {
		Display.Reference reference;

		if (node.optimised == null) {
			String name = node.tokenValue;
			reference = scope.findReference(name);
			if (reference == null)
				throw new ExceptionSemantic("Variable or parameter " + name + " is undefined.");
			node.optimised = reference;
		} else
			reference = (Display.Reference)node.optimised;
		
		// NOTE: It's hard-coded for ValueObjects. With Arrays, it will need some more logic.
		// If it's not a normal dereference of a variable.
		int numChildren = node.jjtGetNumChildren();
		if (numChildren > 0) {
			ValueObject valueObject = (ValueObject) reference.getValue();
			String keyName = getTokenOfChild(node, 0);

			for (int i = 1; i < numChildren; i++) {
				valueObject = (ValueObject) valueObject.get(keyName);
				keyName = getTokenOfChild(node, i);
			}
			Value value = valueObject.get(keyName);
			if (value == null)
				throw new ExceptionSemantic("Key \"" + keyName + "\" is undefined or equal to null.");

			return value;
		}

		return reference.getValue();
	}

	/**
	 * Execute an assignment statement.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTAssignment node, Object data) {		
		Display.Reference reference;
		int numChildren = node.jjtGetNumChildren();
		
		if (node.optimised == null) {
			String name = getTokenOfChild(node, 0);
			reference = scope.findReference(name);
			if (reference == null)
				reference = scope.defineVariable(name);
			node.optimised = reference;
		} else
			reference = (Display.Reference)node.optimised;
		
		// NOTE: It's hard-coded for ValueObjects. With Arrays, it will need some more logic.
		if (numChildren > 2) {
			ValueObject valueObject = (ValueObject) reference.getValue();
			String keyName = getTokenOfChild(node, 1);

			// Traversing the nested anonymous objects.
			// 'numChildren - 2' because '-1' is the value. 
			for (int i = 1; i < numChildren - 2; i++) {
				valueObject = (ValueObject) valueObject.get(keyName);
				keyName = getTokenOfChild(node, i + 1);
			}

			valueObject.set(keyName, doChild(node, numChildren - 1));

			return data;
		}

		reference.setValue(doChild(node, 1));
		return data;
	}

	// OR
	public Object visit(ASTOr node, Object data) {
		return doChild(node, 0).or(doChild(node, 1));
	}

	// AND
	public Object visit(ASTAnd node, Object data) {
		return doChild(node, 0).and(doChild(node, 1));
	}

	// ==
	public Object visit(ASTCompEqual node, Object data) {
		return doChild(node, 0).eq(doChild(node, 1));
	}

	// !=
	public Object visit(ASTCompNequal node, Object data) {
		return doChild(node, 0).neq(doChild(node, 1));
	}

	// >=
	public Object visit(ASTCompGTE node, Object data) {
		return doChild(node, 0).gte(doChild(node, 1));
	}

	// <=
	public Object visit(ASTCompLTE node, Object data) {
		return doChild(node, 0).lte(doChild(node, 1));
	}

	// >
	public Object visit(ASTCompGT node, Object data) {
		return doChild(node, 0).gt(doChild(node, 1));
	}

	// <
	public Object visit(ASTCompLT node, Object data) {
		return doChild(node, 0).lt(doChild(node, 1));
	}

	// +
	public Object visit(ASTAdd node, Object data) {
		return doChild(node, 0).add(doChild(node, 1));
	}

	// -
	public Object visit(ASTSubtract node, Object data) {
		return doChild(node, 0).subtract(doChild(node, 1));
	}

	// *
	public Object visit(ASTTimes node, Object data) {
		return doChild(node, 0).mult(doChild(node, 1));
	}

	// /
	public Object visit(ASTDivide node, Object data) {
		return doChild(node, 0).div(doChild(node, 1));
	}

	// NOT
	public Object visit(ASTUnaryNot node, Object data) {
		return doChild(node, 0).not();
	}

	// + (unary)
	public Object visit(ASTUnaryPlus node, Object data) {
		return doChild(node, 0).unary_plus();
	}

	// - (unary)
	public Object visit(ASTUnaryMinus node, Object data) {
		return doChild(node, 0).unary_minus();
	}

	// Return string literal
	public Object visit(ASTCharacter node, Object data) {
		if (node.optimised == null)
			node.optimised = ValueString.stripDelimited(node.tokenValue);
		return node.optimised;
	}

	// Return integer literal
	public Object visit(ASTInteger node, Object data) {
		if (node.optimised == null)
			node.optimised = new ValueInteger(Long.parseLong(node.tokenValue));
		return node.optimised;
	}

	// Return floating point literal
	public Object visit(ASTRational node, Object data) {
		if (node.optimised == null)
			node.optimised = new ValueRational(Double.parseDouble(node.tokenValue));
		return node.optimised;
	}

	// Return true literal
	public Object visit(ASTTrue node, Object data) {
		if (node.optimised == null)
			node.optimised = new ValueBoolean(true);
		return node.optimised;
	}

	// Return false literal
	public Object visit(ASTFalse node, Object data) {
		if (node.optimised == null)
			node.optimised = new ValueBoolean(false);
		return node.optimised;
	}

	/**
	 * Quit expression.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTQuit node, Object data) {
		System.exit(0);
		return null;
	}
}
