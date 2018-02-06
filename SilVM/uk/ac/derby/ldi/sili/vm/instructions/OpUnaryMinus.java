package uk.ac.derby.ldi.sili.vm.instructions;

import uk.ac.derby.ldi.sili.vm.Context;
import uk.ac.derby.ldi.sili.vm.Instruction;

public class OpUnaryMinus extends Instruction {
	private final static long serialVersionUID = 0;
	public final void execute(Context context) {
		context.push(context.pop().unary_minus());
	}
}
