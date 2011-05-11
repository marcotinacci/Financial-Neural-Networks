/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package testjnn;

import org.neuroph.nnet.learning.BackPropagation;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.stochprocess.BrownianMotion;

/**
 *
 * @author Marco Tinacci
 */
public class STEBackPropagation extends BackPropagation{

   private double itoIntegral(BrownianMotion bm, double delta, int times)
   {
      bm.setObservationTimes(delta, times);
      double path[] = bm.generatePath();
      double sum = 0;
      for (int i = 0; i < path.length - 1; i++) {
         sum += path[i+1] - path[i];
      }
      return bm.getSigma()*sum + (times-1)*bm.getMu();
   }

   /**
    * Metodo di generazione del moto browniano
    * @param initValue
    * @param mu tasso di crescita, drift
    * @param sigma deviazione standard nell'intervallo
    * @return moto browniano
    */
   private BrownianMotion getBrownianMotion(double initValue, double mu,
           double sigma)
   {
      long[] seed = new long[6];
      for(int i = 0; i < 3; i++)
      {
         seed[i] = (long)(Math.random() * 4294967087d);
      }
      for(int i = 0; i < 3; i++)
      {
         seed[i+3] = (long)(Math.random() * 4294944443d);
      }
      return getBrownianMotion(initValue, mu, sigma, seed);
   }

   /**
    * Metodo di generazione del moto browniano
    * @param initValue
    * @param mu tasso di crescita, drift
    * @param sigma deviazione standard nell'intervallo
    * @param seed vettore seed di 6 elementi composto dai primi 3 elementi in 
    * [0,4294967087] e i secondi 3 in [0,4294944443]
    * @return moto browniano
    */
   private BrownianMotion getBrownianMotion(double initValue, double mu, 
           double sigma, long seed[])
   {
      MRG32k3a.setPackageSeed(seed);
      MRG32k3a rs = new MRG32k3a();
      return new BrownianMotion(initValue, mu, sigma, rs);
   }
}
