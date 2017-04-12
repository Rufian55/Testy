/*
 * CS362 Winter 2017
 * Final Project Part B
 * Team members: Linh Vu (vuli), Jessica Spokoyny (spokoynj), Chris Kearns (kearnsc)
 * Last updated: 15 Mar 2017
 */


import junit.framework.TestCase;
import static org.junit.Assert.*;
import java.util.regex.Matcher;

/**
 * Performs Validation Test for url validations.
 *
 * @version $Revision: 1128446 $ $Date: 2011-05-27 13:29:27 -0700 (Fri, 27 May 2011) $
 */

public class UrlValidatorTest extends TestCase {
   private enum PrintOption {
	   ALL, ON_FAIL, NONE
   }
   private static final PrintOption PRINT = PrintOption.ON_FAIL;
   
   UrlValidator val = new UrlValidator();   

   public UrlValidatorTest(String testName) {
	      super(testName);
   }
   
   
   /* Here only test case UrlValidator(null, null, null) 
    * Consider refactor into function to test different constructors */
   
/*-------------------------------------------------------------------------*/
/* Define test inputs */
   ResultPair[] testSchemes = {new ResultPair("http://", true),
		   new ResultPair("https://", true),
		   new ResultPair("ftp://", true), 
		   new ResultPair("http1://", false),
		   new ResultPair("1ftp://", false),
		   new ResultPair("https:/", false),
		   new ResultPair("fpt:", false),
		   new ResultPair("HtTpS://", false),
		   new ResultPair("://", false),
		   new ResultPair(" ", false),
		   new ResultPair("", false)
   };

   ResultPair[] testSchemes_wo_slashes = {new ResultPair("http", true),
		   new ResultPair("https", true),
		   new ResultPair("ftp", true), 
		   new ResultPair("http1", false),
		   new ResultPair("1ftp", false),
		   new ResultPair("https:/", false),
		   new ResultPair("fpt:", false),
		   new ResultPair("HtTpS", false),
		   new ResultPair(" ", false),
		   new ResultPair("", false)
   };

   
   ResultPair[] testAuthorities = {new ResultPair("google.com", true),
		   new ResultPair("www.google.com", true),
		   new ResultPair("my.goggle.au", true),
//		   new ResultPair("www.google.com ", false),
		   new ResultPair("www.google~.com", false),
		   new ResultPair("www.g@ogle.com", false),
		   new ResultPair("www.google", false),
		   new ResultPair("52.41.119.135", true),
		   new ResultPair("0.0.0.0", true),
		   new ResultPair("255.255.255.100", true),
		   new ResultPair("255.255.255.256", false),
		   new ResultPair("", false),
		   new ResultPair(" ", false)		   
   };
   
   ResultPair[] testPorts = {new ResultPair(":1", true),
		   new ResultPair(":500", true),
		   new ResultPair(":1000", true),
		   new ResultPair(":65535", true),
		   new ResultPair(":65536", false), // should fail; bug in original code TODO
		   new ResultPair(":655360", false),
		   new ResultPair(":0", true),
		   new ResultPair(":-1", false),
		   new ResultPair("", true),
		   new ResultPair("::12", false),		   
		   new ResultPair(":12a", false)		   
   };

   ResultPair[] testPaths = {new ResultPair("/test", true),
		   new ResultPair("/test/", true),
		   new ResultPair("//test", false),
		   new ResultPair("test", false),
//		   new ResultPair(" /test", false),
		   new ResultPair("/docs/books/tutorial/index.html", true),
		   new ResultPair("/some/p%20th", true),
		   new ResultPair("/some/p ath", false),
		   new ResultPair("", true),
//		   new ResultPair("/", true),
		   new ResultPair(".", false),
		   new ResultPair("..", false),
		   new ResultPair("/../path", false),
//		   new ResultPair(" ", false),		   
   };
   
