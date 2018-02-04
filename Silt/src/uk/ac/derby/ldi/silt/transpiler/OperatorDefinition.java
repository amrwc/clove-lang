package uk.ac.derby.ldi.silt.transpiler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.ac.derby.ldi.sili.exceptions.ExceptionFatal;
import uk.ac.derby.ldi.sili.exceptions.ExceptionSemantic;
import uk.ac.derby.ldi.silt.parser.ast.Node;

/** This class captures information about the operator currently being defined, including its generated code.
 * 
 * @author dave
 *
 */
class OperatorDefinition {
	
	private OperatorDefinition parent;	
	private String name;
	private HashMap<String, OperatorDefinition> operators = new HashMap<String, OperatorDefinition>();
	private Map<String, Slot> slots = new HashMap<String, Slot>();
	private Vector<Parameter> parameters = new Vector<Parameter>();
	private boolean hasReturn = false;
	private String bodySource = "";
	
	private String getParmDecls() {
		String firstParameterType = (parent != null) ? parent.getSignature() + "_closure" : null;
		String firstParameter = (firstParameterType == null) ? "" : firstParameterType + " __closure";		
		String parmlist = 
				Stream.concat(
						Stream.of(firstParameter), 
						parameters.stream().map(parm -> "Value " + parm.getName()))
				.collect(Collectors.joining(", "));
		return "(" + parmlist + ")";
	}
	
	private String getNestedOperatorSource() {
		return operators.values().stream()
				.map(operator -> operator.getSource())
				.collect(Collectors.joining());
	}
	
	private String getClosureClassName() {
		return name + "_closure";
	}
	
	private String getClosureDef() {
		String vardefs = "";
		String ctorBody = "";
		String ctorParmDef = "";
		if (parent != null) {
			vardefs += "\t" + parent.getSignature() + "_closure __closure;\n";
			ctorBody += "\tthis.__closure = __closure;\n";
			ctorParmDef += parent.getSignature() + "_closure __closure";
		}
		for (Slot slot: slots.values()) {
			vardefs += "\tValue " + slot.getName() + ";\n";
			ctorBody += "\tthis." + slot.getName() + " = " + slot.getName() + ";\n";
			if (ctorParmDef.length() > 0)
				ctorParmDef += ", ";
			ctorParmDef += "Value " + slot.getName();
		}
		String closureClassName = getClosureClassName();
		return "static class " + closureClassName + " {\n" + 
				vardefs + 
				In.dent("public " + closureClassName + "(" + ctorParmDef + ") {\n" + ctorBody + "}\n") +
				"}\n";
	}

	private String getClosureConstruction() {
		String slotNames = 
				Stream.concat(
						Stream.of("__closure").filter(p -> parent != null), 
						slots.values().stream().map(slot -> slot.getName()))
				.collect(Collectors.joining(", "));
		return "new " + getClosureClassName() + "(" + slotNames + ")";
	}
	
	private String getVarDefs() {
		return slots.values().stream()
				.filter(slot -> slot instanceof Variable)
				.map(slot -> "Value " + slot.getName() + ";\n")
				.collect(Collectors.joining());
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
	
	private void checkSlotDefined(String refname) {
		if (isSlotDefined(refname))
			throw new ExceptionFatal("ERROR: " + refname + " is already defined in " + getSignature());
	}
	
	/** Add a nested operator to this operator. */
	private void addOperator(OperatorDefinition definition, Node node) {
		String signature = definition.getSignature();
		if (isOperatorDefined(signature))
			throw new ExceptionSemantic("Operator " + signature + " is already defined.", node);
		operators.put(signature, definition);
	}

	/** Ctor for operator definition. */
	OperatorDefinition(String operatorName, OperatorDefinition parent, Node node) {
		this.parent = parent;
		name = operatorName;
		if (parent != null)
			parent.addOperator(this, node);
	}
	
	/** Return true if an operator exists within this operator. */
	boolean isOperatorDefined(String signature) {
		return (operators.containsKey(signature));
	}
	
	/** Get the signature of this operator. */
	String getSignature() {
		return name;
	}
	
	/** Return true if a variable, parameter, or slot exists. */
	boolean isSlotDefined(String name) {
		return slots.containsKey(name);
	}

	void setHasReturn(boolean hasReturn) {
		this.hasReturn = hasReturn;
	}
	
	/** Get parent operator definition.  Null if this is the root operator. */
	OperatorDefinition getParentOperatorDefinition() {
		return parent;
	}

	/** Create a variable. */
	Slot createVariable(String refname) {
		checkSlotDefined(refname);
		Variable variable = new Variable(refname);
		slots.put(refname, variable);
		return variable;
	}

	/** Add a parameter */
	Slot addParameter(String refname) {
		checkSlotDefined(refname);
		Parameter parameter = new Parameter(refname);
		slots.put(refname, parameter);
		parameters.add(parameter);
		return parameter;
	}
	
	/** Get variable/parameter dereference Java code given name. */
	String findReference(String refname) {
		String outRef = refname;
		OperatorDefinition opDef = this;
		do {
			Slot slot = opDef.slots.get(refname);
			if (slot != null)
				return outRef;
			opDef = opDef.parent;
			outRef = "__closure." + outRef;
		} while (opDef != null);
		return null;
	}
	
	/** Get function invocation Java code given function name and argument list. */
	String findInvocation(String fnname, Vector<String> arglist) {
		OperatorDefinition foundOperator = null;
		OperatorDefinition opDef = this;
		int nesting = 0;
		do {
			foundOperator = opDef.operators.get(fnname);
			if (foundOperator != null)
				break;
			opDef = opDef.getParentOperatorDefinition();
			nesting++;
		} while (opDef != null);
		if (foundOperator == null)
			return null;		
		String firstArg = nesting > 0 
				? Collections.nCopies(nesting, "__closure").stream().collect(Collectors.joining(".")) 
				: getClosureConstruction();
		String arglistText = Stream.concat(
				Stream.of(firstArg),
				arglist.stream())
					.collect(Collectors.joining(", "));
		return fnname + "(" + arglistText + ")";		
	}
	
	/** Add source code to this definition. */
	void addSource(String source) {
		bodySource += source;
	}

	/** Get the source code for this definition. */
	String getSource() {
		return	getNestedOperatorSource() + 
				"\n" +
			 	getComment() +
				getClosureDef() +
			 	"\npublic static " + ((hasReturn) ? "Value " : "void ") + name + getParmDecls() + " {\n" + 
				In.dent(getVarDefs() + bodySource) + 
				"}\n";
	}
}
