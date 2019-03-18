package uk.ac.derby.ldi.slip.tests;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTests extends TestCase {

	public static TestSuite suite() {
		TestSuite suite = new TestSuite("Tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestValues.class);
		suite.addTestSuite(TestEngine.class);
		suite.addTestSuite(TestIsolated.class);
		//$JUnit-END$
		return suite;
	}

}
