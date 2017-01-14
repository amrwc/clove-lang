package uk.ac.derby.ldi.slip.operators;

import uk.ac.derby.ldi.slip.values.*;

/** (let ((var1 value1) (var2 value2) ... (varn valuen)) body1 body2 ... bodyn) */
public class Let extends Operator {

	private static final long serialVersionUID = 0;
	
	/** Execute body in a local variable context. */
	public Value evaluate(Resolver resolver, Walker args) {
		// Make sure there's a variable definition list
		Walker vardefs = toBunch(resolver, args, "variable definitions list").getWalker();
		// Create new context as child of current context
		Resolver newContext = new Resolver(resolver);
		// Process var defs ((var1 value1) (var2 value2) ... (varn valuen))
		while (vardefs.hasNext()) {
			// Process var def (var1 value1)
			Walker vardef = toBunch(resolver, vardefs, "variable definition list").getWalker();
			check(vardef.hasNext(), "Empty variable definition list");
			Value identifier = vardef.next().evaluate(resolver);
			check(identifier instanceof Identifier, "First item in a variable definition must be an identifier");
			if (vardef.hasNext()) {
				newContext.put(identifier, vardef.next().evaluate(resolver));
				check(!vardef.hasNext(), "Each variable definition should have at most two items");
			} else
				newContext.put(identifier, Nil.getInstance());
		}
		return evaluateBody(newContext, args);
	}
	
	/** Obtain operator name. */
	public String getOperatorName() {
		return "let";
	}
}
