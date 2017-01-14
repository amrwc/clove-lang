package uk.ac.derby.ldi.sili.vm.instructions;

import uk.ac.derby.ldi.sili.vm.Context;
import uk.ac.derby.ldi.sili.vm.Instruction;

public class OpParameterGet extends Instruction {
	private int depth;
	private int offset;
	
	public OpParameterGet(int depth, int offset) {
		this.depth = depth;
		this.offset = offset;
	}
	
	public final void execute(Context context) {
		context.parmGet(depth, offset);
	}
	
	public String toString() {
		return getName() + " " + depth + " " + offset;
	}
}
