/*
 * CS362 Winter 2017
 * Final Project Part B
 * Team members: Linh Vu (vuli), Jessica Spokoyny (spokoynj), Chris Kearns (kearnsc)
 * Last updated: 18 Mar 2017
 * NOTES:
 *  - Constructors tested: default, ALLOW_ALL_SCHEMES, NO_FRAGMENTS,
 *  	ALLOW_2_SLASHES, ALLOW_LOCAL_URLs, schemes != NULL
 *  - Potential bugs in UrlValditorCorrect:
 *  	+) Ports 65536-99999 evaluated to valid
 *  	+) ALLOW_LOCAL_URLS is wonky -- enabled, 'www.google' evaluated to valid
 *  	+) Query regex matches anything that starts with #, i.e. '# nonsense!' 
 *  		and '#/more nonsense' evaluated to valid
 * TODO:
 *  - Add unit tests for more constructors/partitions (unitTestXX)
 *  - Add edge tests (similar to testValidator202()) to each unitTestXX
 *  - Add to testSchemes, etc. to make more robust/improve input partitioning
 *  - Log bugs and debug
 *  - Prettier formatting? (look up StringUtils.padLeft/padRight)
 *  - Revise manual test/input partitioning test?
 */


import org.junit.Test;


public class UrlValidatorTest {
   private enum PrintOption {
	   ALL, ON_FAIL, NONE
   }
   private static final PrintOption PRINT = PrintOption.ON_FAIL;   

   
/*-------------------------------------------------------------------------*/
/* UrlPart templates */
  private static final String[] testSchemesTemplate = {"http://",
	   "https://",
	   "ftp://",
	   "HtTpS://", 
	   "htp://", 
	   "http1://", 
	   "1ftp://", 
	   "https:/", 
	   "fpt:", 
	   "://", 
	   " ", 
	   "",
  };

  private static final String[] testSchemesTemplate_wo_slashes = {"http", 
		   "https", 
		   "ftp",  
		   "HtTpS", 
		   "htp", 
		   "http1", 
		   "1ftp", 
		   "https:/", 
		   "fpt:", 
		   "://", 
		   " ", 
		   "",
	  };

  private static final String[] testAuthoritiesTemplate = {"google.com", 
	   "www.google.com", 
	   "my.goggle.au", 
	   "google.co.uk", 
	   "52.41.119.135", 
	   "0.0.0.0", 
	   "255.255.255.100",
	   "localhost", // valid if ALLOW_LOCAL_URLS only
	   "localdomain", // valid if ALLOW_LOCAL_URLS only
	   "machine", // valid if ALLOW_LOCAL_URLS only
	   "www.google.bc", // invalid extension 
	   "www.google~.com", 
	   "www.g@ogle.com", 
	   "www.google", 
	   "255.255.255.256", 
	   "255.255.255", 
	   "", 
	   " ",		   
//   		   "www.google.com ", 
  };
  
  private static final String[] testPortsTemplate = {"", 
	   ":0", 
	   ":1", 
	   ":500", 
	   ":1000", 
	   ":65535", 
	   ":655360", 
	   ":-1", 
	   "::12", 		   
	   ":12a",		   
//   		   ":65536",  // should fail; bug in original code TODO
  };

  private static final String[] testPathsTemplate = {"/test", 
	   "/test/", 
	   "/docs/books/tutorial/index.html", 
	   "/some/p%20th", 
	   "", 
	   "//test", // valid if ALLOW_2_SLASHES only
	   "//test/test1//test2",  // valid if ALLOW_2_SLASHES only
	   "//test//test1/test 2",
	   "test", 
	   "/some/p ath", 
	   ".", 
	   "..", 
	   "/../path", 
//   		   "/", 
//   		   " /test", 
//   		   " ", 		   
  };
  
  private static final String[] testQueriesTemplate = {"?test=54", 
	   "?name=first",
	   "?id1=val1&id2=val2",
	   "?test=", 
	   "", 		   
//   		   "/?query=test", 
//   		   "??test=54", 
//   		   "?=test", 
//   		   "test=54", 
//   		   " ", 		   
//   		   "shear utter nonsense!", 	 // should fail; bug in original code TODO	   
  };
  
  private static final String[] testFragmentsTemplate = {"",
	   "#test",
	   "#",   
  };   
   
/*-------------------------------------------------------------------------*/
/* Define utility functions */   
   public String formatResult(String testedObject, boolean computed, boolean expected) {
	   String resultComputed = (computed == true) ? "valid" : "invalid";
	   String resultExpected = (expected == true) ? "valid" : "invalid";
	   String result = (computed == expected) ? "Passed" : "FAILED";
	   String out = String.format("\nTesting '%s':\n\tExpected %s - Got %s\t --> %s", testedObject, resultExpected, resultComputed, result);
	   return out;
   }
   
