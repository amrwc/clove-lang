package uk.ac.derby.ldi.calcast.calculator;

import uk.ac.derby.ldi.calcast.parser.*;

public class Calculator {
	
	public static void main(String args[]) {
		Calc calculator = new Calc(System.in);
		try {
			System.out.println("CalcAST version 1.0");
			while (true)
				calculator.code().jjtAccept(new Parser(), null);
		} catch (Throwable e) {
			System.out.println(e.getMessage());
		}
	}

}
