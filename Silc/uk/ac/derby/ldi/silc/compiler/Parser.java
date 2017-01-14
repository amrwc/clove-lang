package uk.ac.derby.ldi.silc.compiler;

import uk.ac.derby.ldi.silc.parser.*;

public class Parser implements SilcVisitor {

	// Code generator
	private Generator generator;
	
	/** Ctor */
	public Parser() {
		generator = new Generator();
	}
	
	// Compile a given child of the given node
	private Object compileChild(SimpleNode node, int childIndex, Object data) {
		return node.jjtGetChild(childIndex).jjtAccept(this, data);
	}
	
	// Compile all children of the given node
	private Object compileChildren(SimpleNode node, Object data) {
		return node.childrenAccept(this, data);
	}

	// Get the ith child as a BaseASTNode
	private static BaseASTNode getChild(SimpleNode node, int childIndex) {
		return (BaseASTNode)node.jjtGetChild(childIndex);
	}
	
	public Object visit(SimpleNode node, Object data) {
		System.out.println(node + ": acceptor not implemented in subclass?");
		return data;
	}
	
	// Compile a Sili program, and return a ValueObject that can be executed by a VirtualMachine.
	public Object visit(ASTCode node, Object data) {
		generator.beginCompilation();
		// Compile all children of this node, which compiles the whole program.
		compileChildren(node, data);
		return generator.endCompilation();
	}
	
	// Compile a statement
	public Object visit(ASTStatement node, Object data) {
		return compileChildren(node, data);
	}

	// Compile a block
	public Object visit(ASTBlock node, Object data) {
		return compileChildren(node, data);	
	}
	
	// Function definition
	public Object visit(ASTFnDef node, Object data) {
		// Child 0 - identifier (fn name)
		compileChild(node, 0, data);
		String fnname = getChild(node, 0).tokenValue;
		generator.beginOperator(fnname);
		// Child 1 - function definition parameter list
		compileChild(node, 1, data);
		// Child 2 - function body
		compileChild(node, 2, data);
		// Child 3 - optional return expression
		if (node.fnHasReturn)
			compileChild(node, 3, data);
		generator.endOperator();
		return data;
	}
	
	// Function definition parameter list
	public Object visit(ASTParmlist node, Object data) {
		compileChildren(node, data);
		generator.beginParameterDefinitions();
		for (int i=0; i<node.jjtGetNumChildren(); i++)
			generator.defineOperatorParameter(getChild(node, i).tokenValue);
		generator.endParameterDefinitions();
		return data;
	}
	
	// Function body
	public Object visit(ASTFnBody node, Object data) {
		return compileChildren(node, data);
	}
	
	// Function return expression
	public Object visit(ASTReturnExpression node, Object data) {
		compileChildren(node, data);
		generator.compileReturnValue();
		return null;
	}
	
	// Function invocation argument list.  Returns number of arguments.
	public Object visit(ASTArgList node, Object data) {
		// Compile arguments
		for (int i=0; i < node.jjtGetNumChildren(); i++)
			compileChild(node, i, data);
		// Return the number of arguments
		return new Integer(node.jjtGetNumChildren());
	}
	
	// Function call
	public Object visit(ASTCall node, Object data) {
		// Child 0 - identifier (fn name)
		String fnname = getChild(node, 0).tokenValue;
		// Child 1 - arglist
		compileChild(node, 1, data);
		// Compile invocation
		generator.compileCall(fnname);
		return data;
	}
	
	// Function invocation in an expression
	public Object visit(ASTFnInvoke node, Object data) {
		// Child 0 - identifier (fn name)
		String fnname = getChild(node, 0).tokenValue;
		// Child 1 - arglist
		compileChild(node, 1, data);
		// Compile invocation
		generator.compileEvaluate(fnname);
		return data;
	}

	// Compile an IF 
	public Object visit(ASTIfStatement node, Object data) {
		// compile boolean expression
		compileChild(node, 0, data);
		Generator.IfStatement ifStatement = generator.new IfStatement();
		// compile the if(true) block
		compileChild(node, 1, data);
		// if the IF statement has an ELSE block...
		if (node.ifHasElse) {
			ifStatement.beginElse();
			// compile the ELSE block
			compileChild(node, 2, data);
		}
		// end if
		ifStatement.endIf();
		return data;
	}
	
