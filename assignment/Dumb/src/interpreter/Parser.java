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
		Object result = doChildren(node, data);
		removeDefinitions(node);
		return result;
	}

	/**
	 * Anonymous object declaration.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTValueObject node, Object data) {
		ValueObject valueObject = new ValueObject();

		// Add all the key-value pairs to the anonymous object.
		String keyName;
		Value value;
		for (int i = 0; i < node.jjtGetNumChildren(); i += 2) {
			keyName = getTokenOfChild(node, i);
			value = doChild(node, i + 1);
			valueObject.add(keyName, value);
		}

		return valueObject;
	}
	
	/**
	 * List declaration.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTValueList node, Object data) {
		ValueList valueList = new ValueList();

		// Add all the values to the list.
		int keyCount = node.jjtGetNumChildren();
		Value currentValue;
		for (int i = 0; i < keyCount; i++) {
			currentValue = doChild(node, i);
			valueList.append(currentValue);
		}

		return valueList;
	}

	/**
	 * Anonymous function declaration.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTFnVal node, Object data) {
		FunctionDefinition currentFnDef = new FunctionDefinition(scope.getLevel() + 1);

		// Child 0 -- function definition parameter list
		doChild(node, 0, currentFnDef);
		// Child 1 -- function body
		currentFnDef.setFunctionBody(getChild(node, 1));
		// Child 2 -- optional return expression
		if (node.fnHasReturn)
			currentFnDef.setFunctionReturnExpression(getChild(node, 2));

		return new ValueFn(currentFnDef);
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
		int leftNumChildren = node.jjtGetChild(0).jjtGetNumChildren();

		// If there's more than 1 child in the left child, then it's not just an identifier.
		if (leftNumChildren > 0) {
			fndef = getValueFn(node);
			node.optimised = fndef;
		}

		if (node.optimised == null) { 
			String fnname = getTokenOfChild(node, 0); // Child 0 - identifier (fn name)
			fndef = scope.findFunction(fnname);
			if (fndef == null) fndef = findValueFn(fnname);

			node.optimised = fndef; // Save it for next time
		} else
			fndef = (FunctionDefinition) node.optimised;

		FunctionInvocation newInvocation = new FunctionInvocation(fndef);
		doChild(node, 1, newInvocation); // Child 1 - arglist
		scope.execute(newInvocation, this); // Execute

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

		// If there's more than 1 child in the left child, then it's not just an identifier.
		if (leftNumChildren > 0) {
			fndef = getValueFn(node);
			node.optimised = fndef;
		}

		if (node.optimised == null) {
			String fnname = getTokenOfChild(node, 0);
			fndef = scope.findFunction(fnname);
			if (fndef == null) fndef = findValueFn(fnname);

			if (!fndef.hasReturn())
				throw new ExceptionSemantic("Function " + fnname + " is being invoked in an expression but does not have a return value.");

			node.optimised = fndef; // Save it for next time
		} else
			fndef = (FunctionDefinition) node.optimised;

		FunctionInvocation newInvocation = new FunctionInvocation(fndef);
		doChild(node, 1, newInvocation); // Child 1 - arglist

		return scope.execute(newInvocation, this); // Execute and return the outcome.
	}

	/**
	 * Retrieves ValueFn from a dereference and returns its FunctionDefinition.
	 * 
	 * @param node
	 * @author amrwc
	 */
	private FunctionDefinition getValueFn(SimpleNode node) {
		Value value = doChild(node, 0); // Do the dereference.

		ValueFn valueFunction = (ValueFn) value;
		if (valueFunction == null)
			throw new ExceptionSemantic("The value function you are trying to invoke is undefined.");

		FunctionDefinition fndef = valueFunction.get();
		if (fndef == null)
			throw new ExceptionSemantic("Function " + valueFunction.getName() + " is undefined.");

		return fndef;
	}

	/**
	 * Prototype function invocation.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTProtoInvoke node, Object data) {
		Value value = doChild(node, 0);
		String protoFunc = node.tokenValue;
		ArrayList<Value> protoArgs = (node.jjtGetNumChildren() > 1)
			? parseProtoArgs((SimpleNode) node)
			: null;

		if (value instanceof ValueList) {
			return ((ValueList) value).execProto(protoFunc, protoArgs);
		} else if (value instanceof ValueObject) {
			return ((ValueObject) value).execProto(protoFunc, protoArgs);
		} else {
			throw new ExceptionSemantic("Variable \""
				+ ((SimpleNode) node.jjtGetChild(0)).tokenValue
				+ "\" of type \"" + value.getClass().getCanonicalName()
				+ "\" does not support prototype functions.");
		}
	}

	/**
	 * Parse the prototype function's arguments.
	 * 
	 * @param node -- proto_invoke() == ASTProtoInvoke
	 * @author amrwc
	 */
	private ArrayList<Value> parseProtoArgs(SimpleNode node) {
		ArrayList<Value> values = new ArrayList<Value>();
		for (int i = 1; i < node.jjtGetNumChildren(); i++) {
			values.add((Value) node.jjtGetChild(i).jjtAccept(this, null));
		}
		return values;
	}

	// Function invocation argument list.
	public Object visit(ASTArgList node, Object data) {
		FunctionInvocation newInvocation = (FunctionInvocation)data;
		for (int i=0; i<node.jjtGetNumChildren(); i++)
			newInvocation.setArgument(doChild(node, i));
		newInvocation.checkArgumentCount();
		return data;
	}

	/**
	 * Execute an if statement.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTIfStatement node, Object data) {
		Value test = doChild(node, 0);
		if (!(test instanceof ValueBoolean))
			throw new ExceptionSemantic("The test expression of an if statement must be boolean.");

		if (((ValueBoolean) test).booleanValue()) // If test evaluated to true...
			doChild(node, 1);                     // ...do 'if'. Or...
		else if (node.ifHasElse)                  // ...if it evaluated to false and has 'else'...
			doChild(node, 2);                     // ...do 'else'.

		removeDefinitions(node);
		return data;
	}

	/**
	 * Execute a for loop.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTForLoop node, Object data) {
		doChild(node, 0); // Initialise the loop (usually 'let i = 0').

		while (true) {
			Value loopTest = doChild(node, 1);
			if (!(loopTest instanceof ValueBoolean))
				throw new ExceptionSemantic("The test expression of a for loop must be boolean.");

			if (!((ValueBoolean) loopTest).booleanValue()) // If loopTest evaluated to false, break.
				break;

			doChild(node, 3); // Do the loop statement()/body().

			// Remove the definitions made inside a statement() without block().
			removeDefinitions(node);

			doChild(node, 2); // Evaluate the loop expression (usually 'i++').
		}

		// Remove all definitions in this scope, including the initialisation.
		removeDefinitions(node, (SimpleNode) node.jjtGetChild(0));
		return data;
	}

	/**
	 * Execute a while loop.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTWhileLoop node, Object data) {
		while (true) {
			Value loopTest = doChild(node, 0);
			if (!(loopTest instanceof ValueBoolean))
				throw new ExceptionSemantic("The test expression of a while loop must be boolean.");

			if (!((ValueBoolean) loopTest).booleanValue()) // If loopTest evaluated to false, break.
				break;

			doChild(node, 1); // Do loop statement()/block().
			removeDefinitions(node); // Clean up the variable/function definitions.
		}

		return data;
	}

	/**
	 * Remove all definitions from the scope.
	 * 
	 * @param node
	 * @param init -- initialisation node in for-loops.
	 * @author amrwc
	 */
	public void removeDefinitions(SimpleNode node, SimpleNode init) {
		ArrayList<SimpleNode> definitions = collectDefinitions(node, init);

		Consumer<SimpleNode> removeDefinition = definition -> {
			if (definition instanceof ASTDefinition) {
				SimpleNode assignmentNode = (SimpleNode) definition.jjtGetChild(0);
				String variableName = getTokenOfChild(assignmentNode, 0);
				scope.removeVariable(variableName);
			} else if (definition instanceof ASTFnDef) {
				String fnName = getTokenOfChild(definition, 0);
				scope.removeFunction(fnName);
			}
		};

		definitions.forEach(removeDefinition);
	}

	public void removeDefinitions(SimpleNode node) {
		removeDefinitions(node, null);
	}

	/**
	 * Collect all definitions in the scope.
	 * 
	 * @param node
	 * @param init -- initialisation node in for-loops.
	 * @author amrwc
	 */
	private ArrayList<SimpleNode> collectDefinitions(SimpleNode node, SimpleNode init) {
		ArrayList<SimpleNode> definitions = new ArrayList<SimpleNode>();

		// Handle raw block() that occurs without any preceding statement such as for()/if().
		if (node instanceof ASTBlock) {
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				Node innerNode = node.jjtGetChild(i).jjtGetChild(0);
				if (innerNode instanceof ASTDefinition || innerNode instanceof ASTFnDef)
					definitions.add((SimpleNode) innerNode);
			}
			return definitions;
		}

		// If it's a for-loop including an initialisation node...
		if (init != null && init instanceof ASTDefinition) definitions.add(init);

		// Handle statement()/block() without children.
		if (node.jjtGetNumChildren() == 0) return definitions;

		// Collect the possible single definition from the statement().
		Node statement = node.jjtGetChild(node.jjtGetNumChildren() - 1);

		// statement() -> definition()/fndef() -- there can only be one, since it's not a block.
		Node innerNode = statement.jjtGetChild(0);
		if (innerNode instanceof ASTDefinition || innerNode instanceof ASTFnDef)
			definitions.add((SimpleNode) innerNode);

		return definitions;
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
	 * Dereference a variable or parameter, and return its value.,
	 * or call/invoke a function.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTDereference node, Object data) {
		Display.Reference reference;

		// Get the main variable's/parameter's name (token).
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

			// ...traverse through the chain of dereferences.
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
		return valueList.get(index);
	}

	/**
	 * Dereference of an anonymous object.
	 * 
	 * @author amrwc
	 */
	private Value objectDereference(SimpleNode node, Value v, int currChild) {
		ValueObject valueObject = (ValueObject) v;
		String keyName = getTokenOfChild(node, currChild);
		return valueObject.get(keyName);
	}

	/**
	 * Definition using the <LET> keyword.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTDefinition node, Object data) {
		Display.Reference reference;
		Node assignment = node.jjtGetChild(0);
		String name = getTokenOfChild((SimpleNode) assignment, 0);

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

		Value rightVal = doChild(node, numChildren - 1);
		if (rightVal == null)
			throw new ExceptionSemantic("Right value of the assignment cannot resolve to null.");

		if (node.optimised == null) {
			String name = getTokenOfChild(node, 0);
			reference = scope.findReference(name);
			if (reference == null)
				throw new ExceptionSemantic("Variable \"" + name + "\" is undefined.");
			node.optimised = reference;
		} else
			reference = (Display.Reference) node.optimised;

		if (node.shorthandOperator != null) {
			Value value = reference.getValue();

			switch (node.shorthandOperator) {
				case "+=":
					reference.setValue(value.add(rightVal));
					break;
				case "-=":
					reference.setValue(value.subtract(rightVal));
			}
			return data;
		}

		if (numChildren > 2) {
			Value value = reference.getValue();

			if (value instanceof ValueList) {
				int index = (int) ((ValueInteger) doChild(node, numChildren - 2)).longValue();
				((ValueList) value).set(index, rightVal);
			} else if (value instanceof ValueObject) {
				String keyName = getTokenOfChild(node, numChildren - 2);
				((ValueObject) value).set(keyName, rightVal);
			}

			return data;
		}

		reference.setValue(rightVal);
		return data;
	}

	/**
	 * Increment/decrement.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTIncrementDecrement node, Object data) {
		Display.Reference reference;

		String name = getTokenOfChild(node, 0);
		reference = scope.findReference(name);
		if (reference == null)
			throw new ExceptionSemantic("Variable \"" + name + "\" is undefined.");

		Value value = reference.getValue();
		ValueInteger one = new ValueInteger(1);

		switch (node.shorthandOperator) {
			case "pre++":
				reference.setValue(value.add(one));
				value = reference.getValue();
				break;
			case "pre--":
				reference.setValue(value.subtract(one));
				value = reference.getValue();
				break;
			case "post++":
				reference.setValue(value.add(one));
				break;
			case "post--":
				reference.setValue(value.subtract(one));
		}

		return value;
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

	// %
	public Object visit(ASTModulo node, Object data) {
		return doChild(node, 0).mod(doChild(node, 1));
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
