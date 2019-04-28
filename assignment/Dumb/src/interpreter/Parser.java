package interpreter;

import java.util.ArrayList;
import java.util.function.Consumer;

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

//			fndef = scope.findFunctionInCurrentLevel(valueFunction.getName());
			fndef = valueFunction.get();
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

	/**
	 * Prototype function invocation.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTProtoInvoke node, Object data) {
		Value value = doChild(node, 0);
		String protoFunc = node.tokenValue;
		Value protoArg = (node.jjtGetNumChildren() > 1)
			? doChild(node, 1)
			: null;

		if (value instanceof ValueList) {
			switch (protoFunc.toString()) {
				case "append":
					((ValueList) value).append(protoArg);
					break;
				case "length":
					return ((ValueList) value).length();
				default:
					throw new ExceptionSemantic("There is no prototype function \"" + protoFunc + "\" in ValueList.");
			}
		} else if (value instanceof ValueObject) {
			switch (protoFunc) {
				case "remove":
					((ValueObject) value).remove(protoArg.stringValue());
					break;
				case "tryRemove":
					((ValueObject) value).tryRemove(protoArg.stringValue());
					break;
				default:
					throw new ExceptionSemantic("There is no prototype function \"" + protoFunc + "\" in ValueObject.");
			}
		}

		return data;
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
		doChild(node, 0); // loop initialisation

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

		ArrayList<SimpleNode> declarations = new ArrayList<SimpleNode>();

		// Get the variable name declared on the loop's initialisation.
		Node initialisationNode = node.jjtGetChild(0);
		declarations.add((SimpleNode) initialisationNode);

		// Get all declarations from the loop's statement() (code block).
		// statement() -> block() -> statement() -> declaration()+
		Node loopStatement = node.jjtGetChild(node.jjtGetNumChildren() - 1);
		Node codeBlock = loopStatement.jjtGetChild(0);
		for (int i = 0; i < codeBlock.jjtGetNumChildren(); i++) {
			Node hopefullyDeclaration = codeBlock.jjtGetChild(i).jjtGetChild(0);
			if (hopefullyDeclaration instanceof ASTDeclaration)
				declarations.add((SimpleNode) hopefullyDeclaration);			
		}

		// Remove all declarations in this scope.
		Consumer<SimpleNode> remove = declaration -> {
			SimpleNode assignmentNode = (SimpleNode) declaration.jjtGetChild(0);
			String variableName = getTokenOfChild(assignmentNode, 0);
			scope.removeVariable(variableName);
		};
		declarations.forEach(remove);

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
			reference = (Display.Reference) node.optimised;

		int numChildren = node.jjtGetNumChildren();
		if (numChildren > 0) { // If it's not a normal dereference of a variable...
			int currChild = 0; // Keep track of how far it traversed.
			Value value = reference.getValue();

			for (; currChild < numChildren; currChild++) {
				if (value instanceof ValueList)
					value = listDereference(node, value, currChild);
				else if (value instanceof ValueObject)
					value = objectDereference(node, value, currChild);
			}

			return value;
		}

		return reference.getValue();
	}
	
	/**
	 * Dereference of a list.
	 * 
	 * @author amrwc
	 */
	private Value listDereference(SimpleNode node, Value v, int currChild) {
		ValueList valueList = (ValueList) v;
		int index = (int) ((ValueInteger) doChild(node, currChild)).longValue();
		if (valueList.size() <= index)
			throw new ExceptionSemantic("The index " + index + " is out of bounds of \""
				+ node.tokenValue + "\" of length " + valueList.size() + ".");

		var value = valueList.get(index);
		if (value == null)
			throw new ExceptionSemantic("Value of index " + index +
				" in list " + node.tokenValue + " is undefined or equal to null.");
		
		return value;
	}

	/**
	 * Dereference of an anonymous object.
	 * 
	 * @author amrwc
	 */
	private Value objectDereference(SimpleNode node, Value v, int currChild) {
		ValueObject valueObject = (ValueObject) v;
		String keyName = getTokenOfChild(node, currChild);

		Value value = valueObject.get(keyName);
		if (value == null)
			throw new ExceptionSemantic("Key \"" + keyName + "\" is undefined or equal to null.");
		
		return value;
	}

	/**
	 * Execute a declaration statement.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTDeclaration node, Object data) {
		Display.Reference reference;
		Node assignment = node.jjtGetChild(0);
		String name = getTokenOfChild((SimpleNode) assignment, 0);

		// If it's in a for loop, make an exception and allow for
		// an assignment on subsequent runs.
//		if (node.jjtGetParent() instanceof ASTForLoop) {
//			reference = scope.findReference(name);
//			if (reference == null) {
//				reference = scope.defineVariable(name);
////				System.out.println("hmm " + reference.getLevel());
//				assignment.jjtAccept(this, data);
//			} else
//				assignment.jjtAccept(this, data);
//			return data;
//		}

		reference = scope.findReference(name);
		if (reference == null)
			reference = scope.defineVariable(name);
		else
			throw new ExceptionSemantic("Variable \"" + name + "\" already exists.");

		// Do the assignment.
		assignment.jjtAccept(this, data);
		return data;
	}

	/**
	 * Execute an assignment statement.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTAssignment node, Object data) {
		Display.Reference reference;
		int numChildren = node.jjtGetNumChildren();
		Value newVal = doChild(node, numChildren - 1);
		
		if (node.optimised == null) {
			String name = getTokenOfChild(node, 0);
			reference = scope.findReference(name);
			if (reference == null)
				throw new ExceptionSemantic("Variable \"" + name + "\" doesn't exist.");
			node.optimised = reference;
		} else
			reference = (Display.Reference) node.optimised;

		if (numChildren > 2) {
			int currChild = 1; // Keep track of how far it traversed.
			Value value = reference.getValue();

			// Dereference
			for (; currChild < numChildren - 2; currChild++) {
				if (value instanceof ValueList)
					value = listDereference(node, value, currChild);
				else if (value instanceof ValueObject)
					value = objectDereference(node, value, currChild);
			}

			// Assignment
			if (value instanceof ValueList) {
				int index = (int) ((ValueInteger) doChild(node, currChild)).longValue();
				((ValueList) value).set(index, newVal);
			} else if (value instanceof ValueObject) {
				String keyName = getTokenOfChild(node, currChild);
				((ValueObject) value).set(keyName, newVal);
			}

			return data;
		}

		reference.setValue(newVal);
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
