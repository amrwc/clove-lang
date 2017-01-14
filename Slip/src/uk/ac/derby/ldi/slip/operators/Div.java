package uk.ac.derby.ldi.slip.operators;

import uk.ac.derby.ldi.slip.values.*;

/** (/ parm1 parm2 ... parmn) */
public class Div extends Accumulator {

	private static final long serialVersionUID = 0;

	/** Accumulate right Value into left using appropriate operator. */
	Value accumulate(Value left, Value right) {
		return left.div(right);
	}
	
	/** Obtain operator name. */
	public String getOperatorName() {
		return "/";
	}
}