	// Compile a FOR loop
	public Object visit(ASTForLoop node, Object data) {
		// compile loop initialisation
		compileChild(node, 0, data);
		Generator.DoLoop forLoop = generator.new DoLoop();
		// compile evaluation of loop test
		compileChild(node, 1, data);
		forLoop.testDo();
		// compile loop body
		compileChild(node, 3, data);
		// compile loop increment
		compileChild(node, 2, data);
		forLoop.endDo();
		return data;
	}
	
	// Process an identifier
	// This doesn't do anything, but needs to be here because we need an ASTIdentifier node.
	public Object visit(ASTIdentifier node, Object data) {
		return data;
	}
	
	// Compile the WRITE statement
	public Object visit(ASTWrite node, Object data) {
		compileChildren(node, data);
		generator.compileWrite();
		return data;
	}
	
	// Compile a dereference of a variable or parameter
	public Object visit(ASTDereference node, Object data) {
		String refname = node.tokenValue;
		generator.compileGet(refname);
		return data;
	}
	
	// Compile an assignment statement, by popping a value off the stack and assigning it
	// to a variable or parameter.
	public Object visit(ASTAssignment node, Object data) {
		compileChildren(node, data);
		String refname = getChild(node, 0).tokenValue;
		generator.compileSet(refname);
		return data;
	}

	// OR
	public Object visit(ASTOr node, Object data) {
		compileChildren(node, data);	
		generator.compileOr();
		return data;
	}

	// AND
	public Object visit(ASTAnd node, Object data) {
		compileChildren(node, data);	
		generator.compileAnd();
		return data;		
	}

	// ==
	public Object visit(ASTCompEqual node, Object data) {
		compileChildren(node, data);	
		generator.compileEQ();
		return data;
	}

	// !=
	public Object visit(ASTCompNequal node, Object data) {
		compileChildren(node, data);
		generator.compileNEQ();
		return data;
	}

	// >=
	public Object visit(ASTCompGTE node, Object data) {
		compileChildren(node, data);	
		generator.compileGTE();
		return data;
	}

	// <=
	public Object visit(ASTCompLTE node, Object data) {
		compileChildren(node, data);	
		generator.compileLTE();
		return data;
	}

	// >
	public Object visit(ASTCompGT node, Object data) {
		compileChildren(node, data);	
		generator.compileGT();
		return data;
	}

	// <
	public Object visit(ASTCompLT node, Object data) {
		compileChildren(node, data);	
		generator.compileLT();
		return data;
	}

	// +
	public Object visit(ASTAdd node, Object data) {
		compileChildren(node, data);	
		generator.compilePlus();
		return data;
	}

	// -
	public Object visit(ASTSubtract node, Object data) {
		compileChildren(node, data);	
		generator.compileMinus();
		return data;
	}

	// *
	public Object visit(ASTTimes node, Object data) {
		compileChildren(node, data);	
		generator.compileMultiply();
		return data;
	}

	// /
	public Object visit(ASTDivide node, Object data) {
		compileChildren(node, data);	
		generator.compileDivide();
		return data;
	}

	// NOT
	public Object visit(ASTUnaryNot node, Object data) {
		compileChildren(node, data);	
		generator.compileUnaryNot();
		return data;
	}

	// + (unary)
	public Object visit(ASTUnaryPlus node, Object data) {
		compileChildren(node, data);	
		generator.compileUnaryPlus();
		return data;
	}

	// - (unary)
	public Object visit(ASTUnaryMinus node, Object data) {
		compileChildren(node, data);	
		generator.compileUnaryMinus();
		return data;
	}

	// Push integer literal to stack
	public Object visit(ASTInteger node, Object data) {
		compileChildren(node, data);
		generator.compilePush(Integer.parseInt(node.tokenValue));
		return data;
	}

	// Push floating point literal to stack
	public Object visit(ASTRational node, Object data) {
		compileChildren(node, data);
		// TODO - Not implemented yet.
		return data;
	}

	// Push true literal to stack
	public Object visit(ASTTrue node, Object data) {
		compileChildren(node, data);
		generator.compilePush(true);
		return data;
	}

	// Push false literal to stack
	public Object visit(ASTFalse node, Object data) {
		compileChildren(node, data);
		generator.compilePush(false);
		return data;
	}

}
