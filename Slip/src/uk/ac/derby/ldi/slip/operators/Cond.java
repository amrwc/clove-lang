package uk.ac.derby.ldi.slip.operators;

import uk.ac.derby.ldi.slip.values.*;

/** (cond (test1 res1) (test2 res2) ... (testn resn)) */
public class Cond extends Operator {

	private static final long serialVersionUID = 0;
	
	/** Evaluate each test in (test result) pairs in a list until test is true, then
	    return evaluation of result */
	public Value evaluate(Resolver resolver, Walker args) {
		while (args.hasNext()) {
			Bunch testPair = toBunch(resolver, args, "(test result)");
			if (testPair.getHead().evaluate(resolver).booleanValue())
				return testPair.getRest().evaluate(resolver);
		}
		return Nil.getInstance();
	}
	
	/** Obtain operator name. */
	public String getOperatorName() {
		return "cond";
	}
}
