package uk.ac.derby.ldi.sili.vm;

/** Base class for VM operators. */
public abstract class Instruction {
	
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