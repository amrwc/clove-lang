package uk.ac.derby.ldi.sili.vm;

import java.util.Vector;

import uk.ac.derby.ldi.sili.vm.instructions.OpCallInvoke;

import java.util.Collections;
import java.util.HashSet;

public class Dumper {

	private HashSet<String> printed;
	
	public HashSet<Operator> dumpMachineCode(Instruction code[]) {
		HashSet<Operator> invoked = new HashSet<>();
		int address = 0;
		for (Instruction op : code) {
			if (op == null)
				System.out.print("<NULL>");
			else if (op instanceof Instruction) {
				Instruction instruction = (Instruction)op;
				System.out.println();
				System.out.print(address + ": " + instruction.toString());
				if (op instanceof OpCallInvoke) {
					Operator invokedOperator = ((OpCallInvoke)op).getOperator();
					invoked.add(invokedOperator);
					String invokedSignature = invokedOperator.getSignature();
					System.out.print(" " + invokedSignature);
				}
			} else
				System.out.print(op.toString());
			address++;
			System.out.print(" ");
		}
		return invoked;
	}
	
	public void dump(Operator operator) {
		Vector<Operator> operators = new Vector<Operator>();
		if (printed.contains(operator.toString()))
			return;
		String heading = "--------"	+ operator.toString() + "--------";
		System.out.print(heading);
		printed.add(operator.toString());
		operators.addAll(dumpMachineCode(operator.obtainCode()));
		String footer = String.join("", Collections.nCopies(heading.length(), "-"));
		System.out.println("\n" + footer);
		for (Operator code: operators) {
			dump(code);
		}
	}
	
	/** Dump machine code. */
	public void dumpMachineCode(Operator operator) {
		 printed = new HashSet<String>();
		 dump(operator);
	}

}
