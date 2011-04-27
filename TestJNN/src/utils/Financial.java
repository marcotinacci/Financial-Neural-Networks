/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utils;

import org.apache.commons.math.stat.descriptive.moment.Mean;

/**
 *
 * @author marco tinacci
 */
public class Financial {
   /**
    * Exponential Moving Average
    * @param vin
    * @param timePer
    * @return vout
    */
   static public double[] EMA(double[] vin, int timePer){
      double k = 2. / (timePer + 1);
      double oneK = 1 - k;
      double vout[] = new double[vin.length];
      Mean mean = new Mean();
      vout[timePer-1] = mean.evaluate(vin, 0, timePer);

      double kvin[] = new double[vin.length];
      for(int i = timePer-1; i < vin.length; i++){
         kvin[i-timePer+1] = k * vin[i];
      }

      for(int i = 0; i < timePer-1; i++){
         vout[i] = Double.NaN;
      }

      vout[timePer-1] = kvin[0] + vout[timePer-1] * oneK;
      for(int i = timePer; i < vout.length; i++){
         vout[i] = kvin[i-timePer+1] + vout[i-1] * oneK;
      }
      return vout;
   }


   //RSI = 100 * U / (U + D) dove
   //U = media delle chiusure al rialzo di X giorni
   //D = media delle chiusure al ribasso di X giorni

   static public double[] RSI(double[] vin, int timePer){
      double[] vout = new double[vin.length];

      for(int i = 0; i < timePer-1; i++){
         vout[i] = Double.NaN;
      }



      return null;
   }

   //static public int Mean(double[] vin, int begin, int nel){
   //
   //}
}
