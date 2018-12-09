package uk.ac.derby.ldi.silc.compiler;

import uk.ac.derby.ldi.sili.vm.instructions.*;

public class Parameter extends Slot {
	
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
}

