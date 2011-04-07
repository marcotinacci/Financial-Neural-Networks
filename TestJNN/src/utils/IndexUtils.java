/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utils;

/**
 *
 * @author Marco Tinacci
 */
public class IndexUtils {
   static private final double ALPHA = 0.5;

   static public double movingExpMean(double[] data, int begin, int len){
      double sum = 0d;
      final double BETA = 1-ALPHA;
      for(int i = 0; i < data.length; i++){
         sum += Math.pow(BETA, i) * data[i];
      }
      return ALPHA*sum;
   }
}
