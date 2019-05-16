package interpreter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import interpreter.Display.Reference;
import parser.ast.*;
import values.*;

public class Parser implements CloveVisitor {
	private String[] argv;
	private Display scope = new Display(); // Scope display handler

	public Parser(String[] args) {
		argv = args;
	}

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

	// Execute a Clove program
	public Object visit(ASTCode node, Object data) {
		return doChildren(node, data);	
	}



	/***********************************************
	 *                 Statements                  *
	 ***********************************************/

	// Execute a statement
	public Object visit(ASTStatement node, Object data) {
		return doChildren(node, data);
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
	 * Definition using the <LET> and <CONST> keywords.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTDefinition node, Object data) {
		Node initialisation = node.jjtGetChild(0);
		String name = getTokenOfChild((SimpleNode) initialisation, 0);

		if (scope.findReference(name) == null && scope.findReference("constant" + name) == null) {
			switch (node.defType) {
				case "variable":
					scope.defineVariable(name);
					break;
				case "constant":
					scope.defineConstant(name);
			}
			initialisation.jjtAccept(this, data); // Do the initialisation.
		}
		else
			throw new ExceptionSemantic("Variable or constant \"" + name + "\" already exists.");

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

		// Get the reference of the parent value.
		if (node.optimised == null) {
			String name = getTokenOfChild(node, 0);
			reference = scope.findReference(name);
			if (reference == null) {
				// Try finding a constant.
				reference = scope.findReference("constant" + name);
				if (reference == null)
					throw new ExceptionSemantic("Variable or constant \"" + name + "\" is undefined.");

				// If it's a constant but it's not a dereference into a const obj or list.
				if (reference != null && numChildren <= 2)
					throw new ExceptionSemantic("\"" + name + "\" is a constant and cannot be reassigned.");
			}
			node.optimised = reference;
		} else
			reference = (Display.Reference) node.optimised;

		// If it's not a normal dereference of a variable...
		if (numChildren > 2) {
			Value value = reference.getValue();
			int currChild = 1; // Keep track how far it traversed.

			// -2 := the parent of the rightmost value to the left
			// of the assignment operator.
			final int limit = numChildren - 2;

			// ...traverse through the dereference to find the parent of
			// the rightmost value to the left of the assignment operator...
			for (; currChild < limit; currChild++) {
				if (value instanceof ValueList)
					value = listDereference(node, value, currChild);
				else if (value instanceof ValueObject)
					value = objectDereference(node, value, currChild);
			}

			// Handle a shorthand operator on a dereferenced variable.
			if (node.shorthandOperator != null) {
				// If the shorthand operator is present, get the rightmost
				// value of the dereference (the deepest dereference of Lvalue).
				Value val = null;
				if (value instanceof ValueList)
					val = listDereference(node, value, currChild);
				else if (value instanceof ValueObject)
					val = objectDereference(node, value, currChild);
				else if (value instanceof ValueString)
					val = stringDereference(node, value, currChild);

				// Update the Rvalue that will be assigned.
				rightVal = 
					doShorthand(node.shorthandOperator, val, rightVal);
			}

			// ...and reassign the value of the list...
			if (value instanceof ValueList) {
				int index = (int) ((ValueInteger) doChild(node, numChildren - 2)).longValue();
				((ValueList) value).set(index, rightVal);
			}
			// ...or an object's key.
			else if (value instanceof ValueObject) {
				String keyName = node.jjtGetChild(numChildren - 2) instanceof ASTIdentifier
						? getTokenOfChild(node, numChildren - 2)
						: doChild(node, numChildren - 2).toString();
				((ValueObject) value).set(keyName, rightVal);
			}
		}

		// Normal variable (1 child in L-value).
		else {
			// Handle a shorthand operator on a normal variable.
			if (node.shorthandOperator != null) {
				Value value = reference.getValue();
				Value result = doShorthand(node.shorthandOperator, value, rightVal);
				reference.setValue(result);
				return data;
			}

			// Assignment of a normal variable (no dereference).
			reference.setValue(rightVal);
		}

		return data;
	}

