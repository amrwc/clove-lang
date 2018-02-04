package uk.ac.derby.ldi.sili.vm;

import uk.ac.derby.ldi.sili.exceptions.ExceptionFatal;
import uk.ac.derby.ldi.sili.values.*;
import uk.ac.derby.ldi.sili.vm.instructions.OpNop;

/**
 * Run-time context for operator execution. Allows access to relevant parameters
 * and variables.
 */
public class Context {

	// Operand stack size
	private final static int operandstacksize = 128;

	private Instruction[] code; // current set of instructions
	private int instructionPointer; // instruction pointer

	private Context[] contextDisplay; // this context's view of static scope

	private Cell[] variables; // variables
	private Value[] arguments; // arguments

	private Value[] operandStack; // operand stack
	private int stackPointer; // operand stack pointer

	private Context caller; // context that spawned this one

	private VirtualMachine vm; // VM in which this context lives

	private int depth; // static scope depth

	/** Create a non-executable root (depth=0) context. */
	public Context(VirtualMachine vm) {
		this.vm = vm;
		caller = null;
		depth = 0;
		// Set up scope display
		contextDisplay = new Context[depth + 1];
		contextDisplay[depth] = this;
		// Allocate an operand stack.
		operandStack = new Value[operandstacksize];
		// Point to the executable code.
		code = null;
		// Initialise the instruction and stack pointers.
		instructionPointer = 0;
		stackPointer = 0;
	}

	/** Create a context for an operator invocation. */
	Context(Context caller, Operator operator) {
		this.vm = caller.vm;
		this.caller = caller;
		depth = operator.getDepth();
		// Set up scope display
		contextDisplay = new Context[Math.max(depth + 1, caller.contextDisplay.length)];
		System.out.println("Context: copy display from " + caller + " to " + this);
		System.arraycopy(caller.contextDisplay, 0, contextDisplay, 0, caller.contextDisplay.length);
		// Add this context to the scope display
		contextDisplay[depth] = this;
		// Allocate space for variables.
		if (operator.getVariableCount() > 0)
			variables = new Cell[operator.getVariableCount()];
		// Adjust the caller context's stack pointer to remove the arguments from its stack
		// and move them to this context. This ensures continuations will
		// work, because we no longer need to refer to the caller's operand stack.
		int parmCount = operator.getParameterCount();
		if (parmCount > 0) {
			arguments = new Value[parmCount];
			caller.stackPointer -= parmCount;
			System.out.println("Context: copy stack from " + caller + " to " + this);
			System.arraycopy(caller.operandStack, caller.stackPointer, arguments, 0, parmCount);
		}
		// Allocate an operand stack.
		operandStack = new Value[operandstacksize];
		// Point to the executable code.
		code = operator.obtainCode();
		// Initialise the instruction and stack pointers.
		instructionPointer = 0;
		stackPointer = 0;
	}

	private void dumpstack() {
		if (variables != null) {
			System.out.println("Variables:");
			for (int i = 0; i < variables.length; i++) {
				System.out.print("V[" + i + "] = ");
				if (variables[i] == null)
					System.out.println("uninitialised");
				else
					System.out.println(variables[i]);
			}
		}
		System.out.println("Stack:");
		for (int i = 0; i < stackPointer; i++) {
			System.out.print("S[" + i + "] = ");
			if (operandStack[i] == null)
				System.out.println("uninitialised");
			else
				System.out.println(operandStack[i]);
		}
	}

	/** Dump this context. */
	public void dump(String prompt) {
		System.out.println("----------" + prompt + "----------");
		System.out.println("Arguments:");
		for (int i = 0; i <= depth; i++) {
			int offset = 0;
			if (contextDisplay != null && contextDisplay[i] != null && contextDisplay[i].arguments != null) {
				for (Value v : contextDisplay[i].arguments)
					System.out.println("[" + i + " " + (offset++) + "] " + v);
			} else
				System.out.println("[" + i + " 0] none");
		}
		dumpstack();
		System.out.println("Context ID: " + this);
		System.out.print("Depth: " + depth);
		(new Dumper()).dumpMachineCode(code);
		System.out.println();
		System.out.println("--------------------");
	}

	/** Get the currently-executing instruction. */
	public final Instruction getCurrentInstruction() {
		if (code == null)
			return new OpNop();
		return code[instructionPointer - 1];
	}

	/** Get the virtual machine upon which this Context is running. */
	public final VirtualMachine getVirtualMachine() {
		return vm;
	}

	private final void execute() {
		vm.setCurrentContext(this);
		while (instructionPointer < code.length) {
			Instruction i = code[instructionPointer++];
			// System.out.println("About to execute " + i.toString());
			i.execute(this);
		}
		vm.setCurrentContext(caller);
	}

	// Invoke user-defined operator in its own context, i.e., call it.
	public final void call(Operator operator) {
		(new Context(this, operator)).execute();
	}

	public final void doReturn() {
		instructionPointer = code.length;
	}

	public final void doReturnValue() {
		caller.push(pop());
		doReturn();
	}

	/** Go to a given instruction. */
	public final void jump(int newIP) {
		instructionPointer = newIP;
	}

	/** Return Value on top of the stack */
	private final Value peek() {
		return operandStack[stackPointer - 1];
	}

	final Context getCaller() {
		return caller;
	}

	final int getStackCount() {
		return stackPointer;
	}

	/** Push a Value onto the operand stack. */
	public final void push(Value v) {
		operandStack[stackPointer++] = v;
	}

	/** Pop a Value from the operand stack. */
	public final Value pop() {
		return operandStack[--stackPointer];
	}

	/**
	 * Operator to set a parameter's argument.
	 * 
	 * RT: POP - value
	 */
	public final void parmSet(int depth, int offset) {
		contextDisplay[depth].arguments[offset] = pop();
	}

