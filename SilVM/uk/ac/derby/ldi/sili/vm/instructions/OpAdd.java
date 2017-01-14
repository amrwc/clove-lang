package uk.ac.derby.ldi.sili.vm.instructions;

import uk.ac.derby.ldi.sili.vm.Context;
import uk.ac.derby.ldi.sili.vm.Instruction;

public final class OpAdd extends Instruction {
	public final void execute(Context context) {
		context.add();
	}
}
