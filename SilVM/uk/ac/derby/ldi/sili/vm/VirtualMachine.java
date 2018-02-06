package uk.ac.derby.ldi.sili.vm;


import java.io.PrintStream;

import uk.ac.derby.ldi.sili.values.*;

/**
 * The Rel virtual machine, slightly stripped down to become the Sili virtual machine.
 * 
 * @author dave
 */

public class VirtualMachine {
	
	// root execution Context.
	private Context rootContext;
	
	// Output stream
	private PrintStream printStream;
	
	/** Create a virtual machine. */
	public VirtualMachine(PrintStream printStream) {
		this.printStream = printStream;
		resetVM();
	}
	
	/** Get the output stream assigned to this VM. */
	public PrintStream getPrintStream() {
		return printStream;
	}
	
	/** Reset the VM. */
	public void resetVM() {
		rootContext = new Context(this);
	}

	/** Execute the given ValueOperator in the root Context. */
	public final void execute(Operator op) {
		rootContext.call(op);
	}
	
	/** Pop a value from the root Context. */
	public final Value pop() {
		return rootContext.pop();
	}

	/** Get the number of items on the stack in the root Context. */
	public final int getStackCount() {
		return rootContext.getStackCount();
	};

}
