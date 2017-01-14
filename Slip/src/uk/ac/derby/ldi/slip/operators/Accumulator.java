package uk.ac.derby.ldi.slip.operators;

import uk.ac.derby.ldi.slip.values.*;

public abstract class Accumulator extends Operator {

	private static final long serialVersionUID = 1L;

	/** Accumulate right Value into left using appropriate operator. */
	abstract Value accumulate(Value left, Value right);
	
	/** Evaluate. */
	public Value evaluate(Resolver resolver, Walker args) {
		throwIfNullArguments(args);
		Value accumulator = Nil.getInstance();
		if (args.hasNext()) {
			accumulator = args.next().evaluate(resolver);
			while (args.hasNext())
				accumulator = accumulate(accumulator, args.next().evaluate(resolver));
		}
		return accumulator;
	}
}