   // from UrlValidator.java, looks like query regex will match anything??? TODO
   ResultPair[] testQueries = {new ResultPair("?test=54", true),
		   new ResultPair("?name=first", true),
		   new ResultPair("", true),
		   new ResultPair("?test=", true),
		   new ResultPair("", true),		   
//		   new ResultPair("/?query=test", false),
//		   new ResultPair("??test=54", false),
//		   new ResultPair("?=test", false),
//		   new ResultPair("test=54", false),
//		   new ResultPair(" ", false),		   
//		   new ResultPair("shear utter nonsense!", false),		   
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
   
   
/*-------------------------------------------------------------------------*/
/* Define functions to test individual isValid[URLpart] functions */   
   public boolean testIsValidScheme(UrlValidator val, ResultPair testScheme, PrintOption option){
	   boolean computed = val.isValidScheme(testScheme.item);
	   if (option == PrintOption.ALL || (option == PrintOption.ON_FAIL && testScheme.valid != computed)) {
		   System.out.print(formatResult(testScheme.item, computed, testScheme.valid));
	   }
	   return computed == testScheme.valid;
   }
   
   public boolean testIsValidAuthorityPort(UrlValidator val, ResultPair testAuthorityPort, PrintOption option){
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
   

/*-------------------------------------------------------------------------*/   
/* Run testIsValid[URLpart] through all test URL components */
   public void testAllSchemes() { // won't work on its own, probably due to weird scheme regex TODO
	   System.out.printf("\n\n==========Testing All Schemes:====================================\n");
	   for (int i=0; i < testSchemes_wo_slashes.length; i++) {
		   ResultPair scheme = testSchemes_wo_slashes[i];
//		   assertTrue(testIsValidScheme(val, scheme, PRINT));
		   testIsValidScheme(val, scheme, PRINT);
	   }
   }

   public void testAllAuthoritiesPorts() {
	   System.out.printf("\n\n==========Testing All Authorities and Ports:====================================\n");
	   for (int i1=0; i1 < testAuthorities.length; i1++) {
		   for (int i2=0; i2 < testPorts.length; i2++) {
			   StringBuffer buffer = new StringBuffer();
			   buffer.append(testAuthorities[i1].item);
			   buffer.append(testPorts[i2].item);
			   String authorityPortItem = buffer.toString();
			   boolean authorityPortValid = testAuthorities[i1].valid & testPorts[i2].valid;
			   ResultPair authorityPort = new ResultPair(authorityPortItem, authorityPortValid);
//			   assertTrue(testIsValidAuthorityPort(val, authorityPort, PRINT));
			   testIsValidAuthorityPort(val, authorityPort, PRINT);
		   }
	   }
   }
   
   public void testAllPaths() {
	   System.out.printf("\n\n==========Testing All Paths:====================================\n");
	   for (int i=0; i < testPaths.length; i++) {
		   ResultPair path = testPaths[i];
//		   assertTrue(testIsValidPath(val, path, PRINT));
		   testIsValidPath(val, path, PRINT);
	   }
   }
   
   public void testAllQueries() {
	   System.out.printf("\n\n==========Testing All Queries:====================================\n");
	   for (int i=0; i < testQueries.length; i++) {
		   ResultPair query = testQueries[i];
//		   assertTrue(testIsValidQuery(val, query, PRINT));
		   testIsValidQuery(val, query, PRINT);
	   }
   }


/*-------------------------------------------------------------------------*/   
/* Run through all permutations of test components */
   public void testCombos() {	   
	   System.out.printf("\n\n==========Testing All Permutations:====================================\n");	   
	   for (int i1 = 0; i1 < testSchemes.length; i1++) {
		   ResultPair scheme = testSchemes[i1];
		   for (int i2=0; i2 < testAuthorities.length; i2++) {
			   ResultPair authority = testAuthorities[i2];
			   for (int i3=0; i3 < testPaths.length; i3++) {
				   ResultPair path = testPaths[i3];
				   for (int i4=0; i4 < testQueries.length; i4++) {
					   ResultPair query = testQueries[i4];
					   StringBuffer buffer = new StringBuffer();
					   buffer.append(scheme.item);
					   buffer.append(authority.item);
					   buffer.append(path.item);
					   buffer.append(query.item);
					   String url = buffer.toString();

					   boolean expected = scheme.valid & authority.valid & path.valid & query.valid;						   
					   boolean computed = val.isValid(url);
					   
					   if (PRINT == PrintOption.ALL || (PRINT == PrintOption.ON_FAIL && computed != expected)) {
						   System.out.print(formatResult(url, computed, expected));					   						   
					   }
//					   assertTrue(computed==expected);
				   }
			   }
		   }
	   }	   
   }


   
/*-------------------------------------------------------------------------*/   
/* Manual test */
/* Linh's notes: I think you want to use the default constructor here. Setting 
 * ALLOW_ALL_SCHEMES passes everything that meets SCHEME_REGEX as valid
 * including the three "Reports true???" cases
 * */
   public void testManualTest() {
	   UrlValidator urlVal = new UrlValidator();
//	   UrlValidator urlVal = new UrlValidator(null, null, UrlValidator.ALLOW_ALL_SCHEMES);
	   System.out.println("\n\n==========Testing Manual Tests:=============================\n");
	   System.out.println("Below should return true:");
	   System.out.println(urlVal.isValid("http://www.amazon.com"));
	   System.out.println(urlVal.isValid("https://imperialadvisors.com/S&P500/S&P500%20Daily/S&P500daily.php"));
	   System.out.println(urlVal.isValid("http://www.ChrisKearnsEatsWorms.com/"));
	   System.out.println(urlVal.isValid("http://imperialadvisors.com"));
	   System.out.println(urlVal.isValid("https://www.imperialadvisors.com"));
	   System.out.println(urlVal.isValid("https://imperialadvisors.com"));
	   System.out.println("Below should return false:");
	   System.out.println(urlVal.isValid("http//imperialadvisors.com"));
	   System.out.println(urlVal.isValid("http:/imperialadvisors.co"));
	   System.out.println(urlVal.isValid("http://www.harefieldmanor.co.uk/"));	// This is actually a valid url for United Kingdom.
	   System.out.println(urlVal.isValid("ttp://imperialadvisors.com"));		// Reports true???
	   System.out.println(urlVal.isValid("htt://imperialadvisors.com"));		// Reports true???

	   // BUG_1: For the .co.uk, we could bring up the missing countries after Italy or the fact that .co.uk is not in the set.
   
   }
   
   public void testIsValid() {
	   for(int i = 0; i < 10000; i++) {

		   
	   }
   }
   
   public void testAnyOtherUnitTest() {
	   
   }
}

