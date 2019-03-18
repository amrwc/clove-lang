package uk.ac.derby.ldi.slip.operators;

import uk.ac.derby.ldi.slip.values.*;

/** (if bool expr1 expr2) */
public class If extends Operator {

	private static final long serialVersionUID = 0;
	
	/** If first parm is true, return evaluation of second parm.  
	 * Else return evaluation of third parm. */
	public Value evaluate(Resolver resolver, Walker args) {
		Value[] parms = toRawArgumentsArray(args, 3);
		if (parms[0].evaluate(resolver).booleanValue())
			return parms[1].evaluate(resolver);
		else 
			return parms[2].evaluate(resolver);
	}
	
	/** Obtain operator name. */
	public String getOperatorName() {
		return "if";
	}
}
