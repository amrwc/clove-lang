package uk.ac.derby.ldi.sili.vm.instructions;

import uk.ac.derby.ldi.sili.vm.Context;
import uk.ac.derby.ldi.sili.vm.Instruction;

public class OpBranchIfFalse extends Instruction {
	private int address;
	
	public OpBranchIfFalse(int address) {
		this.address = address;
	}
	
	public final void execute(Context context) {
		context.branchIfFalse(address);
	}
	
	public String toString() {
		return getName() + " " + address;
	}

}
