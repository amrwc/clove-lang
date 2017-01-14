package uk.ac.derby.ldi.slip.values;

/** A RootResolver is the Resolver (it should be singleton) that
 * is the root of the Resolver tree and manages persistence for the hierarchy. */
public class RootResolver extends Resolver {

	public static final long serialVersionUID = 0;
	
	/** Create the root resolver. */
	public RootResolver() {
		super(null);
	}
	
}
