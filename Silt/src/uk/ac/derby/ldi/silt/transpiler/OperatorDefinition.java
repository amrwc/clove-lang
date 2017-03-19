package uk.ac.derby.ldi.silt.transpiler;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import uk.ac.derby.ldi.sili.exceptions.ExceptionSemantic;
import uk.ac.derby.ldi.silt.parser.ast.Node;

/** This class captures information about the operator currently being defined, including its generated code.
 * 
 * @author dave
 *
 */
public class OperatorDefinition {
	
	private OperatorDefinition parent;	
	private String name;
	private HashMap<String, OperatorDefinition> operators = new HashMap<String, OperatorDefinition>();
	private Map<String, Slot> slots = new HashMap<String, Slot>();
	private Vector<Parameter> parameters = new Vector<Parameter>();
	private boolean hasReturn = false;
	private String bodySource = "";

	/** Ctor for operator definition. */
	public OperatorDefinition(String operatorName, OperatorDefinition parent, Node node) {
		this.parent = parent;
		name = operatorName;
		if (parent != null)
			parent.addOperator(this, node);
	}
	
	/** Get the signature of this operator. */
	public String getSignature() {
		return name;
	}

	public void setHasReturn(boolean hasReturn) {
		this.hasReturn = hasReturn;
	}
	
	/** Get parent operator definition.  Null if this is the root operator. */
	OperatorDefinition getParentOperatorDefinition() {
		return parent;
	}
	
	/** Return true if a variable, parameter, or slot exists. */
	boolean isDefined(String name) {
		return slots.containsKey(name);
	}

	private void checkDefined(String name, Node node) {
		if (isDefined(name))
			throw new ExceptionSemantic(name + " is already defined in operator " + getSignature(), node);		
	}
	
	/** Return true if an operator exists. */
	private boolean isOperatorDefined(String signature) {
		return (operators.containsKey(signature));
	}

	public Slot createVariable(String refname, Node node) {
		checkDefined(refname, node);
		Variable variable = new Variable(refname);
		slots.put(refname, variable);
		return variable;
	}
	
	public Slot addParameter(String refname, Node node) {
		checkDefined(refname, node);
		Parameter parameter = new Parameter(refname);
		slots.put(refname, parameter);
		parameters.add(parameter);
		return parameter;
	}
	
	public String findReference(String refname) {
		String outRef = refname;
		OperatorDefinition opDef = this;
		do {
			Slot slot = opDef.slots.get(refname);
			if (slot != null) {
				return outRef;
			}
			opDef = opDef.parent;
			outRef = "__closure." + outRef;
		} while (opDef != null);
		return null;
	}
	
	/** Add a nested operator to this operator. */
	private void addOperator(OperatorDefinition definition, Node node) {
		String signature = definition.getSignature();
		if (isOperatorDefined(signature))
			throw new ExceptionSemantic("Operator " + signature + " is already defined.", node);
		operators.put(signature, definition);
	}
	
	/** Return an operator in this or an outer operator definition. Null if we can't find it. */
	OperatorDefinition getOperator(String signature) {
		OperatorDefinition opDef = this;
		do {
			OperatorDefinition operator = opDef.operators.get(signature);
			if (operator != null)
				return operator;
			opDef = opDef.getParentOperatorDefinition();
		} while (opDef != null);
		return null;		
	}

	public void addSource(String source) {
		bodySource += source;
	}

	private String getParmDecls() {
		String parmlist = (parent != null) ? parent.getSignature() + "_closure __closure" : "";
		for (Parameter parm: parameters) {
			if (!parmlist.isEmpty())
				parmlist += ", ";
			parmlist += "Value " + parm.getName();
		}
		return "(" + parmlist + ")";
	}
	
	private String getNestedOperatorSource() {
		String out = "";
		for (OperatorDefinition op: operators.values())
			out += op.getSource();
		return out;
	}
	
	public String getClosureClassName() {
		return name + "_closure";
	}
	
	private String getClosureDef() {
		String vardefs = "";
		String ctorBody = "";
		String ctorParmDef = "";
		if (parent != null) {
			vardefs += "\tpublic " + parent.getSignature() + "_closure __closure;\n";
			ctorBody += "\tthis.__closure = __closure;\n";
			ctorParmDef += parent.getSignature() + "_closure __closure";
		}
		for (Slot slot: slots.values()) {
			vardefs += "\tpublic Value " + slot.getName() + ";\n";
			ctorBody += "\tthis." + slot.getName() + " = " + slot.getName() + ";\n";
			if (ctorParmDef.length() > 0)
				ctorParmDef += ", ";
			ctorParmDef += "Value " + slot.getName();
		}
		String closureClassName = getClosureClassName();
		return "public static class " + closureClassName + " {\n" + 
				vardefs + 
				In.dent("public " + closureClassName + "(" + ctorParmDef + ")\n{\n" + ctorBody + "}\n") +
				"}\n";
	}

	public String getClosureConstruction() {
		String slotNames = "";
		if (parent != null)
			slotNames += "__closure";
		for (Slot slot: slots.values()) {
			if (slotNames.length() > 0)
				slotNames += ", ";
			slotNames += slot.getName();
		}
		return "new " + getClosureClassName() + "(" + slotNames + ")";
	}
	
	private String getVarDefs() {
		String vardefs = "";
		for (Slot slot: slots.values())
			if (slot instanceof Variable)
				vardefs += "Value " + slot.getName() + ";\n";
		return vardefs;
	}
	
	private String getComment() {
		String content = "";
		OperatorDefinition opDef = this;
		do {
			if (content.length() > 0)
				content += " in ";
			content += opDef.getSignature();
			opDef = opDef.getParentOperatorDefinition();
		} while (opDef != null);
		return "/** " + content + " */\n\n";
	}

	public String getSource() {
		return	getNestedOperatorSource() + 
				"\n" +
			 	getComment() +
				getClosureDef() +
			 	"\npublic static " + ((hasReturn) ? "Value " : "void ") + name + getParmDecls() + 
				" {\n" + 
				In.dent(getVarDefs() + bodySource) + 
				"}\n";
	}
}
