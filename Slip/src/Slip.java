import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import uk.ac.derby.ldi.slip.engine.Evaluator;

public class Slip {

	public static void main(String[] args) throws IOException {
		Evaluator.eval(new BufferedReader(new InputStreamReader(System.in)), System.out);
	}

}
