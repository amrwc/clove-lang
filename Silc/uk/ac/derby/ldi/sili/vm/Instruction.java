package uk.ac.derby.ldi.sili.vm;

import java.io.Serializable;

/** Base class for VM operators. */
public abstract class Instruction implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/** Get this instruction's name. */
    public final String getName() {
        return getClass().getSimpleName();
    }
    
    /** Stringify */
    public String toString() {
        return getName();
    }
    
    /** Execute this instruction on a given Context. */ 
    public abstract void execute(Context context);
}