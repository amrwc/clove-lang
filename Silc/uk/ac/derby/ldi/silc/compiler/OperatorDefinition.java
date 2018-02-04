package uk.ac.derby.ldi.silc.compiler;

import java.util.HashMap;

import uk.ac.derby.ldi.sili.exceptions.ExceptionSemantic;
import uk.ac.derby.ldi.sili.vm.Instruction;
import uk.ac.derby.ldi.sili.vm.Operator;
import uk.ac.derby.ldi.sili.vm.instructions.*;

/** This class captures information about the Rel operator currently being defined,
 * including its generated code.
 * 
 * @author dave
 *
 */
public class OperatorDefinition {
	
	private OperatorDefinition parent;	
	private String name;
	private Operator operator;
	private HashMap<String, OperatorDefinition> operators = new HashMap<String, OperatorDefinition>();
	private HashMap<String, Slot> slots = new HashMap<String, Slot>();
	private boolean definedReturnValue = false;
	private boolean isUsedInExpression = false;

	/** Ctor for operator definition. */
	public OperatorDefinition(String operatorName, OperatorDefinition parentDefinition) {
		parent = parentDefinition;
		name = operatorName;
		operator = new Operator(operatorName, (parent == null) ? 0 : parent.operator.getDepth() + 1);
	}
	
	/** Get the signature of this operator. */
	public String getSignature() {
		return name;
	}
	
	/** Get static depth. */
	int getDepth() {
		return operator.getDepth();
	}

	/** Set whether or not this operator has defined a return value via a RETURN statement. */
	public void setDefinedReturnValue(boolean flag) {
		definedReturnValue = flag;
	}
	
	/** Return true if this operator has defined a return value via a RETURN statement. */
	public boolean hasDefinedReturnValue() {
		return definedReturnValue;
	}
	
	/** Get parent operator definition.  Null if this is the root operator. */
	OperatorDefinition getParentOperatorDefinition() {
		return parent;
	}

	/** Compile an Instruction. */
	void compile(Instruction o) {
		operator.compile(o);
	}
	
	/** Compile an Instruction at a given address. */
	void compileAt(Instruction o, int address) {
		operator.compileAt(address, o);
	}
	
	/** Return current compilation address. */
	int getCP() {
		return operator.size();
	}
	
	/** Get executable code. */
	Operator getOperator() {
		return operator;
	}
	
	/** Get size of executable code. */
	int getExecutableSize() {
		return operator.size();
	}
	
	/** Return true if a variable, parameter, or slot exists. */
	boolean isDefined(String name) {
		return slots.containsKey(name);
	}

	private void checkDefined(String name) {
		if (isDefined(name))
			throw new ExceptionSemantic(name + " is already defined in operator " + getSignature());		
	}
	
	/** Define a slot. */
	void defineSlot(String name, Slot slot) {
		checkDefined(name);
		slots.put(name, slot);
	}
	
	private void defVar(String name, Slot slot) {
		defineSlot(name, slot);
		operator.setVariableCount(operator.getVariableCount() + 1);		
	}
	
	/** Define a variable. */
	void defineVariable(String name) {
		defVar(name, new Variable(operator.getDepth(), operator.getVariableCount()));
	}
	
	/** Define a parameter. */
	void defineParameter(String name) {
		defineSlot(name, new Parameter(operator.getDepth(), operator.getParameterCount()));
		operator.setParameterCount(operator.getParameterCount() + 1);
	}
	
	/** Get the location of a variable, parameter or other identifier.  Return null if it doesn't exist. */
	Slot getReference(String name) {
		OperatorDefinition definition = this;
		while (definition != null) {
			Slot slot = definition.slots.get(name);
			if (slot != null)
				return slot;
			definition = definition.getParentOperatorDefinition();
		}
		return null;
	}
	
	/** Return true if an operator exists. */
	private boolean isOperatorDefined(String signature) {
		return (operators.containsKey(signature));
	}
	
	/** Define an operator. */
	void defineOperator(OperatorDefinition definition) {
		String signature = definition.getSignature();
		if (isOperatorDefined(signature))
			throw new ExceptionSemantic("Operator " + signature + " is already defined.");
		operators.put(signature, definition);
	}
	
	/** Return an operator. */
	OperatorDefinition getOperator(String signature) {
		OperatorDefinition definition = this;
		while (definition != null) {
			OperatorDefinition operator = definition.operators.get(signature);
			if (operator != null)
				return operator;
			definition = definition.getParentOperatorDefinition();
		}
		return null;		
	}
	
	public void compileEvaluate(Generator generator) {
		generator.compileInstruction(new OpCallInvoke(getOperator()));
		isUsedInExpression = true;
	}
	
	public void compileCall(Generator generator) {
		generator.compileInstruction(new OpCallInvoke(getOperator()));
		if (hasDefinedReturnValue())
			generator.compilePop();
	}
	
	public boolean isUsedInExpression() {
		return isUsedInExpression;
	}
	
}
