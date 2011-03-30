/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utils;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Markov
 */
public class NormalizeTest {

    public NormalizeTest() {
    }

   @BeforeClass
   public static void setUpClass() throws Exception {
   }

   @AfterClass
   public static void tearDownClass() throws Exception {
   }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

   /**
    * Test of normalize method, of class Normalize.
    */
   @Test
   public void testNormalize_5args() {
      System.out.println("normalize");
      double val = 10d;
      double min = 5;
      double max = 15;
      double minRange = 0.1;
      double maxRange = 0.9;
      double expResult = 0.5;
      double result = Normalize.normalize(val, min, max, minRange, maxRange);
      System.out.println(result);
      assertEquals(expResult, result, 0.00000001);
   }

   /**
    * Test of normalize method, of class Normalize.
    */
   @Test
   public void testNormalize_3args() {
      System.out.println("normalize");
      double val = 0.0;
      double min = 0.0;
      double max = 0.0;
      double expResult = 0.0;
      double result = Normalize.normalize(val, min, max);
      assertEquals(expResult, result, 0.0);
      // TODO review the generated test code and remove the default call to fail.
      fail("The test case is a prototype.");
   }

   /**
    * Test of denormalize method, of class Normalize.
    */
   @Test
   public void testDenormalize_5args() {
      System.out.println("denormalize");
      double val = 0.0;
      double min = 0.0;
      double max = 0.0;
      double minRange = 0.0;
      double maxRange = 0.0;
      double expResult = 0.0;
      double result = Normalize.denormalize(val, min, max, minRange, maxRange);
      assertEquals(expResult, result, 0.0);
      // TODO review the generated test code and remove the default call to fail.
      fail("The test case is a prototype.");
   }

   /**
    * Test of denormalize method, of class Normalize.
    */
   @Test
   public void testDenormalize_3args() {
      System.out.println("denormalize");
      double val = 0.0;
      double min = 0.0;
      double max = 0.0;
      double expResult = 0.0;
      double result = Normalize.denormalize(val, min, max);
      assertEquals(expResult, result, 0.0);
      // TODO review the generated test code and remove the default call to fail.
      fail("The test case is a prototype.");
   }

}