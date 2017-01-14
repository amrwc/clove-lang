package uk.ac.derby.ldi.silc.compiler;

import uk.ac.derby.ldi.sili.vm.CellMutableFactory;
import uk.ac.derby.ldi.sili.vm.instructions.*;

public class Parameter extends Slot {
	
	private static CellMutableFactory variableFactory = new CellMutableFactory();
	
	public Parameter(int depth, int offset) {
		super(depth, offset);
	}

	@Override
	public void compileGet(Generator generator) {
		// Compile retrieval of parameter value
		generator.compileInstruction(new OpParameterGet(getDepth(), getOffset()));
	}

	@Override
	public void compileSet(Generator generator) {
		// compile assignment
		generator.compileInstruction(new OpParameterSet(getDepth(), getOffset()));
	}
		
	@Override
	public void compileInitialise(Generator generator) {
		generator.compileInstruction(new OpVariableInitialise(getDepth(), getOffset(), variableFactory));
	}
}

