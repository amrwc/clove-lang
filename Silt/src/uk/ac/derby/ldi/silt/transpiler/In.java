package uk.ac.derby.ldi.silt.transpiler;

import java.util.regex.Pattern;

public class In {
	
	public static String dent(Object o) {
		if (o == null)
			return null;
		return Pattern.compile("^", Pattern.MULTILINE).matcher(o.toString()).replaceAll("\t");
	}

}