	/**
	 * Operator to get a parameter's argument. RT: PUSH - value
	 */
	public final void parmGet(int depth, int offset) {
		push(contextDisplay[depth].arguments[offset]);
	}

	/**
	 * Operator to set value of a local variable. RT: POP - value
	 */
	public final void varSet(int depth, int offset) {
		Value v = pop();
		Cell cell = contextDisplay[depth].variables[offset];
		if (cell == null)
			(new ExceptionFatal("Context: varSet: cell in " + this + " at depth " + depth + " and offset " + offset + " is null")).printStackTrace();
		cell.setValue(v);
	}

	/**
	 * Operator to get value of a local variable. RT: PUSH - value
	 */
	public final void varGet(int depth, int offset) {
		push(contextDisplay[depth].variables[offset].getValue());
	}

	/**
	 * Operator to define a Cell. RT:
	 * 
	 */
	public final void varSetCell(int depth, int offset, Cell cell) {
		System.out.println("Context: varSetCell: cell in " + this + " at depth " + depth + " and offset " + offset + " is " + cell);
		contextDisplay[depth].variables[offset] = cell;
	}

	/**
	 * Operator to obtain the Cell at a given slot. RT:
	 * 
	 */
	public final Cell varGetCell(int depth, int offset) {
		return contextDisplay[depth].variables[offset];
	}

	/**
	 * Conditional Jump operator.
	 * 
	 * POP - ValueBoolean
	 * 
	 */
	public final void branchIfTrue(int jumpTo) {
		if (pop().booleanValue())
			jump(jumpTo);
	}

	/**
	 * Conditional Jump operator.
	 * 
	 * POP - ValueBoolean
	 * 
	 */
	public final void branchIfFalse(int jumpTo) {
		if (!pop().booleanValue())
			jump(jumpTo);
	}

	// Push literal
	// PUSH - Value
	public final void pushLiteral(Value literal) {
		push(literal);
	}

	// Push true literal to stack
	// PUSH - ValueBoolean - true
	public final void pushTrue() {
		push(ValueBoolean.getTrue());
	}

	// Push false literal to stack
	// PUSH - ValueBoolean - false
	public final void pushFalse() {
		push(ValueBoolean.getFalse());
	}

	// Duplicate value on top of stack
	public final void duplicate() {
		push(peek());
	}

	// Duplicate value under topmost on stack. Topmost remains unchanged.
	public final void duplicateUnder() {
		Value v = pop();
		push(peek());
		push(v);
	}

	// Swap values on top of stack
	public final void swap() {
		Value v1 = pop();
		Value v2 = pop();
		push(v1);
		push(v2);
	}

	// MAX - return larger of two values
	// POP - Value
	// POP - Value
	// PUSH - Value
	public final void max() {
		Value p1 = pop();
		Value p2 = pop();
		if (p1.gt(p2).booleanValue())
			push(p1);
		else
			push(p2);
	}

	// MIN - return smaller of two values
	// POP - Value
	// POP - Value
	// PUSH - Value
	public final void min() {
		Value p1 = pop();
		Value p2 = pop();
		if (p1.lt(p2).booleanValue())
			push(p1);
		else
			push(p2);
	}

	// Logical XOR
	// POP - Value
	// POP - Value
	// PUSH - Value
	public final void xor() {
		push(pop().xor(pop()));
	}

	// Logical OR
	// POP - Value
	// POP - Value
	// PUSH - Value
	public final void or() {
		push(pop().or(pop()));
	}

	// Logical AND
	// POP - Value
	// POP - Value
	// PUSH - Value
	public final void and() {
		push(pop().and(pop()));
	}

	// =
	// POP - Value
	// POP - Value
	// PUSH - ValueBoolean
	public final void eq() {
		push(pop().eq(pop()));
	}

	// !=
	// POP - Value
	// POP - Value
	// PUSH - ValueBoolean
	public final void neq() {
		push(pop().neq(pop()));
	}

	// >=
	// POP - Value
	// POP - Value
	// PUSH - ValueBoolean
	public final void gte() {
		Value v2 = pop();
		push(pop().gte(v2));
	}

	// <=
	// POP - Value
	// POP - Value
	// PUSH - ValueBoolean
	public final void lte() {
		Value v2 = pop();
		push(pop().lte(v2));
	}

	// >
	// POP - Value
	// POP - Value
	// PUSH - ValueBoolean
	public final void gt() {
		Value v2 = pop();
		push(pop().gt(v2));
	}

	// <
	// POP - Value
	// POP - Value
	// PUSH - ValueBoolean
	public final void lt() {
		Value v2 = pop();
		push(pop().lt(v2));
	}

	// +
	// POP - Value
	// POP - Value
	// PUSH - Value
	public final void add() {
		push(pop().add(pop()));
	}

	// -
	// POP - Value
	// POP - Value
	// PUSH - Value
	public final void subtract() {
		Value v2 = pop();
		push(pop().subtract(v2));
	}

	// *
	// POP - Value
	// POP - Value
	// PUSH - Value
	public final void mult() {
		push(pop().mult(pop()));
	}

	// /
	// POP - Value
	// POP - Value
	// PUSH - Value
	public final void div() {
		Value v2 = pop();
		push(pop().div(v2));
	}

	// Logical NOT
	// POP - Value
	// PUSH - Value
	public final void not() {
		push(pop().not());
	}

	// Unary +
	// POP - Value
	// PUSH - Value
	public final void unary_plus() {
		push(pop().unary_plus());
	}

	// Unary -
	// POP - Value
	// PUSH - Value
	public final void unary_minus() {
		push(pop().unary_minus());
	}
}