package uk.ac.derby.ldi.silc.compiler;

import uk.ac.derby.ldi.sili.vm.CellMutableFactory;
import uk.ac.derby.ldi.sili.vm.instructions.*;

public class Variable extends Slot {
	
	private static CellMutableFactory variableFactory = new CellMutableFactory();
	private boolean firstUse = true;
	
	public Variable(int depth, int offset) {
		super(depth, offset);
	}
	
	@Override
	public void compileGet(Generator generator) {
		if (firstUse) {
			compileInitialise(generator);
			firstUse = false;
		}
		// Compile retrieval of variable value
		generator.compileInstruction(new OpVariableGet(getDepth(), getOffset()));
	}

	@Override
	public void compileSet(Generator generator) {
		if (firstUse) {
			compileInitialise(generator);
			firstUse = false;
		}
		// compile assignment
		generator.compileInstruction(new OpVariableSet(getDepth(), getOffset()));
	}
	
	@Override
	public void compileInitialise(Generator generator) {
		generator.compileInstruction(new OpVariableInitialise(getDepth(), getOffset(), variableFactory));
	}
}
