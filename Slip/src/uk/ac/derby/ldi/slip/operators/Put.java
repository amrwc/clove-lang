package uk.ac.derby.ldi.slip.operators;

import uk.ac.derby.ldi.slip.values.*;

/** (put parm1 parm2 ... parmn) */
public class Put extends Operator {

	private static final long serialVersionUID = 0;
	
	/** Print to stdout. */
	public Value evaluate(Resolver resolver, Walker args) {
		throwIfNullArguments(args);
		while (args.hasNext())
			System.out.print(args.next().evaluate(resolver).stringValue());
		return Nil.getInstance();
	}
	
	/** Obtain operator name. */
	public String getOperatorName() {
		return "put";		
	}
}