   /* Functions to build test ResultPair
    * Returns ResultPair array with .item from items, 
    * .valid=true before idx and =false from idx
    * */
   public ResultPair[] buildResultPair(String[] items, int idx) {
	   ResultPair[] out = new ResultPair[items.length];
	   for (int i=0; i<out.length; i++) {
		   out[i] = new ResultPair(items[i], true);
	   }	   
	   for (int i=idx; i<out.length; i++) {
		   out[i].valid = false;
	   }
	   return out;
   }
      
   /* Functions to test isValid[UrlPart] */   
   public boolean testIsValidScheme(UrlValidator val, ResultPair testScheme, PrintOption option){
	   boolean computed = val.isValidScheme(testScheme.item);
	   if (option == PrintOption.ALL || (option == PrintOption.ON_FAIL && testScheme.valid != computed)) {
		   System.out.print(formatResult(testScheme.item, computed, testScheme.valid));
	   }
	   return computed == testScheme.valid;
   }
   
   public boolean testIsValidAuthority(UrlValidator val, ResultPair testAuthorityPort, PrintOption option){ // note UrlValidator.isValidAuthority actually tests Authority+Port
	   boolean computed = val.isValidAuthority(testAuthorityPort.item);
	   if (option == PrintOption.ALL || (option == PrintOption.ON_FAIL && testAuthorityPort.valid != computed)) {
		   System.out.print(formatResult(testAuthorityPort.item, computed, testAuthorityPort.valid));
	   }
	   return computed == testAuthorityPort.valid;
   }
   
   public boolean testIsValidPath(UrlValidator val, ResultPair testPath, PrintOption option){
	   boolean computed = val.isValidPath(testPath.item);
	   if (option == PrintOption.ALL || (option == PrintOption.ON_FAIL && testPath.valid != computed)) {
		   System.out.print(formatResult(testPath.item, computed, testPath.valid));
	   }
	   return computed == testPath.valid;
   }
   
   public boolean testIsValidQuery(UrlValidator val, ResultPair testQuery, PrintOption option){
	   boolean computed = val.isValidQuery(testQuery.item);
	   if (option == PrintOption.ALL || (option == PrintOption.ON_FAIL && testQuery.valid != computed)) {
		   System.out.print(formatResult(testQuery.item, computed, testQuery.valid));
	   }
	   return computed == testQuery.valid;
   }
   
   public boolean testIsValidFragment(UrlValidator val, ResultPair testFragment, PrintOption option){
	   boolean computed = val.isValidFragment(testFragment.item);
	   if ("".equals(testFragment.item)) {
		   computed = val.isValidFragment(null);
	   }
	   if (option == PrintOption.ALL || (option == PrintOption.ON_FAIL && testFragment.valid != computed)) {
		   System.out.print(formatResult(testFragment.item, computed, testFragment.valid));
	   }
	   return computed == testFragment.valid;
   }
   

   /* Functions to run testIsValid[UrlPart] through UrlPart array */
   public void testAllSchemes(UrlValidator val, ResultPair[] testSchemes) {	   
	   System.out.printf("\n\nTesting All Schemes=========================================\n");
	   for (int i=0; i < testSchemes.length; i++) {
		   ResultPair scheme = testSchemes[i];
		   testIsValidScheme(val, scheme, PRINT);
	   }
   }

   public void testAllAuthoritiesPorts(UrlValidator val, ResultPair[] testAuthorities, ResultPair[] testPorts) {
	   System.out.printf("\n\nTesting All Authorities and Ports==========================\n");
	   for (int i1=0; i1 < testAuthorities.length; i1++) {
		   for (int i2=0; i2 < testPorts.length; i2++) {
			   StringBuffer buffer = new StringBuffer();
			   buffer.append(testAuthorities[i1].item);
			   buffer.append(testPorts[i2].item);
			   String authorityPortItem = buffer.toString();
			   boolean authorityPortValid = testAuthorities[i1].valid & testPorts[i2].valid;
			   ResultPair authorityPort = new ResultPair(authorityPortItem, authorityPortValid);
			   testIsValidAuthority(val, authorityPort, PRINT);
		   }
	   }
   }
   
   public void testAllPaths(UrlValidator val, ResultPair[] testPaths) {
	   System.out.printf("\n\nTesting All Paths==========================================\n");
	   for (int i=0; i < testPaths.length; i++) {
		   ResultPair path = testPaths[i];
		   testIsValidPath(val, path, PRINT);
	   }
   }
   
   public void testAllQueries(UrlValidator val, ResultPair[] testQueries) {
	   System.out.printf("\n\nTesting All Queries========================================\n");
	   for (int i=0; i < testQueries.length; i++) {
		   ResultPair query = testQueries[i];
		   testIsValidQuery(val, query, PRINT);
	   }
   }
   
