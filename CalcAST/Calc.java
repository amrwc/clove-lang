/** Convenient runner for calculator. */

public class Calc {
	public static void main(String[] args) {
		try {
			uk.ac.derby.ldi.calcast.calculator.Calculator.main(args);
		} catch (Throwable t) {
			System.out.println("Error: " + t);
		}
	}
}
