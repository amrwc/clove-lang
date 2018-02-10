package uk.ac.derby.ldi.silc.compiler;

import uk.ac.derby.ldi.sili.exceptions.*;
import uk.ac.derby.ldi.sili.values.*;
import uk.ac.derby.ldi.sili.vm.*;
import uk.ac.derby.ldi.sili.vm.instructions.*;

/** Code generator.  This mostly delegates to currentOperatorDefinition. */
public class Generator {

	// Reference to current operator definition.
	private OperatorDefinition currentOperatorDefinition = null;
	
	public void beginCompilation() {
		// Begin _main operator definition
		defineOperator("_main");
	}
	
	public Operator endCompilation() {
		// Capture reference to main operator definition
		OperatorDefinition mainOperatorDefinition = currentOperatorDefinition;
		// End main operator definition
		endOperator();
		// Get the executable _main operator
		return mainOperatorDefinition.getOperator();
	}
	
	public void compileInstruction(Instruction instruction) {
		currentOperatorDefinition.compile(instruction);
	}
	
	public void compileInstructionAt(Instruction instruction, int address) {
		currentOperatorDefinition.compileAt(instruction, address);
	}
	
	private void defineOperator(String fnname) {
		currentOperatorDefinition = new OperatorDefinition(fnname, currentOperatorDefinition);
	}

	private static long anonymousNameSerial = 0;
	
	public OperatorDefinition beginAnonymousOperator() {
		defineOperator("%_anon" + (anonymousNameSerial++));
		return currentOperatorDefinition;
	}
	
	public void beginOperator(String fnname) {
		defineOperator(fnname);
	}
	
	public String getCurrentDefinitionSignature() {
		return currentOperatorDefinition.getSignature();
	}
	
	public void compileReturnValue() {
		compileInstruction(new OpReturnValue());
		currentOperatorDefinition.setDefinedReturnValue(true);
	}
	
	public void compileReturn() {
		compileInstruction(new OpReturn());
	}
	
	public void beginParameterDefinitions() {
		// This is a no-op, but here in case needed in the future.
	}
	
	public void defineOperatorParameter(String name) {
		currentOperatorDefinition.defineParameter(name);		
	}
	
	public void endParameterDefinitions() {
		currentOperatorDefinition.getParentOperatorDefinition().defineOperator(currentOperatorDefinition);		
	}
	
	public void endOperator() {
		if (currentOperatorDefinition.hasDefinedReturnValue()) {
			compilePush(new ValueInteger(0));
			compileInstruction(new OpReturnValue());
		} else {
			compileInstruction(new OpReturn());
			if (currentOperatorDefinition.isUsedInExpression())
				throw new ExceptionSemantic(currentOperatorDefinition.getSignature() + " was used in an expression but never returns a value.");
		}
		// Done.  Restore previous definition context.
		currentOperatorDefinition = currentOperatorDefinition.getParentOperatorDefinition();
	}
	
	private OperatorDefinition findOperator(String signature) {
		OperatorDefinition fn = currentOperatorDefinition.getOperator(signature);
		if (fn == null)
			throw new ExceptionSemantic("Operator " + signature + " does not exist.");
		return fn;
	}

	public void compileCall(OperatorDefinition operator) {
		operator.compileCall(this);
	}
	
	public void compileCall(String signature) {
		compileCall(findOperator(signature));
	}

	public void compileEvaluate(OperatorDefinition operator) {
		operator.compileEvaluate(this);
	}
	
	public void compileEvaluate(String signature) {
		compileEvaluate(findOperator(signature));
	}
	
	public class IfStatement {
		private int resolveThisForwardBranch;
		private boolean conditional;
		
		public IfStatement() {
			// if the expression returns false, branch past the if(true) block
			resolveThisForwardBranch = currentOperatorDefinition.getCP();
			conditional = true;
			compileInstruction(new OpBranchIfFalse(0));		// needs to be resolved			
		}

		public void beginElse() {
			// if we executed the if(true) block, we need to jump past the else block
			int pendingForwardBranch = resolveThisForwardBranch;
			resolveThisForwardBranch = currentOperatorDefinition.getCP();
			conditional = false;
			compileInstruction(new OpJump(0));			// needs to be resolved
			// resolve the pending forward branch from the expression test
			// if the test returns false, we need to branch to this point
			compileInstructionAt(new OpBranchIfFalse(currentOperatorDefinition.getCP()), pendingForwardBranch);
		}
		
