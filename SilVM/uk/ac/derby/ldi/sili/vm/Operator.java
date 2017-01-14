package uk.ac.derby.ldi.sili.vm;


import java.util.*;

public class Operator {

	private String signature;
	private List<Instruction> executableCode = new LinkedList<Instruction>();
	private transient Instruction[] executableCodeCache = null;
	private int depth;
	private int varCount;
	private int parmCount;
	
	public Operator(String signature, int depth) {
		this.signature = signature;
		this.depth = depth;
	}

	public Operator(String signature, int depth, int varCount) {
		this(signature, depth);
		this.varCount = varCount;
	}

	public void compile(Instruction op) {
		executableCode.add(op);
		executableCodeCache = null;
	}
	
	public void compileAt(int address, Instruction op) {
		executableCode.set(address, op);
		executableCodeCache = null;
	}
	
	public String getSignature() {
		return signature;
	}
	
	public int getDepth() {
		return depth;
	}

	public void setVariableCount(int n) {
		varCount = n;
	}
	
	public int getVariableCount() {
		return varCount;
	}
	
	public void setParameterCount(int n) {
		parmCount = n;
	}
	
	public int getParameterCount() {
		return parmCount;
	}
	
	public Instruction[] getExecutableCode() {
		if (executableCodeCache == null)
			executableCodeCache = executableCode.toArray(new Instruction[0]);
		return executableCodeCache;
	}

	/** Get code size. */
	public int size() {
		return executableCode.size();
	}
	
	public String toString() {
		return "<" + signature + "[" + depth + "]>";
	}
}
