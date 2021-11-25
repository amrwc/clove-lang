package dev.amrw.clovelang.interpreter;

import dev.amrw.clovelang.values.Value;

/**
 * A display manages run-time access to variable and parameter scope where
 * functions may be nested.
 * 
 * @author dave
 */
class Display {
	private final int maximumFunctionNesting = 64;
	private final FunctionInvocation[] display = new FunctionInvocation[maximumFunctionNesting];
	private int currentLevel;

	/** Reference to a slot. */
	class Reference {
		private final int displayDepth;
		private final int slotNumber;

		/** Ctor */
		Reference(int depth, int slot) {
			displayDepth = depth;
			slotNumber = slot;
		}

		/** Set value pointed to by this reference. */
		void setValue(Value v) {
			display[displayDepth].setValue(slotNumber, v);
		}

		/** Get value pointed to by this reference. */
		Value getValue() {
			return display[displayDepth].getValue(slotNumber);
		}
	}

	/** Ctor */
	Display() {
		// root or 0th scope
		currentLevel = 0;
		display[currentLevel] = new FunctionInvocation(
				new FunctionDefinition("%main", currentLevel));
	}

	/** Execute a function in its scope, using a specified parser. */
	Value execute(FunctionInvocation fn, Parser p) {
		final int changeLevel = fn.getLevel();
		final FunctionInvocation oldContext = display[changeLevel];
		final int oldLevel = currentLevel;
		display[changeLevel] = fn;
		currentLevel = changeLevel;
		final Value v = display[currentLevel].execute(p);
		display[changeLevel] = oldContext;
		currentLevel = oldLevel;
		return v;
	}

	/** Get the current scope nesting level. */
	int getLevel() {
		return currentLevel;
	}

	/**
	 * Return a Reference to a variable or parameter. Return null if it doesn't
	 * exist.
	 */
	Reference findReference(String name) {
		int level = currentLevel;
		while (level >= 0) {
			final int offset = display[level].findSlotNumber(name);
			if (offset >= 0)
				return new Reference(level, offset);
			level--;
		}
		return null;
	}

	/** Create a variable in the current level and return its Reference. */
	Reference defineVariable(String name) {
		return new Reference(currentLevel, display[currentLevel].defineVariable(name));
	}

	Reference defineConstant(String name) {
		return new Reference(currentLevel, display[currentLevel].defineConstant(name));
	}

	/** Find a function. Return null if it doesn't exist. */
	FunctionDefinition findFunction(String name) {
		int level = currentLevel;
		while (level >= 0) {
			final FunctionDefinition definition = display[level].findFunction(name);
			if (definition != null)
				return definition;
			level--;
		}
		return null;
	}

	/** Find a function in the current level. Return null if it doesn't exist. */
	FunctionDefinition findFunctionInCurrentLevel(String name) {
		return display[currentLevel].findFunction(name);
	}

	/** Add a function to the current level. */
	void addFunction(FunctionDefinition definition) {
		display[currentLevel].addFunction(definition);
	}

	// The methods below this point are here for scope management
	// in order to accommodate variables declared outside of functions.
	// NOTE: It's a hack for scoped operations, such as loops,
	// to make them inaccessible outside of their scope
	// -- a functionality not offered by this Display
	// implementation by default.
	/**
	 * Remove a scoped variable.
	 * 
	 * @author amrwc
	 */
	public void removeVariable(String name) {
		display[currentLevel].removeVariable(name);
	}

	/**
	 * Remove a scoped function.
	 * 
	 * @author amrwc
	 */
	public void removeFunction(String fnName) {
		display[currentLevel].removeFunction(fnName);
	}
}
