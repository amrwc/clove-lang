package uk.ac.derby.ldi.slip.values;

public class Lambda extends Operator {

	private static final long serialVersionUID = 0;

	private Bunch parameters;
	private Bunch executable;

	public Lambda(Bunch params, Bunch code) {
		parameters = params;
		executable = code;
	}
	
	public Value evaluate(Resolver resolver, Walker args) {
		// Bind parameters to arguments in iterator.
		Resolver context = new Resolver(resolver);
		Walker pI = parameters.getWalker();
		while (pI.hasNext()) {
			Value parameter = pI.next().evaluate(resolver);
			check(parameter instanceof Identifier, "Parameter " + parameter + " should evaluate to an identifier");
			check(args.hasNext(), "Argument expected");
			context.put(parameter, args.next().evaluate(resolver));
		}
		check(!args.hasNext(), "More arguments than expected");
		// Evaluate it, using new context.
		return executable.evaluate(context);
	}
	
	public String getOperatorName() {
		return "fun";
	}

}
