package uk.ac.derby.ldi.slip.operators;

import uk.ac.derby.ldi.slip.values.*;

/** (prog parm1 parm2 ... parmn) */
public class Prog extends Operator {

	private static final long serialVersionUID = 0;
	
	/** Sequentially evaluate each item in a list. */
	public Value evaluate(Resolver resolver, Walker args) {
		return evaluateBody(resolver, args);
	}
	
	/** Obtain operator name. */
	public String getOperatorName() {
		return "prog";
	}
}
