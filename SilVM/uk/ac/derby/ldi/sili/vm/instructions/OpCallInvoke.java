package uk.ac.derby.ldi.sili.vm.instructions;

import uk.ac.derby.ldi.sili.vm.Context;
import uk.ac.derby.ldi.sili.vm.Instruction;
import uk.ac.derby.ldi.sili.vm.Operator;

public class OpCallInvoke extends Instruction {
	private Operator operator;
	
	public OpCallInvoke(Operator operator) {
		this.operator = operator;
	}
	
	public final void execute(Context context) {
		context.call(operator);
	}	
}
