package interpreter;

import parser.ast.*;

public class ParserDebugger implements CloveVisitor {
	private int indent = 0;

	private String indentString() {
		final StringBuffer sb = new StringBuffer();
		for (int i = 0; i < indent; ++i) {
			sb.append(" ");
		}
		return sb.toString();
	}

	/** Debugging dump of a node. */
	private Object dump(SimpleNode node, Object data) {
		System.out.println(indentString() + node);
		++indent;
		data = node.childrenAccept(this, data);
		--indent;
		return data;		
	}

	@Override
	public Object visit(SimpleNode node, Object data) {
		System.out.println(node + ": acceptor not implemented in subclass?");
		return data;
	}

	// Execute a Clove program
	@Override
	public Object visit(ASTCode node, Object data) {
		dump(node, data);
		return data;
	}



	/***********************************************
	 *                 Statements                  *
	 ***********************************************/

	// Execute a statement
	@Override
	public Object visit(ASTStatement node, Object data) {
		dump(node, data);
		return data;
	}

	// Function call
	@Override
	public Object visit(ASTCall node, Object data) {
		dump(node, data);
		return data;
	}

	@Override
	public Object visit(ASTDefinition node, Object data) {
		dump(node, data);
		return null;
	}

	@Override
	public Object visit(ASTDeclaration node, Object data) {
		dump(node, data);
		return null;
	}

	// Execute an assignment statement, by popping a value off the stack and assigning it
	// to a variable.
	@Override
	public Object visit(ASTAssignment node, Object data) {
		dump(node, data);
		return data;
	}

	// Function definition
	@Override
	public Object visit(ASTFunctionDefinition node, Object data) {
		dump(node, data);
		return data;
	}

	// Execute a block
	@Override
	public Object visit(ASTBlock node, Object data) {
		dump(node, data);
		return data;	
	}

	// Execute an IF 
	@Override
	public Object visit(ASTIfStatement node, Object data) {
		dump(node, data);
		return data;
	}

	// Execute a FOR loop
	@Override
	public Object visit(ASTForLoop node, Object data) {
		dump(node, data);
		return data;
	}

	@Override
	public Object visit(ASTWhileLoop node, Object data) {
		dump(node, data);
		return data;
	}

	// Execute the WRITE statement
	@Override
	public Object visit(ASTWrite node, Object data) {
		dump(node, data);
		return data;
	}

	@Override
	public Object visit(ASTQuit node, Object data) {
		dump(node, data);
		return data;
	}

	@Override
	public Object visit(ASTProtoInvoke node, Object data) {
		dump(node, data);
		return data;
	}

	@Override
	public Object visit(ASTIncrementDecrement node, Object data) {
		dump(node, data);
		return null;
	}

	@Override
	public Object visit(ASTHttp node, Object data) {
		dump(node, data);
		return null;
	}

	@Override
	public Object visit(ASTFile node, Object data) {
		dump(node, data);
		return null;
	}



	/***********************************************
	 *               Sub-statements                *
	 ***********************************************/

	// Process an identifier
	// This doesn't do anything, but needs to be here because we need an ASTIdentifier node.
	@Override
	public Object visit(ASTIdentifier node, Object data) {
		dump(node, data);
		return data;
	}

	@Override
	public Object visit(ASTConstInit node, Object data) {
		dump(node, data);
		return null;
	}

	@Override
	public Object visit(ASTArrayInit node, Object data) {
		dump(node, data);
		return null;
	}

	// Function argument list
	@Override
	public Object visit(ASTArgumentList node, Object data) {
		dump(node, data);
		return data;
	}

	// Function definition parameter list
	@Override
	public Object visit(ASTParameterList node, Object data) {
		dump(node, data);
		return data;
	}

	// Function body
	@Override
	public Object visit(ASTFunctionBody node, Object data) {
		dump(node, data);
		return data;
	}

	// Function return expression
	@Override
	public Object visit(ASTReturnExpression node, Object data) {
		dump(node, data);
		return data;
	}



	/***********************************************
	 *                Expressions                  *
	 ***********************************************/

	// OR
	@Override
	public Object visit(ASTOr node, Object data) {
		dump(node, data);
		return data;
	}

	// AND
	@Override
	public Object visit(ASTAnd node, Object data) {
		dump(node, data);
		return data;		
	}

	// ==
	@Override
	public Object visit(ASTCompEqual node, Object data) {
		dump(node, data);
		return data;
	}

	// !=
	@Override
	public Object visit(ASTCompNequal node, Object data) {
		dump(node, data);
		return data;
	}

	// >=
	@Override
	public Object visit(ASTCompGTE node, Object data) {
		dump(node, data);
		return data;
	}

	// <=
	@Override
	public Object visit(ASTCompLTE node, Object data) {
		dump(node, data);
		return data;
	}

	// >
	@Override
	public Object visit(ASTCompGT node, Object data) {
		dump(node, data);
		return data;
	}

	// <
	@Override
	public Object visit(ASTCompLT node, Object data) {
		dump(node, data);
		return data;
	}

	// +
	@Override
	public Object visit(ASTAdd node, Object data) {
		dump(node, data);
		return data;
	}

	// -
	@Override
	public Object visit(ASTSubtract node, Object data) {
		dump(node, data);
		return data;
	}

	// *
	@Override
	public Object visit(ASTTimes node, Object data) {
		dump(node, data);
		return data;
	}

	// /
	@Override
	public Object visit(ASTDivide node, Object data) {
		dump(node, data);
		return data;
	}

	// %
	@Override
	public Object visit(ASTModulo node, Object data) {
		dump(node, data);
		return data;
	}

	// NOT
	@Override
	public Object visit(ASTUnaryNot node, Object data) {
		dump(node, data);
		return data;
	}

	// + (unary)
	@Override
	public Object visit(ASTUnaryPlus node, Object data) {
		dump(node, data);
		return data;
	}

	// - (unary)
	@Override
	public Object visit(ASTUnaryMinus node, Object data) {
		dump(node, data);
		return data;
	}

	// Function invocation in an expression
	@Override
	public Object visit(ASTFunctionInvocation node, Object data) {
		dump(node, data);
		return data;
	}

	// Dereference a variable, and push its value onto the stack
	@Override
	public Object visit(ASTDereference node, Object data) {
		dump(node, data);
		return data;
	}

	@Override
	public Object visit(ASTGetArgs node, Object data) {
		dump(node, data);
		return null;
	}

	@Override
	public Object visit(ASTRandom node, Object data) {
		dump(node, data);
		return null;
	}



	/***********************************************
	 *                 Literals                    *
	 ***********************************************/

	@Override
	public Object visit(ASTInteger node, Object data) {
		dump(node, data);
		return data;
	}

	@Override
	public Object visit(ASTCharacter node, Object data) {
		dump(node, data);
		return data;
	}

	@Override
	public Object visit(ASTRational node, Object data) {
		dump(node, data);
		return data;
	}

	@Override
	public Object visit(ASTTrue node, Object data) {
		dump(node, data);
		return data;
	}

	@Override
	public Object visit(ASTFalse node, Object data) {
		dump(node, data);
		return data;
	}

	@Override
	public Object visit(ASTValueFunction node, Object data) {
		dump(node, data);
		return data;
	}

	@Override
	public Object visit(ASTValueObject node, Object data) {
		dump(node, data);
		return data;
	}

	@Override
	public Object visit(ASTValueList node, Object data) {
		dump(node, data);
		return data;
	}
}