   public void testAllFragments(UrlValidator val, ResultPair[] testFragments) {
	   System.out.printf("\n\nTesting All Fragments======================================\n");
	   for (int i=0; i < testFragments.length; i++) {
		   ResultPair fragment = testFragments[i];
		   testIsValidFragment(val, fragment, PRINT);
	   }
   }


   /* Function to test whole URL from all permutations of UrlPart */
   public void testAllUrlPerms(UrlValidator val, ResultPair[] testSchemes, ResultPair[] testAuthorities,
		   ResultPair[] testPorts, ResultPair[] testPaths, ResultPair[] testQueries, ResultPair[] testFragments) {	   
	   System.out.printf("\n\nTesting All Permutations===================================\n");	   
	   for (int i1 = 0; i1 < testSchemes.length; i1++) {
		   ResultPair scheme = testSchemes[i1];
		   for (int i2=0; i2 < testAuthorities.length; i2++) {
			   ResultPair authority = testAuthorities[i2];
			   for (int i3=0; i3 < testPorts.length; i3++) {
				   ResultPair port = testPorts[i3];
				   for (int i4=0; i4 < testPaths.length; i4++) {
					   ResultPair path = testPaths[i4];
					   for (int i5=0; i5 < testQueries.length; i5++) {
						   ResultPair query = testQueries[i5];
						   for (int i6=0; i6 < testFragments.length; i6++) {
							   ResultPair fragment = testFragments[i6];
							   StringBuffer buffer = new StringBuffer();
							   buffer.append(scheme.item);
							   buffer.append(authority.item);
							   buffer.append(port.item);
							   buffer.append(path.item);
							   buffer.append(query.item);
							   buffer.append(fragment.item);
							   String url = buffer.toString();
							   boolean expected = scheme.valid & authority.valid & port.valid & 
									   path.valid & query.valid & fragment.valid;						   
							   boolean computed = val.isValid(url);
							   if (PRINT == PrintOption.ALL || (PRINT == PrintOption.ON_FAIL && computed != expected)) {
								   System.out.print(formatResult(url, computed, expected));					   						   
							   }							   
						   }
					   }
				   }
			   }
		   }
	   }	   
   }

   
   /* Function to run testAll[UrlPart] and testUrlAllPerms on given constructor and UrlParts */
   public void testConstructor(UrlValidator val, String valName, ResultPair[] testSchemes, ResultPair[] testSchemes_wo_slashes, ResultPair[] testAuthorities,
		   ResultPair[] testPorts, ResultPair[] testPaths, ResultPair[] testQueries, ResultPair[] testFragments) {
	   System.out.printf("\n\n=======================================================================");	   
	   System.out.printf("\n=============BEGIN Unit Test - Constructor %s=============\n", valName);	   
	   testAllSchemes(val, testSchemes_wo_slashes);
	   testAllAuthoritiesPorts(val, testAuthorities, testPorts);
	   testAllPaths(val, testPaths);
	   testAllQueries(val, testQueries);
	   testAllFragments(val, testFragments);
	   testAllUrlPerms(val, testSchemes, testAuthorities, testPorts, testPaths, testQueries, testFragments);
	   System.out.printf("\n\n=============END Unit Test - Constructor %s=============", valName);	   
	   System.out.printf("\n=======================================================================\n\n");	   
   }
   
/*-------------------------------------------------------------------------*/        
/*-------------------------------------------------------------------------*/   
/* Unit test case 1: Default constructor */
   @Test
   public void unitTest1() {
	   UrlValidator val = new UrlValidator();  
	   String valName = "Default";
	   ResultPair[] testSchemes = buildResultPair(testSchemesTemplate, 3);
	   ResultPair[] testSchemes_wo_slashes = buildResultPair(testSchemesTemplate_wo_slashes, 3);
	   ResultPair[] testAuthorities = buildResultPair(testAuthoritiesTemplate, 7);
	   ResultPair[] testPorts = buildResultPair(testPortsTemplate, 6);
	   ResultPair[] testPaths = buildResultPair(testPathsTemplate, 5);
	   ResultPair[] testQueries = buildResultPair(testQueriesTemplate, 5);
	   ResultPair[] testFragments = buildResultPair(testFragmentsTemplate, 3);
	   testConstructor(val, valName, testSchemes, testSchemes_wo_slashes, testAuthorities, 
			   testPorts, testPaths, testQueries, testFragments);	   
   }
   
/*-------------------------------------------------------------------------*/   
/* Unit test case 2: ALLOW_ALL_SCHEMES */
  @Test
  public void unitTest2() {
	   UrlValidator val = new UrlValidator(UrlValidator.ALLOW_ALL_SCHEMES);  
	   String valName = "ALLOW_ALL_SCHEMES";
	   ResultPair[] testSchemes = buildResultPair(testSchemesTemplate, 6);
	   ResultPair[] testSchemes_wo_slashes = buildResultPair(testSchemesTemplate_wo_slashes, 6);
	   ResultPair[] testAuthorities = buildResultPair(testAuthoritiesTemplate, 7);
	   ResultPair[] testPorts = buildResultPair(testPortsTemplate, 6);
	   ResultPair[] testPaths = buildResultPair(testPathsTemplate, 5);
	   ResultPair[] testQueries = buildResultPair(testQueriesTemplate, 5);
	   ResultPair[] testFragments = buildResultPair(testFragmentsTemplate, 3);
	   testConstructor(val, valName, testSchemes, testSchemes_wo_slashes, testAuthorities, 
			   testPorts, testPaths, testQueries, testFragments);	   
   }
   

/*-------------------------------------------------------------------------*/   
/* Unit test case 3: NO_FRAGMENTS */
  @Test
  public void unitTest3() {
   	   UrlValidator val = new UrlValidator(UrlValidator.NO_FRAGMENTS);  
   	   String valName = "NO_FRAGMENTS";
	   ResultPair[] testSchemes = buildResultPair(testSchemesTemplate, 3);
	   ResultPair[] testSchemes_wo_slashes = buildResultPair(testSchemesTemplate_wo_slashes, 3);
	   ResultPair[] testAuthorities = buildResultPair(testAuthoritiesTemplate, 7);
	   ResultPair[] testPorts = buildResultPair(testPortsTemplate, 6);
	   ResultPair[] testPaths = buildResultPair(testPathsTemplate, 5);
	   ResultPair[] testQueries = buildResultPair(testQueriesTemplate, 5);
	   ResultPair[] testFragments = buildResultPair(testFragmentsTemplate, 1);
	   testConstructor(val, valName, testSchemes, testSchemes_wo_slashes, testAuthorities, 
			   testPorts, testPaths, testQueries, testFragments);
  }

/*-------------------------------------------------------------------------*/   
/* Unit test case 4: ALLOW_LOCAL_URLS */
  @Test
  public void unitTest4() {
   	   UrlValidator val = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);  
   	   String valName = "ALLOW_LOCAL_URLS";
	   ResultPair[] testSchemes = buildResultPair(testSchemesTemplate, 3);
	   ResultPair[] testSchemes_wo_slashes = buildResultPair(testSchemesTemplate_wo_slashes, 3);
	   ResultPair[] testAuthorities = buildResultPair(testAuthoritiesTemplate, 10);
	   ResultPair[] testPorts = buildResultPair(testPortsTemplate, 6);
	   ResultPair[] testPaths = buildResultPair(testPathsTemplate, 5);
	   ResultPair[] testQueries = buildResultPair(testQueriesTemplate, 5);
	   ResultPair[] testFragments = buildResultPair(testFragmentsTemplate, 3);
	   testConstructor(val, valName, testSchemes, testSchemes_wo_slashes, testAuthorities, 
			   testPorts, testPaths, testQueries, testFragments);	
  }  
  // http://www.google/ = valid --> Bug in original code? TODO 

  
