/** Convenient runner for Calc in uk.ac.derby.lpt.calc.parser package. */

public class Calc {
	public static void main(String[] args) {
		while (true)
			try {
				uk.ac.derby.ldi.calc.parser.Calc.main(args);
			} catch (Throwable t) {
				System.out.println("Error: " + t);
			}
	}
}
