package uk.ac.derby.ldi.silt.transpiler;

import java.util.regex.Pattern;

/** Indentation tool. */

public class In {
	
	public static String dent(Object o) {
		if (o == null)
			return null;
		// Put a tab at the start of every line
		return Pattern.compile("^", Pattern.MULTILINE).matcher(o.toString()).replaceAll("\t");
	}

}
