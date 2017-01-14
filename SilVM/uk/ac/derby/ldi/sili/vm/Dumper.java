package uk.ac.derby.ldi.sili.vm;

import java.util.Vector;
import java.util.HashSet;

public class Dumper {

	private HashSet<String> printed;
	
	public void dumpMachineCode(Instruction code[]) {
		int address = 0;
		for (Instruction op : code) {
			if (op == null)
				System.out.print("<NULL>");
			else if (op instanceof Instruction) {
				Instruction instruction = (Instruction)op;
				System.out.println();
				System.out.print(address + ": " + instruction.toString());
			} else
				System.out.print(op.toString());
			address++;
			System.out.print(" ");
		}		
	}
	
	public void dump(Operator operator) {
		System.out.print("--------"	+ operator.toString() + "--------");
		Vector<Operator> operators = new Vector<Operator>();
		if (printed.contains(operator.toString()))
			return;
		printed.add(operator.toString());
		dumpMachineCode(operator.getExecutableCode());
		System.out.println();
		System.out.println("-----------------");		
		for (Operator code : operators) {
			dump(code);
		}
	}
	
	/** Dump machine code. */
	public void dumpMachineCode(Operator operator) {
		 printed = new HashSet<String>();
		 dump(operator);
	}

}
