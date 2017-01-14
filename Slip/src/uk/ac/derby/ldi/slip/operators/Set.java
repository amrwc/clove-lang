package uk.ac.derby.ldi.slip.operators;

import uk.ac.derby.ldi.slip.values.*;

/** (set name1 value1 ...) or (set (expr1) value1 ...) */
public class Set extends Operator {

	private static final long serialVersionUID = 0;
	
	/** Assign a variable. */
	public Value evaluate(Resolver resolver, Walker args) {
		Value lastAssignedValue = Nil.getInstance();
		while (args.hasNext()) {
			Value assignTo = args.next();
			check(args.hasNext(), "Missing expression for assignment to variable");
			if (!(assignTo instanceof Identifier)) {
				assignTo = assignTo.evaluate(resolver);
				check(assignTo instanceof Str, "Variable name expression must resolve to an identifier");
			}
			lastAssignedValue = resolver.set(assignTo, args.next().evaluate(resolver));
		}
		return lastAssignedValue;
	}
	
	/** Obtain operator name. */
	public String getOperatorName() {
		return "set";
	}
}