/*-------------------------------------------------------------------------*/   
/* Unit test case 5: ALLOW_2_SLASHES */
  @Test
  public void unitTest5() {
   	   UrlValidator val = new UrlValidator(UrlValidator.ALLOW_2_SLASHES);  
   	   String valName = "ALLOW_2_SLASHES";
	   ResultPair[] testSchemes = buildResultPair(testSchemesTemplate, 3);
	   ResultPair[] testSchemes_wo_slashes = buildResultPair(testSchemesTemplate_wo_slashes, 3);
	   ResultPair[] testAuthorities = buildResultPair(testAuthoritiesTemplate, 7);
	   ResultPair[] testPorts = buildResultPair(testPortsTemplate, 6);
	   ResultPair[] testPaths = buildResultPair(testPathsTemplate, 7);
	   ResultPair[] testQueries = buildResultPair(testQueriesTemplate, 5);
	   ResultPair[] testFragments = buildResultPair(testFragmentsTemplate, 3);
	   testConstructor(val, valName, testSchemes, testSchemes_wo_slashes, testAuthorities, 
			   testPorts, testPaths, testQueries, testFragments);	
  }  

  
/*-------------------------------------------------------------------------*/   
/* Unit test case 6: schemes not null */
  @Test
  public void unitTest6() {
   	   String[] validSchemes = {"http", "https", "ftp", "HtTpS"};
	   UrlValidator val = new UrlValidator(validSchemes);  
   	   String valName = "with schemes != NULL";
	   ResultPair[] testSchemes = buildResultPair(testSchemesTemplate, 4);
	   ResultPair[] testSchemes_wo_slashes = buildResultPair(testSchemesTemplate_wo_slashes, 4);
	   ResultPair[] testAuthorities = buildResultPair(testAuthoritiesTemplate, 7);
	   ResultPair[] testPorts = buildResultPair(testPortsTemplate, 6);
	   ResultPair[] testPaths = buildResultPair(testPathsTemplate, 5);
	   ResultPair[] testQueries = buildResultPair(testQueriesTemplate, 5);
	   ResultPair[] testFragments = buildResultPair(testFragmentsTemplate, 3);
	   testConstructor(val, valName, testSchemes, testSchemes_wo_slashes, testAuthorities, 
			   testPorts, testPaths, testQueries, testFragments);	
  } 
  

