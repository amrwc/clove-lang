package uk.ac.derby.ldi.sili.vm.instructions;

import uk.ac.derby.ldi.sili.vm.Context;
import uk.ac.derby.ldi.sili.vm.Instruction;

public class OpJump extends Instruction {
	private final static long serialVersionUID = 0;

	private int address;
	
	/* For serialization support */
	
	public OpJump() {
		this.address = -1;
	}
	
	public void setAddress(int address) {
		this.address = address;
	}
	
	public int getAddress() {
		return address;
	}
	
	/* End of serialization support */
	
	public OpJump(int address) {
		this.address = address;
	}
	
	public final void execute(Context context) {
		context.jump(address);
	}
		
	public String toString() {
		return getName() + " " + address;
	}

}