	/**
	 * Executes a shorthand reassignment between L-value and R-value.
	 * 
	 * @param operator -- shorthand operator to be executed
	 * @param ref -- variable reference
	 * @param val -- base value (operand)
	 * @param rightVal -- second operand
	 * @author amrwc
	 */
	private Value doShorthand(String operator, Value val, Value rightVal) {
		switch (operator) {
			case "+=":
				return val.add(rightVal);
			case "-=":
				return val.subtract(rightVal);
			case "*=":
				return val.mult(rightVal);
			case "/=":
				return val.div(rightVal);
			default:
				throw new ExceptionSemantic("Operator \"" + operator
					+ "\" cannot be used on " + val + " and " + rightVal + ".");
		}
	}

	// Function definition
	public Object visit(ASTFunctionDefinition node, Object data) {
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

	// Execute a block
	public Object visit(ASTBlock node, Object data) {
		Object result = doChildren(node, data);
		removeDefinitions(node);
		return result;
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
				SimpleNode initNode = (SimpleNode) definition.jjtGetChild(0);
				String variableName = getTokenOfChild(initNode, 0);
				if (scope.findReference("constant" + variableName) != null)
					scope.removeVariable("constant" + variableName);
				else
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
	 * Quit expression.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTQuit node, Object data) {
		System.exit(0);
		return null;
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
		} else if (value instanceof ValueString) {
			return ((ValueString) value).execProto(protoFunc, protoArgs);
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

	/**
	 * Declaration of a variable.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTDeclaration node, Object data) {
		if (node.defType == "constant")
			throw new ExceptionSemantic("Constants must be initialised.");

		String name = getTokenOfChild((SimpleNode) node, 0);
		if (scope.findReference(name) != null)
			throw new ExceptionSemantic("Variable \"" + name + "\" already exists.");

		scope.defineVariable(name);
		return data;
	}

	/**
	 * Sends an HTTP request and returns the response.
	 * 
	 * @returns {ValueObject} HTTP response code and body
	 * @author amrwc
	 */
	public Object visit(ASTHttp node, Object data) {
		String method = doChild(node, 0).toString().toUpperCase();
		String url = doChild(node, 1).toString();
		String body = null;

		switch (method) {
			case "GET":
			case "DELETE": {
				return doHttpReq(method, url);
			}
			case "POST":
			case "PUT": {
				if (node.jjtGetNumChildren() < 3)
					throw new ExceptionSemantic("The \"" + method
						+ "\" HTTP method needs a request body.");
				else
					body = doChild(node, 2).toString();
				return doHttpReq(method, url, body);
			}
			default:
				throw new ExceptionSemantic("The http function doesn't support \""
					+ method + "\" method.");
		}
	}

	/**
	 * Sends an HTTP request and returns its response
	 * in a ValueObject.
	 * 
	 * @read https://docs.oracle.com/javase/tutorial/networking/urls/readingWriting.html
	 * @param method
	 * @param requestURL
	 * @param data
	 * @returns {ValueObject} response code and body
	 * @author amrwc
	 */
	private ValueObject doHttpReq(String method, String requestURL, String data) {
		URL url;
		String responseBody = "";
		ValueObject res = new ValueObject();
		res.add("code", null);
		res.add("body", null);

		try {
			url = new URL(requestURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			switch(method) {
				case "GET":
				case "DELETE":
					break;
				case "POST":
				case "PUT": {
					if (data == null)
						throw new ExceptionSemantic("The \"" + method
							+ "\" method's request body cannot evaluate to null.");
	
					conn.setReadTimeout(15000);
					conn.setConnectTimeout(15000);
					conn.setRequestMethod(method);
					conn.setDoOutput(true);
	
					OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
					out.write(data);
					out.flush();
					out.close();
					break;
				}
				default:
					throw new ExceptionSemantic("The \"" + method
						+ "\" method is not supported by the http function.");
			}
			int responseCode = conn.getResponseCode();
			res.set("code", new ValueInteger(responseCode));

			if (responseCode == 200 || responseCode == 201) {
				String line;
				BufferedReader br = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
				while ((line = br.readLine()) != null)
                    responseBody += line.strip() + "\n";
				br.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			res.set("body", new ValueString(responseBody));
		}

		return res;
	}

	private ValueObject doHttpReq(String method, String requestURL) {
		return doHttpReq(method, requestURL, null);
	}

	/**
	 * Writes text content to a file.
	 * 
	 * Example usage:
	 * file("append", "file.txt", content)
	 * ...where content can be any Value type.
	 * 
	 * @read https://stackoverflow.com/a/23221771/10620237
	 * @param (child0) {String} option ("create"/"overwrite"|"open"/"append")
	 * @param (child1) {String/ValueString} path
	 * @param (child2) {String/Value} content
	 * @returns path to the file
	 * @author amrwc
	 */
	public Object visit(ASTFile node, Object data) {
		String option = doChild(node, 0).toString();
		String pathStr = doChild(node, 1).toString();
		Path path = Paths.get(pathStr);
		Path parentDir = path.getParent();
		Value content = doChild(node, 2);

		// Transform the content to an Iterable; end it with a new line.
		List<String> lines = Arrays.asList(content.toString(), "");
		Charset utf8 = StandardCharsets.UTF_8;

		try {
			if (parentDir != null && Files.notExists(parentDir)) 
				Files.createDirectory(parentDir);

			switch (option) {
				case "create":
				case "overwrite":
					Files.write(path, lines, utf8);
					break;
				case "open":
				case "append":
					Files.write(path, lines, utf8,
						StandardOpenOption.CREATE, StandardOpenOption.APPEND);
					break;
				default:
					throw new ExceptionSemantic("There is no \"" + option
						+ "\" option in the file function.");
			}
		} catch (Exception e) {
			System.err.println("Problem writing to the \"" + pathStr + "\" file.");
		    e.printStackTrace();
		}

		return new ValueString(pathStr);
	}



	/***********************************************
	 *               Sub-statements                *
	 ***********************************************/

	// Process an identifier
	// This doesn't do anything, but needs to be here because we need an ASTIdentifier node.
	public Object visit(ASTIdentifier node, Object data) {
		return data;
	}

	/**
	 * Initialise a constant.
	 * 
	 * NOTE: It's separated from assignment, to separate the concerns.
	 *       This way constants can be safely defined and initialised.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTConstInit node, Object data) {
		String name = getTokenOfChild(node, 0);
		Display.Reference reference = scope.findReference("constant" + name);
		Value rightVal = doChild(node, 1);
		if (rightVal == null)
			throw new ExceptionSemantic("Right value of the constant's initialisation cannot resolve to null.");

		reference.setValue(rightVal);
		return data;
	}

	// Function invocation argument list.
	public Object visit(ASTArgumentList node, Object data) {
		FunctionInvocation newInvocation = (FunctionInvocation)data;
		for (int i=0; i<node.jjtGetNumChildren(); i++)
			newInvocation.setArgument(doChild(node, i));
		newInvocation.checkArgumentCount();
		return data;
	}

	// Function definition parameter list
	public Object visit(ASTParameterList node, Object data) {
		FunctionDefinition currentDefinition = (FunctionDefinition)data;
		for (int i=0; i<node.jjtGetNumChildren(); i++)
			currentDefinition.defineParameter(getTokenOfChild(node, i));
		return data;
	}

	// Function body
	public Object visit(ASTFunctionBody node, Object data) {
		return doChildren(node, data);
	}

	// Function return expression
	public Object visit(ASTReturnExpression node, Object data) {
		return doChildren(node, data);
	}



	/***********************************************
	 *                Expressions                  *
	 ***********************************************/

	public Object visit(ASTOr node, Object data) { // OR
		return doChild(node, 0).or(doChild(node, 1));
	}
	public Object visit(ASTAnd node, Object data) { // AND
		return doChild(node, 0).and(doChild(node, 1));
	}
	public Object visit(ASTCompEqual node, Object data) { // ==
		return doChild(node, 0).eq(doChild(node, 1));
	}
	public Object visit(ASTCompNequal node, Object data) { // !=
		return doChild(node, 0).neq(doChild(node, 1));
	}
	public Object visit(ASTCompGTE node, Object data) { // >=
		return doChild(node, 0).gte(doChild(node, 1));
	}
	public Object visit(ASTCompLTE node, Object data) { // <=
		return doChild(node, 0).lte(doChild(node, 1));
	}
	public Object visit(ASTCompGT node, Object data) { // >
		return doChild(node, 0).gt(doChild(node, 1));
	}
	public Object visit(ASTCompLT node, Object data) { // <
		return doChild(node, 0).lt(doChild(node, 1));
	}
	public Object visit(ASTAdd node, Object data) { // +
		return doChild(node, 0).add(doChild(node, 1));
	}
	public Object visit(ASTSubtract node, Object data) { // -
		return doChild(node, 0).subtract(doChild(node, 1));
	}
	public Object visit(ASTTimes node, Object data) { // *
		return doChild(node, 0).mult(doChild(node, 1));
	}
	public Object visit(ASTDivide node, Object data) { // /
		return doChild(node, 0).div(doChild(node, 1));
	}
	public Object visit(ASTModulo node, Object data) { // %
		return doChild(node, 0).mod(doChild(node, 1));
	}
	public Object visit(ASTUnaryNot node, Object data) { // NOT
		return doChild(node, 0).not();
	}
	public Object visit(ASTUnaryPlus node, Object data) { // + (unary)
		return doChild(node, 0).unary_plus();
	}
	public Object visit(ASTUnaryMinus node, Object data) { // - (unary)
		return doChild(node, 0).unary_minus();
	}

	/**
	 * Function invocation in an expression.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTFunctionInvocation node, Object data) {
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
	 * Try finding the ValueFn inside the scope and extract its FunctionDefinition.
	 * 
	 * @author amrwc
	 */
	private FunctionDefinition findValueFn(String fnname) {
		Reference reference = scope.findReference(fnname);
		if (reference == null)
			// Find a constant of the same name.
			reference = scope.findReference("constant" + fnname);
		if (reference == null)
			throw new ExceptionSemantic("Function " + fnname + " is undefined.");

		ValueFn valueFunction = (ValueFn) reference.getValue();
		return valueFunction.get(); // Extract the FunctionDefinition stored in ValueFn.
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
				// Find a constant of the same name.
				reference = scope.findReference("constant" + name);
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
				else if (value instanceof ValueString)
					value = stringDereference(node, value, currChild);
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
		var keyName = node.jjtGetChild(currChild) instanceof ASTIdentifier
			? getTokenOfChild(node, currChild)
			: doChild(node, currChild).toString();
		return valueObject.get(keyName);
	}

	/**
	 * Dereference of a character in a ValueString.
	 * 
	 * @author amrwc
	 */
	private Value stringDereference(SimpleNode node, Value v, int currChild) {
		ValueString valueString = (ValueString) v;
		int index = (int) ((ValueInteger) doChild(node, currChild)).longValue();
		String str = "" + ((ValueString) valueString).stringValue().charAt(index);
		return new ValueString(str);
	}

	/**
	 * Returns command-line arguments as a ValueList.
	 * 
	 * @returns {ValueList} args
	 * @author amrwc
	 */
	public Object visit(ASTGetArgs node, Object data) {
		ValueList args = new ValueList();
		for (String arg: argv)
			args.append(new ValueString(arg));
		if (args.size() == 0)
			System.out.println("Warning: The program asked for command-line arguments, "
				+ "but none were passed in.");
		return args;
	}

	/**
	 * Returns a random value between min inclusive and max exclusive.
	 * 
	 * @param (child0) {int/float/ValueInteger/ValueRational} min
	 * @param (child1) {int/float/ValueInteger/ValueRational} max
	 * @returns random value in range
	 * @author amrwc
	 */
	public Object visit(ASTRandom node, Object data) {
		Value min = doChild(node, 0);
		Value max = doChild(node, 1);

		if (min instanceof ValueRational || max instanceof ValueRational) {
			double minDouble = min.doubleValue();
			double maxDouble = max.doubleValue();
			double result = ThreadLocalRandom.current().nextDouble(minDouble, maxDouble);
			return new ValueRational(result);
		}
		
		else if (min instanceof ValueInteger && max instanceof ValueInteger) {
			long minLong = min.longValue();
			long maxLong = max.longValue();
			long result = ThreadLocalRandom.current().nextLong(minLong, maxLong);
			return new ValueInteger(result);
		}

		else
			throw new ExceptionSemantic("The random() function requires the arguments"
					+ "to be either of ValueInteger or ValueRational type.");
	}



	/***********************************************
	 *                 Literals                    *
	 ***********************************************/

	// Return integer literal
	public Object visit(ASTInteger node, Object data) {
		if (node.optimised == null)
			node.optimised = new ValueInteger(Long.parseLong(node.tokenValue));
		return node.optimised;
	}

	// Return string literal
	public Object visit(ASTCharacter node, Object data) {
		if (node.optimised == null)
			node.optimised = ValueString.stripDelimited(node.tokenValue);
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
	 * Anonymous function declaration.
	 * 
	 * @author amrwc
	 */
	public Object visit(ASTValueFunction node, Object data) {
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
}