/*-------------------------------------------------------------------------*/       
/*-------------------------------------------------------------------------*/   
/* Manual test */
  @Test 
  public void manualTest() {
	   UrlValidator val = new UrlValidator();
	   String[] validUrls = {"http://www.amazon.com",
			   "http://imperialadvisors.com",
			   "https://imperialadvisors.com",
			   "https://imperialadvisors.com/S&P500/S&P500%20Daily/S&P500daily.php",
			   "http://www.harefieldmanor.co.uk/",
			   "ftp://www.random.website.us:80/random/path?query=test",
	   };
	   String[] invalidUrls = {"http//imperialadvisors.com",
			   "http:/imperialadvisors.co",
			   "ttp://imperialadvisors.com",
			   "htt://imperialadvisors.com",
			   "https://localhost:80",
			   "ftp://mywebsite.com:80myport",
			   "https://www.mywebsite.vn/path1//path2?query=test"
	   };
	   
	   System.out.printf("\n=======================================================================");	   
	   System.out.printf("\n==============BEGIN Manual Test========================================\n");	   	   
	   
	   System.out.printf("\n\nTesting Valid URLs==========================================\n");
	   for (int i=0; i<validUrls.length; i++) {
		   String url = validUrls[i];
		   boolean computed = val.isValid(url);
		   if (PRINT == PrintOption.ALL || (PRINT == PrintOption.ON_FAIL && computed != true)) {
			   System.out.print(formatResult(url, computed, true));					   						   
		   }
	   }
	   
	   System.out.printf("\n\nTesting Invalid URLs==========================================\n");
	   for (int i=0; i<invalidUrls.length; i++) {
		   String url = invalidUrls[i];
		   boolean computed = val.isValid(url);
		   if (PRINT == PrintOption.ALL || (PRINT == PrintOption.ON_FAIL && computed != false)) {
			   System.out.print(formatResult(url, computed, false));					   						   
		   }
	   }
	   
	   System.out.printf("\n\n==============END Manual Test==========================================");	   
	   System.out.printf("\n=======================================================================\n\n");	   
	   // BUG_1: For the .co.uk, we could bring up the missing countries after Italy or the fact that .co.uk is not in the set.   
   }



