package uk.ac.derby.ldi.sili.vm.instructions;

import uk.ac.derby.ldi.sili.values.Value;
import uk.ac.derby.ldi.sili.vm.Context;
import uk.ac.derby.ldi.sili.vm.Instruction;

public class OpPushLiteral extends Instruction {
	private Value v;
	
	/* For serialization */
	
	public OpPushLiteral() {
		v = null;
	}
	
	public void setValue(Value v) {
		this.v = v;
	}
	
	public Value getValue() {
		return this.v;
	}
	
	/* End of serialization definitions */
	
	public OpPushLiteral(Value v) {
		this.v = v;
	}
	
	public final void execute(Context context) {
		context.pushLiteral(v);
	}
	
	public String toString() {
		return getName() + " " + v;
	}
}
