package dtool.parser.ddoc;


import org.junit.Test;

import descent.core.ddoc.DdocParser;
import dtool.tests.CommonDToolTest;

// skeleton test
public class DDocParser_Test extends CommonDToolTest {
	
	@Test
	public void testname() throws Exception { testname$(); }
	public void testname$() throws Exception {
		 DdocParser ddocParser = new DdocParser("");
		 ddocParser.parse();
		 
		 new DdocParser("/+\n+/").parse();
		 new DdocParser("/+ \n+/").parse();
		 new DdocParser("/+").parse();
		 new DdocParser("/+ +/").parse();
		 
		 new DdocParser("/** **/").parse();
	}
	
}
