package uk.ac.derby.ldi.slip.operators;

import uk.ac.derby.ldi.slip.values.*;

/** (and parm1 parm2 ... parmn) */
public class And extends Operator {

	private static final long serialVersionUID = 0;
		
	/** Return true if all items are true. */
	public Value evaluate(Resolver resolver, Walker args) {
		throwIfNullArguments(args);
		while (args.hasNext()) {
			if (!args.next().evaluate(resolver).booleanValue()) {
				args.terminate();
				return new Bool(false);				
			}
		}
		return new Bool(true);
	}
	
	public String getOperatorName() {
		return "and";
	}
}