/*-------------------------------------------------------------------------*/     
/*-------------------------------------------------------------------------*/   
/* Input partitioning test */
  
  // helper functions for testing validity of parts
  public void checkScheme(int numValid, String[] testURLscheme, UrlValidator urlVal, PrintOption option) {
 		// should validate
	    for (int i = 0; i < numValid; i++) {
			  String curURL = testURLscheme[i];
			  boolean expected = true;
			  boolean actual = urlVal.isValid(curURL);
			  if (PRINT == PrintOption.ALL || (PRINT == PrintOption.ON_FAIL && actual != expected)) {
				   System.out.print(formatResult(curURL, actual, expected));					   						   
			  }
	    }
	    
	    // should not validate
	    for (int i = numValid; i < testURLscheme.length; i++) {
			  String curURL = testURLscheme[i];
			  boolean expected = false;
			  boolean actual = urlVal.isValid(curURL);
			  if (PRINT == PrintOption.ALL || (PRINT == PrintOption.ON_FAIL && actual != expected)) {
				   System.out.print(formatResult(curURL, actual, expected));					   						   
			  }
	    }
 	}
  
  public void checkAuth(int numValid, String[] testURLauth, UrlValidator urlVal, PrintOption option) {
	   // should validate
	   for (int i = 0; i < numValid; i++) {
			  String curURL = testURLauth[i];
			  boolean expected = true;
			  boolean actual = urlVal.isValid(curURL);
			  if (PRINT == PrintOption.ALL || (PRINT == PrintOption.ON_FAIL && actual != expected)) {
				   System.out.print(formatResult(curURL, actual, expected));					   						   
			  }
	   }
	   
	   // should not validate
	   for (int i = numValid; i < testURLauth.length; i++) {
			  String curURL = testURLauth[i];
			  boolean expected = false;
			  boolean actual = urlVal.isValid(curURL);
			  if (PRINT == PrintOption.ALL || (PRINT == PrintOption.ON_FAIL && actual != expected)) {
				   System.out.print(formatResult(curURL, actual, expected));
			  }
	   }
  }
  
  public void checkPort(int numValid, String[] testURLport, UrlValidator urlVal, PrintOption option) {
	    // should validate
	    for (int i = 0; i < numValid; i++) {
			  String curURL = testURLport[i];
			  boolean expected = true;
			  boolean actual = urlVal.isValid(curURL);
			  if (PRINT == PrintOption.ALL || (PRINT == PrintOption.ON_FAIL && actual != expected)) {
				   System.out.print(formatResult(curURL, actual, expected));					   						   
			  }
	    }
	    
	    // should not validate
	    for (int i = numValid; i < testURLport.length; i++) {
			  String curURL = testURLport[i];
			  boolean expected = false;
			  boolean actual = urlVal.isValid(curURL);
			  if (PRINT == PrintOption.ALL || (PRINT == PrintOption.ON_FAIL && actual != expected)) {
				   System.out.print(formatResult(curURL, actual, expected));
			  }
	    }	   
  }
  
  public void checkPath(int numValid, String[] testURLpath, UrlValidator urlVal, PrintOption option) {
	   if (numValid == 9){
		   // should validate
		   for (int i = 0; i < numValid-2; i++) {
			   String curURL = testURLpath[i];
			   boolean expected = true;
			   boolean actual = urlVal.isValid(curURL);
			   if (PRINT == PrintOption.ALL || (PRINT == PrintOption.ON_FAIL && actual != expected)) {
			 	   System.out.print(formatResult(curURL, actual, expected));					   						   
			  }
		   }
		   String curURL = testURLpath[testURLpath.length-1];
		   boolean expected = true;
		   boolean actual = urlVal.isValid(curURL);
		   if (PRINT == PrintOption.ALL || (PRINT == PrintOption.ON_FAIL && actual != expected)) {
			   System.out.print(formatResult(curURL, actual, expected));					   						   
		   }
		   curURL = testURLpath[testURLpath.length-2];
		   expected = true;
		   actual = urlVal.isValid(curURL);
		   if (PRINT == PrintOption.ALL || (PRINT == PrintOption.ON_FAIL && actual != expected)) {
			   System.out.print(formatResult(curURL, actual, expected));					   						   
		   }
			
		   // should not validate
		   for (int i = numValid-2; i < testURLpath.length-2; i++) {
			   curURL = testURLpath[i];
			   expected = false;
			   actual = urlVal.isValid(curURL);
			   if (PRINT == PrintOption.ALL || (PRINT == PrintOption.ON_FAIL && actual != expected)) {
				   System.out.print(formatResult(curURL, actual, expected));
		       }
		   }	  
	   }
	   else {
		   // should validate
		   for (int i = 0; i < numValid; i++) {
			   String curURL = testURLpath[i];
			   boolean expected = true;
			   boolean actual = urlVal.isValid(curURL);
			   if (PRINT == PrintOption.ALL || (PRINT == PrintOption.ON_FAIL && actual != expected)) {
				   System.out.print(formatResult(curURL, actual, expected));					   						   
			   }
	       }
	    
		   // should not validate
		   for (int i = numValid; i < testURLpath.length; i++) {
			   String curURL = testURLpath[i];
			   boolean expected = false;
			   boolean actual = urlVal.isValid(curURL);
			   if (PRINT == PrintOption.ALL || (PRINT == PrintOption.ON_FAIL && actual != expected)) {
				   System.out.print(formatResult(curURL, actual, expected));
			   }
	       }	   
      }
  }
  
  public void checkQuery(int numValid, String[] testURLquery, UrlValidator urlVal, PrintOption option) {
	    // should validate
	    for (int i = 0; i < numValid; i++) {
			  String curURL = testURLquery[i];
			  boolean expected = true;
			  boolean actual = urlVal.isValid(curURL);
			  if (PRINT == PrintOption.ALL || (PRINT == PrintOption.ON_FAIL && actual != expected)) {
				   System.out.print(formatResult(curURL, actual, expected));					   						   
			  }
	    }
	    
	    // should not validate
	    for (int i = numValid; i < testURLquery.length; i++) {
			  String curURL = testURLquery[i];
			  boolean expected = false;
			  boolean actual = urlVal.isValid(curURL);
			  if (PRINT == PrintOption.ALL || (PRINT == PrintOption.ON_FAIL && actual != expected)) {
				   System.out.print(formatResult(curURL, actual, expected));
			  }
	    }	   
  }
  
  @Test
  public void inputPartitioningTest() {
	   	String[] testURLscheme = {"http://www.google.com", "ftp://www.google.com", "h3tp://www.google.com", ":/www.google.com" , " ://www.google.com", "www.google.com"};
	   	String[] testURLauth = {"http://www.google.au", "http://www.google.nz", "http://localhost/", "http://machine/", "http://www.google~.com", 
	   			"http://www.google", "http://1.2.3", "http:// "};
	   	String[] testURLport = {"http://www.google.com:80","http://www.google.com:289", "http://www.google.com:9966", "http://www.google.com:60000", 
	   			"http://www.google.com:", "http://www.google.com:-16", "http://www.google.com:25a", "http://www.google.com:80000"};
	   	String[] testURLpath = {"http://www.google.com/test", "http://www.google.com/", "http://www.google.com/test/test", "http://www.google.com/test/",
	   			"http://www.google.com/test(1).html", "http://www.google.com/test.html#test", "http://www.google.com/page?query#!state", 
	   			"http://www.google.com/..", "http://www.google.com/../", "http://www.google.com       /", "http://www.google.comtest", 
	   			"http://www.google.comtesttest", "http://www.google.com/test//test", "http://www.google.com//test"};
	   	String[] testURLquery = {"http://www.google.com?query=test", "http://www.google.com?query=test&hi=hello", "http://www.google.com?query=", 
	   			"http://www.google.com=test", "http://www.google.com??query=test"};
	   	
	   	// test allow all schemes
	    UrlValidator urlVal = new UrlValidator(null, null, UrlValidator.ALLOW_ALL_SCHEMES);

	    System.out.printf("\n=======================================================================");	   
		System.out.printf("\n==============BEGIN Input Partitioning Test============================\n\n");
	    
	    System.out.println("----------------------------------- \n");
	    System.out.println("TESTING: ALLOW_ALL_SCHEMES \n");
	    System.out.println("----------------------------------- \n");
	    
	    // scheme
	    System.out.println("----------SCHEME----------------------------------------------");	    
	    int numValid = 3;	    
	    checkScheme(numValid, testURLscheme, urlVal, PRINT);

	    // authority
	    System.out.println("\n\n----------AUTHORITY-------------------------------------------");	    
	    numValid = 2;	    
	    checkAuth(numValid, testURLauth, urlVal, PRINT);
	    
		// port
	    System.out.println("\n\n----------PORT------------------------------------------------");	    
	    numValid = 4;	    
	    checkPort(numValid, testURLport, urlVal, PRINT);
	    
	    // path
	    System.out.println("\n\n----------PATH------------------------------------------------");	    
	    numValid = 7;
	    checkPath(numValid, testURLpath, urlVal, PRINT);
	    
	    // query
	    System.out.println("\n\n----------QUERIES--------------------------------------------");	    
	    numValid = 3;	    
	    checkQuery(numValid, testURLquery, urlVal, PRINT);

	    
	    // test allow_2_slashes
	    UrlValidator urlVal1 = new UrlValidator(null, null, UrlValidator.ALLOW_2_SLASHES);
	    
	    System.out.println("\n\n----------------------------------- \n");
	    System.out.println("TESTING: ALLOW_2_SLASHES \n");
	    System.out.println("----------------------------------- \n");
	    
	    // scheme
	    System.out.println("----------SCHEME----------------------------------------------");	    
	    numValid = 2;	    
	    checkScheme(numValid, testURLscheme, urlVal1, PRINT);

	    // authority
	    System.out.println("\n\n----------AUTHORITY-------------------------------------------");	    
	    numValid = 2;	    
	    checkAuth(numValid, testURLauth, urlVal1, PRINT);
	    
	    // port
	    System.out.println("\n\n----------PORT------------------------------------------------");	    
	    numValid = 4;	    
	    checkPort(numValid, testURLport, urlVal1, PRINT);
	    
	    // path
	    System.out.println("\n\n----------PATH------------------------------------------------");	    
	    numValid = 9;	    
	    checkPath(numValid, testURLpath, urlVal1, PRINT);

	    // query
	    System.out.println("\n\n----------QUERIES--------------------------------------------");	    
	    numValid = 3;	    
	    checkQuery(numValid, testURLquery, urlVal1, PRINT);
	    
	    
	    // test no fragments
	    UrlValidator urlVal2 = new UrlValidator(null, null, UrlValidator.NO_FRAGMENTS);
	    
	    System.out.println("\n\n----------------------------------- \n");
	    System.out.println("TESTING: NO_FRAGMENTS \n");
	    System.out.println("----------------------------------- \n");
	    
	    // scheme
	    System.out.println("----------SCHEME----------------------------------------------");	    
	    numValid = 2;	    
	    checkScheme(numValid, testURLscheme, urlVal2, PRINT);

	    // authority
	    System.out.println("\n\n----------AUTHORITY-------------------------------------------");	    
	    numValid = 2;	    
	    checkAuth(numValid, testURLauth, urlVal2, PRINT);
	    
	    // port
	    System.out.println("\n\n----------PORT------------------------------------------------");	    
	    numValid = 4;	    
	    checkPort(numValid, testURLport, urlVal2, PRINT);
	    
	    // path
	    System.out.println("\n\n----------PATH------------------------------------------------");	    
	    numValid = 5;	    
	    checkPath(numValid, testURLpath, urlVal2, PRINT);

	    // query
	    System.out.println("\n\n----------QUERIES--------------------------------------------");	    
	    numValid = 3;	    
	    checkQuery(numValid, testURLquery, urlVal2, PRINT);

	    
	    //test allow local urls
	    UrlValidator urlVal3 = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);
	    
	    System.out.println("\n\n----------------------------------- \n");
	    System.out.println("TESTING: ALLOW_LOCAL_URLS \n");
	    System.out.println("----------------------------------- \n");

	    // scheme
	    System.out.println("----------SCHEME----------------------------------------------");	    
	    numValid = 2;	    
	    checkScheme(numValid, testURLscheme, urlVal2, PRINT);
	    
	    // authority
	    System.out.println("\n\n----------AUTHORITY-------------------------------------------");	    
	    numValid = 4;	    
	    checkAuth(numValid, testURLauth, urlVal3, PRINT);
	    
	    // port
	    System.out.println("\n\n----------PORT------------------------------------------------");	    
	    numValid = 4;	    
	    checkPort(numValid, testURLport, urlVal3, PRINT);

	    // path
	    System.out.println("\n\n----------PATH------------------------------------------------");	    
	    numValid = 7;	    
	    checkPath(numValid, testURLpath, urlVal3, PRINT);

	    // query
	    System.out.println("\n\n----------QUERIES--------------------------------------------");	    
	    numValid = 3;	    
	    checkQuery(numValid, testURLquery, urlVal3, PRINT);
	    
	    
	    // test default
	    UrlValidator urlVal4 = new UrlValidator();
	   
	    System.out.println("\n\n----------------------------------- \n");
	    System.out.println("TESTING: Default Constructor \n");
	    System.out.println("----------------------------------- \n");

	    // scheme
	    System.out.println("----------SCHEME----------------------------------------------");	    
	    numValid = 2;	    
	    checkScheme(numValid, testURLscheme, urlVal4, PRINT);

	    // authority
	    System.out.println("\n\n----------AUTHORITY-------------------------------------------");	    
	    numValid = 2;	    
	    checkAuth(numValid, testURLauth, urlVal4, PRINT);

	    // port
	    System.out.println("\n\n----------PORT------------------------------------------------");	    
	    numValid = 4;	    
	    checkPort(numValid, testURLport, urlVal4, PRINT);

	    // path
	    System.out.println("\n\n----------PATH------------------------------------------------");
	    numValid = 7;
	    checkPath(numValid, testURLpath, urlVal4, PRINT);

	    // query
	    System.out.println("\n\n----------QUERIES--------------------------------------------");	    
	    numValid = 3;	    
	    checkQuery(numValid, testURLquery, urlVal4, PRINT);
	    
	    // test schemes != NULL
	    String[] validSchemes = {"http", "https", "ftp", "h3tp"};
	    UrlValidator urlVal5 = new UrlValidator(validSchemes);
	   
	    System.out.println("\n\n----------------------------------- \n");
	    System.out.println("TESTING: Schemes != NULL \n");
	    System.out.println("----------------------------------- \n");

	    // scheme
	    System.out.println("----------SCHEME----------------------------------------------");	    
	    numValid = 3;	    
	    checkScheme(numValid, testURLscheme, urlVal5, PRINT);

	    // authority
	    System.out.println("\n\n----------AUTHORITY-------------------------------------------");	    
	    numValid = 2;	    
	    checkAuth(numValid, testURLauth, urlVal5, PRINT);

	    // port
	    System.out.println("\n\n----------PORT------------------------------------------------");	    
	    numValid = 4;	    
	    checkPort(numValid, testURLport, urlVal5, PRINT);

	    // path
	    System.out.println("\n\n----------PATH------------------------------------------------");
	    numValid = 7;
	    checkPath(numValid, testURLpath, urlVal5, PRINT);

	    // query
	    System.out.println("\n\n----------QUERIES--------------------------------------------");	    
	    numValid = 3;	    
	    checkQuery(numValid, testURLquery, urlVal5, PRINT);
	    
	    System.out.printf("\n\n==============END Input Partitioning Test==============================");	   
		System.out.printf("\n=======================================================================\n\n");	        
  }  
  
}