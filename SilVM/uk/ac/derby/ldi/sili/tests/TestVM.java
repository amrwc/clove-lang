package uk.ac.derby.ldi.sili.tests;

import org.junit.Test;

import uk.ac.derby.ldi.sili.values.*;
import uk.ac.derby.ldi.sili.vm.*;
import uk.ac.derby.ldi.sili.vm.instructions.*;

public class TestVM {

	private static VirtualMachine machine = new VirtualMachine(System.out);
	private CellMutableFactory cellGenerator = new CellMutableFactory();
	
	@Test
	public void testVMSimple() {
		machine.resetVM();
		Operator code = new Operator("Test", 0);
		code.compile(new OpPushLiteral(new ValueInteger(60)));		// PUSH 60
		code.compile(new OpPushLiteral(new ValueInteger(45)));		// PUSH 45
		code.compile(new OpAdd());									// +
		code.compile(new OpWriteRaw());								// WRITE
		code.compile(new OpReturn());								// RETURN
		(new Dumper()).dumpMachineCode(code);
		machine.execute(code);
	}

	@Test
	public void testVMComplex() {
		machine.resetVM();
		Operator code = new Operator("Test", 0, 1);
		code.compile(new OpPushLiteral(new ValueInteger(200)));			// PUSH 200
		code.compile(new OpVariableInitialise(0, 0, cellGenerator));	// assign to COUNTER
		code.compile(new OpPushLiteral(new ValueInteger(3)));		// <HERE> PUSH 3
		code.compile(new OpPushLiteral(new ValueInteger(4)));		// PUSH 4
		code.compile(new OpAdd());									// +
		code.compile(new OpWriteRaw());								// WRITE
		code.compile(new OpVariableGet(0, 0));						// COUNTER
		code.compile(new OpWriteRaw());								// WRITE
		code.compile(new OpVariableGet(0, 0));						// COUNTER
		code.compile(new OpPushLiteral(new ValueInteger(1)));		// PUSH 1
		code.compile(new OpSubtract());								// -
		code.compile(new OpVariableSet(0, 0));						// assign to COUNTER
		code.compile(new OpVariableGet(0, 0));						// COUNTER
		code.compile(new OpPushLiteral(new ValueInteger(1)));		// PUSH 1
		code.compile(new OpGte());									// >=
		code.compile(new OpBranchIfTrue(2));						// Jump if true to <HERE>
		code.compile(new OpReturn());								// RETURN
		(new Dumper()).dumpMachineCode(code);
		machine.execute(code);
	}

	@Test
	public void testVMFnCall() {
		machine.resetVM();
		
		// void writeInt(int x)
		Operator writeInt = new Operator("writeInt", 1);
		writeInt.setParameterCount(1);
		writeInt.compile(new OpParameterGet(1, 0));					// PUSH x
		writeInt.compile(new OpWriteRaw());							// write X
		writeInt.compile(new OpReturn());							// RETURN
		
		// main
		Operator main = new Operator("Test", 0, 1);
		main.compile(new OpPushLiteral(new ValueInteger(200)));			// PUSH 200
		main.compile(new OpVariableInitialise(0, 0, cellGenerator));	// assign to COUNTER
																	// <HERE> writeInt(
		main.compile(new OpVariableGet(0, 0));						// COUNTER
		main.compile(new OpCallInvoke(writeInt));					// );
		main.compile(new OpVariableGet(0, 0));						// COUNTER
		main.compile(new OpPushLiteral(new ValueInteger(1)));		// PUSH 1
		main.compile(new OpSubtract());								// -
		main.compile(new OpVariableSet(0, 0));						// assign to COUNTER
		main.compile(new OpVariableGet(0, 0));						// COUNTER
		main.compile(new OpPushLiteral(new ValueInteger(1)));		// PUSH 1
		main.compile(new OpGte());									// >=
		main.compile(new OpBranchIfTrue(2));						// Jump if true to <HERE>
		main.compile(new OpReturn());								// RETURN
		(new Dumper()).dumpMachineCode(main);
		machine.execute(main);		
	}
	
}
