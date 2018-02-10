package uk.ac.derby.ldi.silc.compiler;

import uk.ac.derby.ldi.sili.vm.instructions.*;

public class Variable extends Slot {
	
	public Variable(int depth, int offset) {
		super(depth, offset);
	}
	
	@Override
	public void compileGet(Generator generator) {
		// Compile retrieval of variable value
		generator.compileInstruction(new OpVariableGet(getDepth(), getOffset()));
	}

	@Override
	public void compileSet(Generator generator) {
		// compile assignment
		generator.compileInstruction(new OpVariableSet(getDepth(), getOffset()));
	}
}
