package interpreter;

import java.util.HashMap;
import java.util.UUID;

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
	
	// Execute a Sili program
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
	 * Anonymous function declaration.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTFnVal node, Object data) {
		// Already defined?
		if (node.optimised != null)
			return data;

		// Assign the variable name as the function name.
		String fnname = getTokenOfChild((SimpleNode)node.jjtGetParent(), 0);
		if (fnname == null) {
//			System.out.println("HAHAHA " + scope.getLevel());
			UUID uuid = UUID.randomUUID();
	        fnname = uuid.toString();
//	        System.out.println("HAHAHA2 " + scope.getLevel());
//	        System.out.println("HAHAHA " + fnname);
		}

		if (scope.findFunctionInCurrentLevel(fnname) != null)
			throw new ExceptionSemantic("Function " + fnname + " already exists.");
		FunctionDefinition currentFunctionDefinition = new FunctionDefinition(fnname, scope.getLevel() + 1);

		// Child 0 -- function definition parameter list
		doChild(node, 0, currentFunctionDefinition); // TODO: NOTE: The params are not executed correctly?

		// Add to available functions
		scope.addFunction(currentFunctionDefinition);

		// Child 1 -- function body
		currentFunctionDefinition.setFunctionBody(getChild(node, 1));

		// Child 2 -- optional return expression
		if (node.fnHasReturn)
			currentFunctionDefinition.setFunctionReturnExpression(getChild(node, 2));

		ValueFn valueFunction = new ValueFn(fnname, scope.getLevel() + 1);

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
	
	// Function call
	public Object visit(ASTCall node, Object data) {
		FunctionDefinition fndef;
		if (node.optimised == null) { 
			// Child 0 - identifier (fn name)
			String fnname = getTokenOfChild(node, 0);
			fndef = scope.findFunction(fnname);
			if (fndef == null)
				throw new ExceptionSemantic("Function " + fnname + " is undefined.");
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
	// NOTE: It's a hack to keep track of potentially useful aliases stored like <argument index, UUID.toString()>.
	HashMap<Integer, String> FN_VAL_ALIASES = new HashMap<Integer, String>();

	public Object visit(ASTFnInvoke node, Object data) {
		FunctionDefinition fndef;
		int leftNumChildren = node.jjtGetChild(0).jjtGetNumChildren(); // numChildren of the left child

		// NOTE: This locates ValueFn inside of ValueObject.
		// 		 Only works after modifying grammar to dereference() arglist(). 
		// If there's more than 1 child in the left child, it's an object.
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
//			System.out.println("out loop: " + fndef + " of " + fnname);
//			System.out.println("parameter name: " + fndef.getParameterName(0));
			
			SimpleNode arglist = (SimpleNode) node.jjtGetChild(1);
			
			// Search for ValueFn's among the arguments.
			if (arglist.jjtGetNumChildren() > 0) {
//				HashMap<Integer, String> fnValAliases = new HashMap<Integer, String>();

				for (int i = 0; i < arglist.jjtGetNumChildren(); i++) {
					var currentArg = doChild(arglist, i);
//					System.out.print("FnName: " + currentArg.getName());
//					System.out.println(" FnClass: " + currentArg.getClass());
					if (currentArg instanceof values.ValueFn) {
						FN_VAL_ALIASES.put(i, currentArg.getName()); // Store the alias in the map.
					}
				}
			}

//			System.out.println("luls " + fnValAliases.get(0));
			
			// If function seems to be undefined, try to find the function in the aliases map.
			// This procedure climbs the tree up to the FnVal node, then scans the parameters,
			// and if there is a parameter that matches the function's name, try finding it in
			// the aliases map using the parameter's index, since it must match the argument's
			// position.
			if (fndef == null) {
				var parentNode = node.jjtGetParent();
				while(parentNode.toString() != "FnVal") {
					parentNode = parentNode.jjtGetParent();
				}

				var paramList = parentNode.jjtGetChild(0);
				// TODO: Iterate through all parameters.
				// Run the ASTIdentifier method to get the token (parameter's name).
				doChild((SimpleNode) paramList, 0); // The current param's name is stored in LAST_IDENTIFIER.				

//				System.out.println("cd " + LAST_IDENTIFIER);

				// If the currently invoked function matches the parameter's name, look up an alias.
//				if (fnname == LAST_IDENTIFIER) { // How is this false?????
				if (fnname.compareTo(LAST_IDENTIFIER) == 0) {
					var valueFnPassedAsAnArgument = FN_VAL_ALIASES.get(0); // 0 should be a loop index.
//					System.out.println("wut? " + temp);
					if (valueFnPassedAsAnArgument != null) fnname = valueFnPassedAsAnArgument;
					fndef = scope.findFunction(fnname);
				}
			}

//			if (fndef == null) {
//				SimpleNode arglist = (SimpleNode) node.jjtGetChild(1);
//				System.out.println("in loop111: " + arglist.jjtGetNumChildren());
//
//				for (int i = 0; i < arglist.jjtGetNumChildren(); i++) {
////					doChild(node, i);
//					arglist.jjtGetChild(i);
////					System.out.println("in loop: " + one.jjtGetChild(i).toString());
////					if (one.jjtGetChild(i).toString() == "FnVal") System.out.println("eh");
////					ValueFn two = (ValueFn) one.jjtGetChild(i);
//					var two = arglist;
//					var three = doChild(two, i);
//					System.out.println("in loop: " + three.getName());
////					fnname = three.getName();
//				}
//			}

//			System.out.println("out loop: " + fnname);
			
			// Go through all arguments of the invoked function.
			
//			System.out.println("THERE " + one.jjtGetChild(0).getClass());
//			((ValueFn) one.jjtGetChild(0)).resetName(getTokenOfChild(node, 0));
//			if (one.jjtGetChild(0).getClass() == "class parser.ast.ASTFnVal") {
//				System.out.println("THERE " + one.jjtGetChild(0));
//			}
			// TODO: Find the right condition and then loop through the arguments looking for FnVal.
			// 		 When found, try to get their name by fnVal.getName() and pass that name to the
			//		 function definition below, or just do fnname = fnVal.getName().
//			System.out.println(doChild((SimpleNode) node.jjtGetChild(1), 0));
//			System.out.println(doChild((SimpleNode) node.jjtGetChild(1), 0));
			
			// Child 0 - identifier (fn name)
//			String fnname = getTokenOfChild(node, 0);
//			System.out.println("dupa: " + fnname); // REMOVE
//			fndef = scope.findFunction(fnname);
//			System.out.println("DOPE: " + fndef + "   " + scope.getLevel()); // REMOVE


			// NOTE: This solution works with an anonymous function passed as a parameter,
			// but assigns the function's name to be null. Please fix.
			// UPDATE: It doesn't assign "null" as the function's name here, it's being
			// defined implicitly, and since there is no assignment on the invocation,
			// it assigns null as the zero'th parameter (anonymous function's identifier).
			// Maybe I could assign a random name to it while parsing the parameters?
//			if (fndef == null) {
//				String fnParamName = getTokenOfChild((SimpleNode)node.jjtGetParent(), 0); // WRONG -- returns null
////				System.out.println("DOPE777: " + fnParamName); // REMOVE
//				FunctionDefinition fnParamDefinition = scope.findFunction(fnParamName);
//				
//				fndef = fnParamDefinition;
//			}
			
			
			// NOTE: If the invoked function is not found in the scope, try adding it
			// immediately. If it's still null, then throw the exception. TODO: It's just an anchor to quickly scroll here.
//			if (fndef == null) { // Try to define the anonymous function on the go.
//				FunctionDefinition tryFndef = new FunctionDefinition(fnname, scope.getLevel());
//
//				// Child 0 -- function definition parameter list
//				doChild(node, 0, tryFndef); // Copy-pasted
////				doChild((SimpleNode)node.jjtGetParent(), 0, tryFndef); // Trying differently
//				
//				// Add to available functions
//				scope.addFunction(tryFndef);
//				
//				// Child 1 -- function body
//				tryFndef.setFunctionBody(getChild(node, 1)); // Copy-pasted
////				tryFndef.setFunctionBody(getChild((SimpleNode)node.jjtGetParent(), 1)); // Trying differently
//				
//				// Child 2 -- optional return expression
//				if (node.fnHasReturn) {
//					System.out.println("IT DOES INDEED HAVE RETURN");
////					tryFndef.setFunctionReturnExpression(getChild(node, 2)); // Copy-pasted
//					tryFndef.setFunctionReturnExpression(getChild((SimpleNode)node.jjtGetParent(), 2)); // Trying differently
//				}
//				
//				
//				
////				tryFndef.setFunctionBody(getChild((SimpleNode)node.jjtGetParent(), 0));
////				tryFndef.setFunctionReturnExpression(getChild((SimpleNode)node.jjtGetParent(), 1));
//				
////				tryFndef.getParameterCount(getChild((SimpleNode)node.jjtGetParent(), 0));
////				tryFndef.setFunctionBody(getChild((SimpleNode)node.jjtGetParent(), 1));
////				tryFndef.setFunctionReturnExpression(getChild((SimpleNode)node.jjtGetParent(), 2));
////				System.out.println(tryFndef.getFunctionBody());
//				
//				scope.addFunction(tryFndef);
//				fndef = scope.findFunction(fnname);
//			}
//			System.out.print(fnname); System.out.print(": "); // REMOVE
//			System.out.println(fndef); // REMOVE

			if (fndef == null)
				throw new ExceptionSemantic("Function " + fnname + " is undefined.");
			
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

//		return data; // TODO: What is the difference?
		return null;
	}
	
	// Process an identifier
	// This doesn't do anything, but needs to be here because we need an ASTIdentifier node.
	String LAST_IDENTIFIER; // NOTE: This is a hack to get the Identifier's token for the parameter scanning purposes.
	public Object visit(ASTIdentifier node, Object data) {
		LAST_IDENTIFIER = node.tokenValue;
		return data;
	}
	
	// Execute the WRITE statement
	public Object visit(ASTWrite node, Object data) {
//		int num = node.jjtGetNumChildren();
//		System.out.println(num);
//		for (int i = 0; i < num; num++) {
//			System.out.println(doChild(node, i));
//		}
		System.out.println(doChild(node, 0));
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
