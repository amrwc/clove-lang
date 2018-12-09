/**
 * 
 */
package uk.ac.derby.ldi.silc.compiler;

/** Contains information about a variable, parameter, or other scopable identifier. */
public abstract class Slot {
	private int depth;
	private int offset;
	
	public Slot(int depth, int offset) {
		this.depth = depth;
		this.offset = offset;
	}
	
	public int getOffset() {
		return offset;
	}
	
	public int getDepth() {
		return depth;
	}
	
	/** Compile setter, which is invoked by assignment operation.  Value to be assigned is on stack. */
	public abstract void compileSet(Generator generator);
	
	/** Compile getter, which is invoked by identifier dereference.  Value will be pushed onto stack. */
	public abstract void compileGet(Generator generator);
}
