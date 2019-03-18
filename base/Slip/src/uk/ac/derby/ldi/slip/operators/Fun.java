package uk.ac.derby.ldi.slip.operators;

import uk.ac.derby.ldi.slip.values.*;

/** (fun (parmlist) (body)) */
public class Fun extends Operator {

	private static final long serialVersionUID = 0;
	
	/** Define a lambda (anonymous) function. */
	public Value evaluate(Resolver resolver, Walker args) {
		return new Lambda(toBunch(resolver, args, "parameter definition"), toBunch(resolver, args, "body"));
	}
	
	/** Obtain operator name. */
	public String getOperatorName() {
		return "fun";
	}
}