		public void endIf() {
			// resolve the pending forward branch
			if (conditional)
				compileInstructionAt(new OpBranchIfFalse(currentOperatorDefinition.getCP()), resolveThisForwardBranch);
			else
				compileInstructionAt(new OpJump(currentOperatorDefinition.getCP()), resolveThisForwardBranch);
		}
	}		
	
	public class DoLoop {
		private int head;
		private int resolveThisForwardBranch;

		public DoLoop() {
			head = currentOperatorDefinition.getCP();
		}
		
		public void testDo() {
			// compile branch out of loop if loop test returns false
			resolveThisForwardBranch = currentOperatorDefinition.getCP();
			compileInstruction(new OpBranchIfFalse(0));
		}
		
		public void endDo() {
			// compile jump back to top of loop
			compileInstruction(new OpJump(head));
			// resolve unresolved branch out of loop
			compileInstructionAt(new OpBranchIfFalse(currentOperatorDefinition.getCP()), resolveThisForwardBranch);
		}
	}
	
	public Slot findReference(String refname) {
		Slot local = currentOperatorDefinition.getReference(refname);
		if (local == null) {
			// In a highly questionable move, any missing reference generates
			// a local variable.
			currentOperatorDefinition.defineVariable(refname);
			local = currentOperatorDefinition.getReference(refname);
		}
		return local;
	}
	
	public void compileGet(Slot local) {
		local.compileGet(this);
	}
	
	public void compileGet(String refname) {
		compileGet(findReference(refname));
	}
	
	public void compileSet(Slot local) {
		local.compileSet(this);
	}
	
	public void compileSet(String refname) {
		compileSet(findReference(refname));
	}
	
	// OR
	public void compileOr() {
		compileInstruction(new OpOr());
	}

	// AND
	public void compileAnd() {
		compileInstruction(new OpAnd());
	}

	// ==
	public void compileEQ() {
		compileInstruction(new OpEq());
	}

	// !=
	public void compileNEQ() {
		compileInstruction(new OpNeq());
	}

	// >=
	public void compileGTE() {
		compileInstruction(new OpGte());
	}

	// <=
	public void compileLTE() {
		compileInstruction(new OpLte());
	}

	// >
	public void compileGT() {
		compileInstruction(new OpGt());
	}

	// <
	public void compileLT() {
		compileInstruction(new OpLt());
	}
	
	// +
	public void compilePlus() {
		compileInstruction(new OpAdd());
	}

	// -
	public void compileMinus() {
		compileInstruction(new OpSubtract());
	}

	// *
	public void compileMultiply() {
		compileInstruction(new OpMultiply());
	}

	// /
	public void compileDivide() {
		compileInstruction(new OpDivide());
	}

	// NOT
	public void compileUnaryNot() {
		compileInstruction(new OpNot());
	}

	// + (unary)
	public void compileUnaryPlus() {
		compileInstruction(new OpUnaryPlus());
	}

	// - (unary)
	public void compileUnaryMinus() {
		compileInstruction(new OpUnaryMinus());
	}

	// Pop value from stack
	public void compilePop() {
		compileInstruction(new OpPop());
	}
	
	// Duplicate topmost value on stack
	public void compileDuplicate() {
		compileInstruction(new OpDuplicate());
	}
	
	// Duplicate value under topmost on stack.  Topmost is unchanged.
	public void compileDuplicateUnder() {
		compileInstruction(new OpDuplicateUnder());
	}
	
	// Swap top two values on stack
	public void compileSwap() {
		compileInstruction(new OpSwap());
	}
	
	// Push literal Value to stack
	public void compilePush(Value v) {
		compileInstruction(new OpPushLiteral(v));
	}
	
	// Push integer literal to stack
	public void compilePush(long value) {
		compilePush(new ValueInteger(value));		
	}

	// Push boolean literal to stack
	public void compilePush(boolean flag) {
		compilePush(ValueBoolean.getBoolean(flag));
	}
	
	// Compile: write value on top of stack to console.  For debugging purposes.
	public void compileWrite() {
		compileInstruction(new OpWrite());
	}
}
