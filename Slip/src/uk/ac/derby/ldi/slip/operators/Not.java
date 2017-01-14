package uk.ac.derby.ldi.slip.operators;

import uk.ac.derby.ldi.slip.values.*;

/** (not parm1 parm2 ... parmn) */
public class Not extends Operator {

	private static final long serialVersionUID = 0;
	
	/** Evaluate. */
	public Value evaluate(Resolver resolver, Walker args) {
		throwIfNullArguments(args);
		Bunch newList = null;
		if (args.hasNext()) {
			Value v = args.next().evaluate(resolver);
			if (!args.hasNext())
				return v.not();
			else {
				 newList = new Bunch();
				 newList.insert(v.not());
			}
			while (args.hasNext())
				newList.insert(args.next().evaluate(resolver).not());
		}
		if (newList == null)
			return new Bunch();
		return newList;
	}
	
	public String getOperatorName() {
		return "not";
	}
}
