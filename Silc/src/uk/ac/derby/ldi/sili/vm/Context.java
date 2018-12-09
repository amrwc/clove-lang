package uk.ac.derby.ldi.sili.vm;

import uk.ac.derby.ldi.sili.values.*;

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

	private Value[] variables; // variables
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

	/**
	 * Create a context for an operator invocation, where the variables and parms
	 * referenced are in a specified context
	 */
	private Context(Context caller, Operator operator, Context varparms) {
		this.vm = caller.vm;
		this.caller = caller;
		depth = operator.getDepth();
		// Set up scope display
		contextDisplay = new Context[Math.max(depth + 1, varparms.contextDisplay.length)];
		System.arraycopy(varparms.contextDisplay, 0, contextDisplay, 0, varparms.contextDisplay.length);
		// Add this context to the scope display
		contextDisplay[depth] = this;
		// Allocate space for variables.
		if (operator.getVariableCount() > 0)
			variables = new Value[operator.getVariableCount()];
		// Adjust the caller context's stack pointer to remove the arguments from its stack
		// and move them to this context. This ensures continuations will
		// work, because we no longer need to refer to the caller's operand stack.
		int parmCount = operator.getParameterCount();
		if (parmCount > 0) {
			arguments = new Value[parmCount];
			caller.stackPointer -= parmCount;
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

	/** Create a context for an operator invocation. */
	Context(Context caller, Operator operator) {
		this(caller, operator, caller);
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

	/** Get the virtual machine upon which this Context is running. */
	public final VirtualMachine getVirtualMachine() {
		return vm;
	}

	private final void execute() {
		while (instructionPointer < code.length)
			code[instructionPointer++].execute(this);
	}

	// Invoke user-defined operator in its own context, i.e., call it.
	public final void call(Operator operator) {
		(new Context(this, operator)).execute();
	}

	// Invoke user-defined operator in its own context, using a specified parent
	// context for variable/parm scope.
	public final void call(Operator operator, Context varparmContext) {
		(new Context(this, operator, varparmContext)).execute();
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
	public final Value peek() {
		return operandStack[stackPointer - 1];
	}

	/** Return n values on top of the stack */
	public Value[] peek(int n) {
		Value[] values = new Value[n];
		System.arraycopy(operandStack, stackPointer - n, values, 0, n);
		return values;
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
		contextDisplay[depth].variables[offset] = pop();
	}

	/**
	 * Operator to get value of a local variable. RT: PUSH - value
	 */
	public final void varGet(int depth, int offset) {
		push(contextDisplay[depth].variables[offset]);
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

}