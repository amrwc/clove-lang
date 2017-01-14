package uk.ac.derby.ldi.sili.vm;

import java.io.Serializable;
import java.util.*;

public class Operator implements Serializable {

	private static final long serialVersionUID = 0;
	
	private String signature;
	private List<Instruction> executableCode = new LinkedList<Instruction>();
	private transient Instruction[] executableCodeCache = null;
	private int depth;
	private int varCount;
	private int parmCount;
	
	// This ctor is used to permit XML object serialization
	public Operator() {
		this.signature = "";
		this.depth = 0;
	}
		
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
	
	// Only for XML serialization support
	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	public String getSignature() {
		return signature;
	}
	
	// Only for XML serialization support
	public void setDepth(int depth) {
		this.depth = depth;
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
	
	// Only for XML serialization support
	public void setExecutableCode(List<Instruction> executableCode) {
		this.executableCode = executableCode;
	}

	// Only for XML serialization support
	public List<Instruction> getExecutableCode() {
		return this.executableCode;
	}
	
	public Instruction[] obtainCode() {
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
