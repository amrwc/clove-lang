package uk.ac.derby.ldi.sili.vm.instructions;

import uk.ac.derby.ldi.sili.values.ValueInteger;
import uk.ac.derby.ldi.sili.vm.Cell;
import uk.ac.derby.ldi.sili.vm.CellFactory;
import uk.ac.derby.ldi.sili.vm.Context;
import uk.ac.derby.ldi.sili.vm.Instruction;

public class OpVariableInitialise extends Instruction {
	private final static long serialVersionUID = 0;

	private int depth;
	private int offset;
	private CellFactory cellFactory;

	/* For serialization support */
	
	public OpVariableInitialise() {
		this.depth = -1;
		this.offset = -1;
	}
	
	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	public int getDepth() {
		return this.depth;
	}
	
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	public int getOffset() {
		return this.offset;
	}
	
	public void setCellFactory(CellFactory cellFactory) {
		this.cellFactory = cellFactory;
	}
	
	public CellFactory getCellFactory() {
		return this.cellFactory;
	}
	
	/* End of serialization support. */
	
	public OpVariableInitialise(int depth, int offset, CellFactory cellFactory) {
		this.depth = depth;
		this.offset = offset;
		this.cellFactory = cellFactory;
	}

	// Create a new Cell, using the provided CellFactory.
	// POP - initial value to be stored in new cell.
	public final void execute(Context context) {
		Cell cell = cellFactory.getNewCell();
		cell.setValue(new ValueInteger(0));
		context.varSetCell(depth, offset, cell);
	}

	public String toString() {
		return getName() + " " + depth + " " + offset;
	}
}
