package testjnn;

import java.util.LinkedList;
import java.util.List;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.neuroph.core.Neuron;
import org.neuroph.core.transfer.TransferFunction;
import org.neuroph.nnet.learning.BackPropagation;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.stochprocess.BrownianMotion;

/**
 *
 * @author Marco Tinacci
 */
public class STEBackPropagation extends BackPropagation {

   private double firstClose;
   private StandardDeviation sigma = null;


   @Override
   protected void adjustOutputNeurons(double[] patternError) {
      int i = 0;
      for (Neuron neuron : neuralNetwork.getOutputNeurons()) {
         double outputError = patternError[i];
         if (outputError == 0) {
            neuron.setError(0);
            i++;
            continue;
         }

         TransferFunction transferFunction = neuron.getTransferFunction();
         double neuronInput = neuron.getNetInput();
         // TODO
         double mu = (currentValue - firstClose) / numerogiorni;
         sigma.increment(currentvalue);
         BrownianMotion bm = getBrownianMotion(firstClose, mu, sigma.getResult());
         double delta = itoIntegral(neuron, maxError, i, maxError)
                 * outputError
                 * transferFunction.getDerivative(neuronInput);
         neuron.setError(delta);
         this.updateNeuronWeights(neuron);
         i++;
      } // for
   }

   private double itoIntegral(BrownianMotion bm, double delta, int times, double tau) {
      bm.setObservationTimes(delta, times);


      double path[] = bm.generatePath();


      double sum = 0;


      for (int i = 0; i
              < path.length - 1; i++) {
         sum += path[i + 1] - path[i];


      }
      return (1. / tau) * Math.exp(bm.getSigma() * sum
              + (times - 1) * Math.abs(bm.getMu()));


   }

   /**
    * Metodo di generazione del moto browniano
    * @param initValue
    * @param mu tasso di crescita, drift
    * @param sigma deviazione standard nell'intervallo
    * @return moto browniano
    */
   private BrownianMotion getBrownianMotion(double initValue, double mu,
           double sigma) {
      long[] seed = new long[6];


      for (int i = 0; i
              < 3; i++) {
         seed[i] = (long) (Math.random() * 4294967087d);


      }
      for (int i = 0; i
              < 3; i++) {
         seed[i + 3] = (long) (Math.random() * 4294944443d);


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
           double sigma, long seed[]) {
      MRG32k3a.setPackageSeed(seed);
      MRG32k3a rs = new MRG32k3a();


      return new BrownianMotion(initValue, mu, sigma, rs);

   }
}
