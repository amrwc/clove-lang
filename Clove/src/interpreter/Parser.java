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
	private final Display scope = new Display(); // Scope display handler

	public Parser() {
	}

	public Parser(String[] args) {
		argv = args;
	}

	// Get the ith child of a given node.
	private static SimpleNode getChild(SimpleNode node, int childIndex) {
		return (SimpleNode) node.jjtGetChild(childIndex);
	}

	// Get the token value of the ith child of a given node.
	public static String getTokenOfChild(SimpleNode node, int childIndex) {
		return getChild(node, childIndex).tokenValue;
	}

	// Execute a given child of the given node
	public Object doChild(SimpleNode node, int childIndex, Object data) {
		return node.jjtGetChild(childIndex).jjtAccept(this, data);
	}

	// Execute a given child of a given node, and return its value as a Value.
	// This is used by the expression evaluation nodes.
	public Value doChild(SimpleNode node, int childIndex) {
		return (Value) doChild(node, childIndex, null);
	}

	// Execute all children of the given node
	Object doChildren(SimpleNode node, Object data) {
		return node.childrenAccept(this, data);
	}

	// Called if one of the following methods is missing...
	@Override
	public Object visit(SimpleNode node, Object data) {
		System.out.println(node + ": acceptor not implemented in subclass?");
		return data;
	}

	// Execute a Clove program
	@Override
	public Object visit(ASTCode node, Object data) {
		return doChildren(node, data);
	}






	/***********************************************
	 * Statements *
	 ***********************************************/

	// Execute a statement
	@Override
	public Object visit(ASTStatement node, Object data) {
		return doChildren(node, data);
	}

	/**
	 * Function call.
	 * 
	 * @author amrwc
	 */
	@Override
	public Object visit(ASTCall node, Object data) {
		FunctionDefinition fndef;
		final int leftNumChildren = node.jjtGetChild(0).jjtGetNumChildren();

		// If there's more than 1 child in the left child, then it's not just an
		// identifier.
		if (leftNumChildren > 0) {
			// Check for and call a Reflection Method.
			final Value perhapsReflection = doChild(node, 0);
			if (perhapsReflection instanceof ValueReflection) {
				try {
					((ValueReflection) perhapsReflection).invoke(node, this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			fndef = getValueFunction(node);
			node.optimised = fndef;
		}

		if (node.optimised == null) {
			final String fnname = getTokenOfChild(node, 0); // Child 0 - identifier (fn name)
			fndef = scope.findFunction(fnname);
			if (fndef == null)
				fndef = findValueFunction(fnname);

			node.optimised = fndef; // Save it for next time
		} else
			fndef = (FunctionDefinition) node.optimised;

		final FunctionInvocation newInvocation = new FunctionInvocation(fndef);
		doChild(node, 1, newInvocation); // Child 1 - arglist
		scope.execute(newInvocation, this); // Execute

		return data;
	}

	/**
	 * Definition using the <LET> and <CONST> keywords.
	 * 
	 * @author amrwc
	 */
	@Override
	public Object visit(ASTDefinition node, Object data) {
		final Node initialisation = node.jjtGetChild(0);
		final String name = getTokenOfChild((SimpleNode) initialisation, 0);

		if (scope.findReference(name) == null && scope.findReference("constant" + name) == null) {
			switch (node.defType) {
			case "variable":
				scope.defineVariable(name);
				break;
			case "constant":
				scope.defineConstant(name);
			}
			initialisation.jjtAccept(this, data); // Do the initialisation.
		} else
			throw new ExceptionSemantic("Variable or constant \"" + name + "\" already exists.");

		return data;
	}

	/**
	 * Declaration of a variable.
	 * 
	 * @author amrwc
	 */
	@Override
	public Object visit(ASTDeclaration node, Object data) {
		final String name = getTokenOfChild(node, 0);

		if (node.defType == "constant")
			throw new ExceptionSemantic("Constants must be initialised." + " Change the \"const\" keyword before \""
					+ name + "\" to \"let\".");

		if (scope.findReference(name) != null)
			throw new ExceptionSemantic("Variable \"" + name + "\" already exists.");

		// Define the variable and get the reference.
		final Display.Reference ref = scope.defineVariable(name);

		// If the declaration has an add_expression() in brackets,
		// it's a ValueArray declaration with an explicit capacity.
		if (node.isArrayWithCap == true) {
			final int capacity = (int) doChild(node, 1).getRawValue();
			ref.setValue(new ValueArray(capacity));
		}

		// Otherwise, if it's just an array declaration, set an empty ValueArray
		// with 0 capacity.
		else if (node.isArrayDeclaration == true)
			ref.setValue(new ValueArray(0));

		return data;
	}

	/**
	 * Execute an assignment statement.
	 * 
	 * @author amrwc
	 */
	@Override
	public Object visit(ASTAssignment node, Object data) {
		Display.Reference reference;
		final int numChildren = node.jjtGetNumChildren();

		Value rightVal = doChild(node, numChildren - 1);
		if (rightVal == null)
			throw new ExceptionSemantic("Right value of the assignment cannot resolve to null.");

		// Get the reference of the parent value.
		if (node.optimised == null) {
			final String name = getTokenOfChild(node, 0);
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
			int currChild = 1; // Keep track of how far it traversed.

			// -2 := the parent of the rightmost value to the left
			// of the assignment operator.
			final int limit = numChildren - 2;

			// ...traverse through the dereference to find the parent of
			// the rightmost value to the left of the assignment operator...
			for (; currChild < limit; currChild++)
				value = value.dereference(node, value, currChild, this);

			// Handle a shorthand operator on a dereferenced variable.
			if (node.shorthandOperator != null) {
				// If the shorthand operator is present, get the rightmost
				// value of the dereference (the deepest dereference of Lvalue).
				final Value v = value.dereference(node, value, currChild, this);

				// Update the Rvalue that will be assigned.
				rightVal = doShorthand(node.shorthandOperator, v, rightVal);
			}

			// ...and reassign the value of the list...
			if (value instanceof ValueList) {
				final int index = ((ValueInteger) doChild(node, numChildren - 2)).getRawValue();
				((ValueList) value).set(index, rightVal);
			}
			// ...or an array's value...
			else if (value instanceof ValueArray) {
				final int index = ((ValueInteger) doChild(node, numChildren - 2)).getRawValue();
				((ValueArray) value).set(index, rightVal);
			}
			// ...or an object's key.
			else if (value instanceof ValueObject) {
				final String keyName = node.jjtGetChild(numChildren - 2) instanceof ASTIdentifier
						? getTokenOfChild(node, numChildren - 2)
						: doChild(node, numChildren - 2).toString();
				((ValueObject) value).set(keyName, rightVal);
			}
		}

		// Normal variable (1 child in L-value).
		else {
			// Handle a shorthand operator on a normal variable.
			if (node.shorthandOperator != null) {
				final Value value = reference.getValue();
				final Value result = doShorthand(node.shorthandOperator, value, rightVal);
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
	 * @param ref      -- variable reference
	 * @param val      -- base value (operand)
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
			throw new ExceptionSemantic(
					"Operator \"" + operator + "\" cannot be used on " + val + " and " + rightVal + ".");
		}
	}

	// Function definition
	@Override
	public Object visit(ASTFunctionDefinition node, Object data) {
		// Already defined?
		if (node.optimised != null)
			return data;
		// Child 0 - identifier (fn name)
		final String fnname = getTokenOfChild(node, 0);
		if (scope.findFunctionInCurrentLevel(fnname) != null)
			throw new ExceptionSemantic("Function " + fnname + " already exists.");
		final FunctionDefinition currentFunctionDefinition = new FunctionDefinition(fnname, scope.getLevel() + 1);
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
	@Override
	public Object visit(ASTBlock node, Object data) {
		final Object result = doChildren(node, data);
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
		final ArrayList<SimpleNode> definitions = collectDefinitions(node, init);

		final Consumer<SimpleNode> removeDefinition = definition -> {
			if (definition instanceof ASTDefinition) {
				final SimpleNode initNode = (SimpleNode) definition.jjtGetChild(0);
				final String variableName = getTokenOfChild(initNode, 0);
				if (scope.findReference("constant" + variableName) != null)
					scope.removeVariable("constant" + variableName);
				else
					scope.removeVariable(variableName);
			} else if (definition instanceof ASTFunctionDefinition) {
				final String fnName = getTokenOfChild(definition, 0);
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
		final ArrayList<SimpleNode> definitions = new ArrayList<SimpleNode>();

		// Handle raw block() that occurs without any preceding statement such as
		// for()/if().
		if (node instanceof ASTBlock) {
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				final Node innerNode = node.jjtGetChild(i).jjtGetChild(0);
				if (innerNode instanceof ASTDefinition || innerNode instanceof ASTFunctionDefinition)
					definitions.add((SimpleNode) innerNode);
			}
			return definitions;
		}

		// If it's a for-loop including an initialisation node...
		if (init != null && init instanceof ASTDefinition)
			definitions.add(init);

		// Handle statement()/block() without children.
		if (node.jjtGetNumChildren() == 0)
			return definitions;

		// Collect the possible single definition from the statement().
		final Node statement = node.jjtGetChild(node.jjtGetNumChildren() - 1);

		// statement() -> definition()/fndef() -- there can only be one, since it's not
		// a block.
		final Node innerNode = statement.jjtGetChild(0);
		if (innerNode instanceof ASTDefinition || innerNode instanceof ASTFunctionDefinition)
			definitions.add((SimpleNode) innerNode);

		return definitions;
	}

	/**
	 * Execute an if statement.
	 * 
	 * @author amrwc
	 */
	@Override
	public Object visit(ASTIfStatement node, Object data) {
		final Value test = doChild(node, 0);
		if (!(test instanceof ValueBoolean))
			throw new ExceptionSemantic("The test expression of an if statement must be boolean.");

		if (((ValueBoolean) test).getRawValue()) // If test evaluated to true...
			doChild(node, 1); // ...do 'if'. Or...
		else if (node.ifHasElse) // ...if it evaluated to false and has 'else'...
			doChild(node, 2); // ...do 'else'.

		removeDefinitions(node);
		return data;
	}

	/**
	 * Execute a for loop.
	 * 
	 * @author amrwc
	 */
	@Override
	public Object visit(ASTForLoop node, Object data) {
		doChild(node, 0); // Initialise the loop (usually 'let i = 0').

		while (true) {
			final Value loopTest = doChild(node, 1);
			if (!(loopTest instanceof ValueBoolean))
				throw new ExceptionSemantic("The test expression of a for loop must be boolean.");

			if (!((ValueBoolean) loopTest).getRawValue()) // If loopTest evaluated to false, break.
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
	@Override
	public Object visit(ASTWhileLoop node, Object data) {
		while (true) {
			final Value loopTest = doChild(node, 0);
			if (!(loopTest instanceof ValueBoolean))
				throw new ExceptionSemantic("The test expression of a while loop must be boolean.");

			if (!((ValueBoolean) loopTest).getRawValue()) // If loopTest evaluated to false, break.
				break;

			doChild(node, 1); // Do loop statement()/block().
			removeDefinitions(node); // Clean up the variable/function definitions.
		}

		return data;
	}

	/**
	 * Execute the WRITE statement. Prints out all given arguments.
	 * 
	 * @author amrwc
	 */
	@Override
	public Object visit(ASTWrite node, Object data) {
		final int numChildren = node.jjtGetNumChildren();
		for (int i = 0; i < numChildren; i++)
			System.out.print(doChild(node, i));
		System.out.println();
		return data;
	}

	/**
	 * Execute QUIT statement. Prints out all given arguments.
	 * 
	 * @author amrwc
	 */
	@Override
	public Object visit(ASTQuit node, Object data) {
		final int numChildren = node.jjtGetNumChildren();
		for (int i = 0; i < numChildren; i++)
			System.out.print(doChild(node, i));
		System.out.println();
		System.exit(0);
		return null;
	}

	/**
	 * Prototype function invocation.
	 * 
	 * @author amrwc
	 */
	@Override
	public Object visit(ASTProtoInvoke node, Object data) {
		final Value value = doChild(node, 0);
		final String protoFunc = node.tokenValue;
		final ArrayList<Value> protoArgs = (node.jjtGetNumChildren() > 1) ? parseProtoArgs(node) : null;

		return value.execProto(protoFunc, protoArgs);
	}

	/**
	 * Parse the prototype function's arguments.
	 * 
	 * @param node -- proto_invoke() == ASTProtoInvoke
	 * @author amrwc
	 */
	private ArrayList<Value> parseProtoArgs(SimpleNode node) {
		final ArrayList<Value> values = new ArrayList<Value>();
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
	@Override
	public Object visit(ASTIncrementDecrement node, Object data) {
		Display.Reference ref;
		final String name = getTokenOfChild(node, 0);
		final int numChildren = node.jjtGetNumChildren();
		final ValueInteger one = new ValueInteger(1);

		// Try finding a variable or a constant.
		if ((ref = scope.findReference(name)) == null)
			ref = scope.findReference("constant" + name);
		if (ref == null)
			throw new ExceptionSemantic("Variable \"" + name + "\" is undefined.");

		Value value = ref.getValue();

		// If it's a compound value...
		if (numChildren > 1) {
			int currChild = 0; // Keep track how far it traversed.

			// -2 := the parent of the rightmost value to the left
			// of the assignment operator.
			final int limit = numChildren - 2;

			// ...traverse through the dereference to find the parent of
			// the rightmost value...
			for (; currChild < limit; currChild++)
				value = value.dereference(node, value, currChild + 1, this);

			// ...and reassign the value of the compound value...
			switch (node.shorthandOperator) {
			case "pre++":
				return doIncDecNested(node, numChildren, value, "pre++");
			case "pre--":
				return doIncDecNested(node, numChildren, value, "pre--");
			case "post++":
				return doIncDecNested(node, numChildren, value, "post++");
			case "post--":
				return doIncDecNested(node, numChildren, value, "post--");
			default:
				throw new ExceptionSemantic(
						"Operator \"" + node.shorthandOperator + "\" cannot be used on " + value + " and " + one + ".");
			}
		}

		// Handle a normal variable.
		switch (node.shorthandOperator) {
		case "pre++":
			ref.setValue(value.add(one));
			return ref.getValue();
		case "pre--":
			ref.setValue(value.subtract(one));
			return ref.getValue();
		case "post++":
			ref.setValue(value.add(one));
			return value;
		case "post--":
			ref.setValue(value.subtract(one));
			return value;
		default:
			throw new ExceptionSemantic(
					"Operator \"" + node.shorthandOperator + "\" cannot be used on " + value + " and " + one + ".");
		}
	}

	/**
	 * Reassigns the rightmost value and returns the new or old value depending on
	 * pre/post-fix operator.
	 * 
	 * @param node
	 * @param numChildren
	 * @param value       -- value to be changed
	 * @param operation   -- "post++"/"post--"/"pre++"/"pre--"
	 * @returns updated value or old value
	 */
	private Value doIncDecNested(SimpleNode node, int numChildren, Value value, String operation) {
		Value old = null;
		final ValueInteger one = new ValueInteger(1);

		if (value instanceof ValueList) {
			final ValueList list = (ValueList) value; // Cast value to an appropriate class.
			final int index = ((ValueInteger) doChild(node, numChildren - 1)).getRawValue();
			old = list.get(index);

			if (operation.contains("++"))
				list.set(index, old.add(one)); // Do ++
			else
				list.set(index, old.subtract(one)); // Do --

			// If it's a prefix operation, return the new value immediately.
			if (operation.equals("pre++") || operation.equals("pre--"))
				return list.get(index);
		}

		else if (value instanceof ValueArray) {
			final ValueArray array = (ValueArray) value;
			final int index = ((ValueInteger) doChild(node, numChildren - 1)).getRawValue();
			old = array.get(index);

			if (operation.contains("++"))
				array.set(index, old.add(one));
			else
				array.set(index, old.subtract(one));

			if (operation.equals("pre++") || operation.equals("pre--"))
				return array.get(index);
		}

		else if (value instanceof ValueObject) {
			final ValueObject object = (ValueObject) value;
			final String keyName = node.jjtGetChild(numChildren - 1) instanceof ASTIdentifier
					? getTokenOfChild(node, numChildren - 1)
					: doChild(node, numChildren - 1).toString();
			old = object.get(keyName);

			if (operation.contains("++"))
				object.set(keyName, old.add(one));
			else
				object.set(keyName, old.subtract(one));

			if (operation.equals("pre++") || operation.equals("pre--"))
				return object.get(keyName);
		}

		return old;
	}

	/**
	 * Sends an HTTP request and returns the response.
	 * 
	 * @returns {ValueObject} HTTP response code and body
	 * @author amrwc
	 */
	@Override
	public Object visit(ASTHttp node, Object data) {
		final String method = doChild(node, 0).toString().toUpperCase();
		final String url = doChild(node, 1).toString();
		String body = null;

		switch (method) {
		case "GET":
		case "DELETE": {
			return doHttpReq(method, url);
		}
		case "POST":
		case "PUT": {
			if (node.jjtGetNumChildren() < 3) {
				throw new ExceptionSemantic("The \"" + method + "\" HTTP method needs a request body.");
			} else {
				final var tryBody = doChild(node, 2); // String/ValueObject

				// If the third (request body) argument is an anonymous object...
				if (tryBody instanceof ValueObject) {
					// ...turn the key-value pairs into url-encoded pairs.
					body = ((ValueObject) tryBody).toUrlString();
				} else {
					body = tryBody.toString();
				}
			}
			return doHttpReq(method, url, body);
		}
		default:
			throw new ExceptionSemantic("The http function doesn't support \"" + method + "\" method.");
		}
	}

	/**
	 * Sends an HTTP request and returns its response in a ValueObject.
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
		final ValueObject res = new ValueObject();
		res.add("code", null);
		res.add("body", null);

		try {
			url = new URL(requestURL);
			final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(15000);
			conn.setConnectTimeout(15000);

			switch (method) {
			case "GET":
			case "DELETE":
				break;
			case "POST":
			case "PUT": {
				if (data == null)
					throw new ExceptionSemantic(
							"The \"" + method + "\" method's request body cannot evaluate to null.");

				conn.setRequestMethod(method);
				conn.setDoOutput(true);

				final OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
				out.write(data);
				out.flush();
				out.close();
				break;
			}
			default:
				throw new ExceptionSemantic("The \"" + method + "\" method is not supported by the http function.");
			}
			final int responseCode = conn.getResponseCode();
			res.set("code", new ValueInteger(responseCode));

			if (responseCode == 200 || responseCode == 201) {
				String line;
				final BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				while ((line = br.readLine()) != null)
					responseBody += line.strip() + "\n";
				br.close();
			}
		} catch (final Exception e) {
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
	 * Example usage: file("append", "file.txt", content) ...where content can be
	 * any Value type.
	 * 
	 * @read https://stackoverflow.com/a/23221771/10620237
	 * @param (child0) {String} option ("create"/"overwrite"|"open"/"append")
	 * @param (child1) {String/ValueString} path
	 * @param (child2) {String/Value} content
	 * @returns path to the file
	 * @author amrwc
	 */
	@Override
	public Object visit(ASTFile node, Object data) {
		final String option = doChild(node, 0).toString();
		final String pathStr = doChild(node, 1).toString();
		final Path path = Paths.get(pathStr);
		final Path parentDir = path.getParent();
		final Value content = doChild(node, 2);

		// Transform the content to an Iterable; end it with a new line.
		final List<String> lines = Arrays.asList(content.toString(), "");
		final Charset utf8 = StandardCharsets.UTF_8;

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
				Files.write(path, lines, utf8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
				break;
			default:
				throw new ExceptionSemantic("There is no \"" + option + "\" option in the file function.");
			}
		} catch (final Exception e) {
			System.err.println("Problem writing to the \"" + pathStr + "\" file.");
			e.printStackTrace();
		}

		return new ValueString(pathStr);
	}






	/***********************************************
	 * Sub-statements *
	 ***********************************************/

	// Process an identifier
	// This doesn't do anything, but needs to be here because we need an
	// ASTIdentifier node.
	@Override
	public Object visit(ASTIdentifier node, Object data) {
		return data;
	}

	/**
	 * Initialise a constant.
	 * 
	 * NOTE: It's separated from assignment, to separate the concerns. This way
	 * constants can be safely defined and initialised.
	 * 
	 * @author amrwc
	 */
	@Override
	public Object visit(ASTConstInit node, Object data) {
		final String name = getTokenOfChild(node, 0);
		final Display.Reference reference = scope.findReference("constant" + name);
		final Value rightVal = doChild(node, 1);
		if (rightVal == null)
			throw new ExceptionSemantic("Right value of the constant's initialisation cannot resolve to null.");

		reference.setValue(rightVal);
		return data;
	}

	@Override
	public Object visit(ASTArrayInit node, Object data) {
		final String name = getTokenOfChild(node, 0);
		Display.Reference reference = scope.findReference(name);
		// If the array is being defined as a constant...
		if (reference == null)
			reference = scope.findReference("constant" + name);

		int initValNum = -1;
		int capacity = -1;
		int currChild = -1;
		if (node.isArrayWithCap == true) {
			// Get the number of values in the initialisation.
			// -2 -- the first two children are identifier() and capacity.
			initValNum = node.jjtGetNumChildren() - 2;

			// Get the array's initial capacity from the explicitly specified
			// capacity (child index 1),
			capacity = (int) doChild(node, 1).getRawValue();

			// currChild := 2 -- the values start from the third child.
			currChild = 2;
		} else {
			// -1 -- the first child is an identifier().
			initValNum = node.jjtGetNumChildren() - 1;

			// Capacity is implicitly set with the number of values on initialisation.
			capacity = initValNum;

			// currChild := 1 -- the values start from the second child.
			currChild = 1;
		}

		// If there's more values than the array can store...
		if (initValNum > capacity)
			throw new ExceptionSemantic("There is more initial values for \"" + name + "\" array (" + initValNum
					+ ") than its capacity (" + capacity + ").");

		// Initialise an empty array with the specified capacity.
		final ValueArray valueArray = new ValueArray(capacity);

		// Add all the values to the array.
		Value currentValue;
		for (; currChild < node.jjtGetNumChildren(); currChild++) {
			currentValue = doChild(node, currChild);
			valueArray.append(currentValue);
		}

		reference.setValue(valueArray);
		return data;
	}

	// Function invocation argument list.
	@Override
	public Object visit(ASTArgumentList node, Object data) {
		final FunctionInvocation newInvocation = (FunctionInvocation) data;
		for (int i = 0; i < node.jjtGetNumChildren(); i++)
			newInvocation.setArgument(doChild(node, i));
		newInvocation.checkArgumentCount();
		return data;
	}

	// Function definition parameter list
	@Override
	public Object visit(ASTParameterList node, Object data) {
		final FunctionDefinition currentDefinition = (FunctionDefinition) data;
		for (int i = 0; i < node.jjtGetNumChildren(); i++)
			currentDefinition.defineParameter(getTokenOfChild(node, i));
		return data;
	}

	// Function body
	@Override
	public Object visit(ASTFunctionBody node, Object data) {
		return doChildren(node, data);
	}

	// Function return expression
	@Override
	public Object visit(ASTReturnExpression node, Object data) {
		return doChildren(node, data);
	}






	/***********************************************
	 * Expressions *
	 ***********************************************/

	@Override
	public Object visit(ASTOr node, Object data) { // OR
		return doChild(node, 0).or(doChild(node, 1));
	}

	@Override
	public Object visit(ASTAnd node, Object data) { // AND
		return doChild(node, 0).and(doChild(node, 1));
	}

	@Override
	public Object visit(ASTCompEqual node, Object data) { // ==
		return doChild(node, 0).eq(doChild(node, 1));
	}

	@Override
	public Object visit(ASTCompNequal node, Object data) { // !=
		return doChild(node, 0).neq(doChild(node, 1));
	}

	@Override
	public Object visit(ASTCompGTE node, Object data) { // >=
		return doChild(node, 0).gte(doChild(node, 1));
	}

	@Override
	public Object visit(ASTCompLTE node, Object data) { // <=
		return doChild(node, 0).lte(doChild(node, 1));
	}

	@Override
	public Object visit(ASTCompGT node, Object data) { // >
		return doChild(node, 0).gt(doChild(node, 1));
	}

	@Override
	public Object visit(ASTCompLT node, Object data) { // <
		return doChild(node, 0).lt(doChild(node, 1));
	}

	@Override
	public Object visit(ASTAdd node, Object data) { // +
		return doChild(node, 0).add(doChild(node, 1));
	}

	@Override
	public Object visit(ASTSubtract node, Object data) { // -
		return doChild(node, 0).subtract(doChild(node, 1));
	}

	@Override
	public Object visit(ASTTimes node, Object data) { // *
		return doChild(node, 0).mult(doChild(node, 1));
	}

	@Override
	public Object visit(ASTDivide node, Object data) { // /
		return doChild(node, 0).div(doChild(node, 1));
	}

	@Override
	public Object visit(ASTModulo node, Object data) { // %
		return doChild(node, 0).mod(doChild(node, 1));
	}

	@Override
	public Object visit(ASTUnaryNot node, Object data) { // NOT
		return doChild(node, 0).not();
	}

	@Override
	public Object visit(ASTUnaryPlus node, Object data) { // + (unary)
		return doChild(node, 0).unary_plus();
	}

	@Override
	public Object visit(ASTUnaryMinus node, Object data) { // - (unary)
		return doChild(node, 0).unary_minus();
	}

	/**
	 * Function invocation in an expression.
	 * 
	 * @author amrwc
	 */
	@Override
	public Object visit(ASTFunctionInvocation node, Object data) {
		FunctionDefinition fndef;
		final int leftNumChildren = node.jjtGetChild(0).jjtGetNumChildren();

		// If there's more than 1 child in the left child,
		// then it's not just an identifier.
		if (leftNumChildren > 0) {
			// Check for and invoke a Reflection Method.
			final Value perhapsReflection = doChild(node, 0);
			if (perhapsReflection instanceof ValueReflection) {
				try {
					return ((ValueReflection) perhapsReflection).invoke(node, this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			fndef = getValueFunction(node);
			node.optimised = fndef;
		}

		if (node.optimised == null) {
			final String fnname = getTokenOfChild(node, 0);
			fndef = scope.findFunction(fnname);
			if (fndef == null)
				fndef = findValueFunction(fnname);

			if (!fndef.hasReturn())
				throw new ExceptionSemantic("Function " + fnname + " is being"
					+ " invoked in an expression but does not have a return value.");

			node.optimised = fndef; // Save it for next time
		} else
			fndef = (FunctionDefinition) node.optimised;

		final FunctionInvocation newInvocation = new FunctionInvocation(fndef);
		doChild(node, 1, newInvocation); // Child 1 - arglist

		return scope.execute(newInvocation, this); // Execute and return the outcome.
	}

	/**
	 * Try finding the ValueFunction inside the scope and extract its
	 * FunctionDefinition.
	 * 
	 * @author amrwc
	 */
	private FunctionDefinition findValueFunction(String fnname) {
		Reference reference = scope.findReference(fnname);
		if (reference == null)
			// Find a constant of the same name.
			reference = scope.findReference("constant" + fnname);
		if (reference == null)
			throw new ExceptionSemantic("Function " + fnname + " is undefined.");

		final ValueFunction valueFunction = (ValueFunction) reference.getValue();
		return valueFunction.get(); // Extract the FunctionDefinition stored in ValueFunction.
	}

	/**
	 * Retrieves ValueFunction from a dereference and returns its
	 * FunctionDefinition.
	 * 
	 * @param node
	 * @author amrwc
	 */
	private FunctionDefinition getValueFunction(SimpleNode node) {
		final Value value = doChild(node, 0); // Do the dereference.

		final ValueFunction valueFunction = (ValueFunction) value;
		if (valueFunction == null)
			throw new ExceptionSemantic("The value function you are trying"
				+ " to invoke is undefined.");

		final FunctionDefinition fndef = valueFunction.get();
		if (fndef == null)
			throw new ExceptionSemantic("Function " + valueFunction.getName()
				+ " is undefined.");

		return fndef;
	}

	/**
	 * Dereference a variable or parameter, and return its value., or call/invoke a
	 * nested function.
	 * 
	 * @author amrwc
	 */
	@Override
	public Object visit(ASTDereference node, Object data) {
		Display.Reference reference;

		if (node.optimised == null) {
			// Get the main variable's/parameter's name (token).
			final String name = node.tokenValue;
			reference = scope.findReference(name);
			if (reference == null)
				// Find a constant of the same name.
				reference = scope.findReference("constant" + name);
			if (reference == null)
				throw new ExceptionSemantic("Variable or parameter \"" + name + "\" is undefined.");
			node.optimised = reference;
		} else
			reference = (Display.Reference) node.optimised;

		final int numChildren = node.jjtGetNumChildren();
		if (numChildren > 0) { // If it's not a normal dereference of a variable...
			int currChild = 0; // Keep track of how far it traversed.
			Value value = reference.getValue();

			// ...traverse through the chain of dereferences.
			for (; currChild < numChildren; currChild++)
				value = value.dereference(node, value, currChild, this);

			return value;
		}

		return reference.getValue();
	}

	/**
	 * Returns command-line arguments as a ValueList.
	 * 
	 * @returns {ValueList} args
	 * @author amrwc
	 */
	@Override
	public Object visit(ASTGetArgs node, Object data) {
		final ValueList args = new ValueList();
		for (final String arg : argv)
			args.append(new ValueString(arg));
		if (args.size() == 0)
			System.out.println("Warning: The program asked for command-line arguments, " + "but none were passed in.");
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
	@Override
	public Object visit(ASTRandom node, Object data) {
		final Value min = doChild(node, 0);
		final Value max = doChild(node, 1);

		if (min instanceof ValueRational || max instanceof ValueRational) {
			final double minDouble = min.doubleValue();
			final double maxDouble = max.doubleValue();
			final double result = ThreadLocalRandom.current().nextDouble(minDouble, maxDouble);
			return new ValueRational(result);
		}

		else if (min instanceof ValueInteger && max instanceof ValueInteger) {
			final int minLong = min.getRawValue();
			final int maxLong = max.getRawValue();
			final int result = ThreadLocalRandom.current().nextInt(minLong, maxLong);
			return new ValueInteger(result);
		}

		else
			throw new ExceptionSemantic("The random() function requires the"
				+ " arguments to be either of ValueInteger or ValueRational type.");
	}

	/**
	 * Instantiates a class requested at run-time
	 * and stores it in the ValueReflection type.
	 * 
	 * @author amrwc
	 */
	@Override
	public Object visit(ASTReflect node, Object data) {
		final String className = doChild(node, 0).stringValue();
		final int numChildren = node.jjtGetNumChildren();

		if (numChildren > 2)
			throw new ExceptionSemantic("ValueReflection only accepts"
				+ " up to 2 arguments.");

		try {
			// If there's only a class name...
			if (numChildren == 1)
				return new ValueReflection(className);

			// ...or if there's a class name and constructor arguments...
			else if (numChildren == 2) {
				// Get all constructor arguments.
				ValueList ctorArgsValue = (ValueList) doChild(node, 1);
				final Value[] ctorArgs = new Value[ctorArgsValue.size()];
				for (int i = 0; i < ctorArgsValue.size(); i++)
					ctorArgs[i] = ctorArgsValue.get(i);

				return new ValueReflection(className, ctorArgs);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return data;
	}






	/***********************************************
	 * Literals *
	 ***********************************************/

	// Return integer literal
	@Override
	public Object visit(ASTInteger node, Object data) {
		if (node.optimised == null)
			node.optimised = new ValueInteger(Integer.parseInt(node.tokenValue));
		return node.optimised;
	}

	// Return string literal
	@Override
	public Object visit(ASTCharacter node, Object data) {
		if (node.optimised == null)
			node.optimised = ValueString.stripDelimited(node.tokenValue);
		return node.optimised;
	}

	// Return floating point literal
	@Override
	public Object visit(ASTRational node, Object data) {
		if (node.optimised == null)
			node.optimised = new ValueRational(Double.parseDouble(node.tokenValue));
		return node.optimised;
	}

	// Return true literal
	@Override
	public Object visit(ASTTrue node, Object data) {
		if (node.optimised == null)
			node.optimised = new ValueBoolean(true);
		return node.optimised;
	}

	// Return false literal
	@Override
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
	@Override
	public Object visit(ASTValueFunction node, Object data) {
		final FunctionDefinition currentFnDef = new FunctionDefinition(scope.getLevel() + 1);

		// Child 0 -- function definition parameter list
		doChild(node, 0, currentFnDef);
		// Child 1 -- function body
		currentFnDef.setFunctionBody(getChild(node, 1));
		// Child 2 -- optional return expression
		if (node.fnHasReturn)
			currentFnDef.setFunctionReturnExpression(getChild(node, 2));

		return new ValueFunction(currentFnDef);
	}

	/**
	 * Anonymous object literal.
	 * 
	 * @author amrwc
	 */
	@Override
	public Object visit(ASTValueObject node, Object data) {
		final ValueObject valueObject = new ValueObject();

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
	 * List literal.
	 * 
	 * @author amrwc
	 */
	@Override
	public Object visit(ASTValueList node, Object data) {
		final ValueList valueList = new ValueList();

		// Add all the values to the list.
		Value currentValue;
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			currentValue = doChild(node, i);
			valueList.append(currentValue);
		}

		return valueList;
	}

	/**
	 * Reflection literal with a cast.
	 * 
	 * @author amrwc
	 */
	@Override
	public Object visit(ASTValueReflection node, Object data) {
		final String targetClassName = doChild(node, 0).stringValue();
		final Value objToCast = doChild(node, 1);

		if (objToCast instanceof ValueReflection)
			return ValueReflection.cast(targetClassName, (ValueReflection) objToCast);
		
		else
			throw new ExceptionSemantic("The object to cast must be of"
				+ " ValueReflection type.");
	}
}
