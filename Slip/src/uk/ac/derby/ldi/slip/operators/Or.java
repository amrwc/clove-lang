package uk.ac.derby.ldi.slip.operators;

import uk.ac.derby.ldi.slip.values.*;

/** (or parm1 parm2 ... parmn) */
public class Or extends Operator {

	private static final long serialVersionUID = 0;
		
	/** Return true if any item is true. */
	public Value evaluate(Resolver resolver, Walker args) {
		throwIfNullArguments(args);
		while (args.hasNext()) {
			if (args.next().evaluate(resolver).booleanValue()) {
				args.terminate();
				return new Bool(true);
			}
		}
		return new Bool(false);
	}
	
	public String getOperatorName() {
		return "or";
	}
}
