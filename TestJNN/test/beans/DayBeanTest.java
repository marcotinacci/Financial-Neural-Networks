/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package beans;

import java.util.Date;
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
public class DayBeanTest {

    public DayBeanTest() {
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
    * Test of getDate method, of class DayBean.
    */
   @Test
   public void testGetDate() {
      System.out.println("getDate");
      DayBean instance = new DayBean();
      Date expResult = null;
      Date result = instance.getDate();
      assertEquals(expResult, result);
   }

   /**
    * Test of setDate method, of class DayBean.
    */
   @Test
   public void testSetDate() {
      System.out.println("setDate");
      Date date = new Date();
      DayBean instance = new DayBean();
      instance.setDate(date);
      assertEquals(date, instance.getDate());
   }

}