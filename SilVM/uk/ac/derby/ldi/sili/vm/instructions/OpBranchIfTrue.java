package uk.ac.derby.ldi.sili.vm.instructions;

import uk.ac.derby.ldi.sili.vm.Context;
import uk.ac.derby.ldi.sili.vm.Instruction;

public class OpBranchIfTrue extends Instruction {
	private int address;

	/* For serialization support */
	
	public OpBranchIfTrue() {
		this.address = -1;
	}
	
	public void setAddress(int address) {
		this.address = address;
	}
	
	public int getAddress() {
		return address;
	}
	
	/* End of serialization support */

	public OpBranchIfTrue(int address) {
		this.address = address;
	}
	
	public final void execute(Context context) {
		context.branchIfTrue(address);
	}
		
	public String toString() {
		return getName() + " " + address;
	}

}
