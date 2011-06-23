package org.rn.financialneuralnetwork.learningRules;

import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.neuroph.core.Connection;
import org.neuroph.core.Neuron;
import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.core.transfer.TransferFunction;
import org.neuroph.nnet.learning.BackPropagation;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.stochprocess.BrownianMotion;

/**
 *
 * @author Marco Tinacci
 */
public class STEBackPropagation extends BackPropagation {

   private double firstClose = 0;
   private StandardDeviation sigma = null;
   private int nDays = 0;
   private int previousEpoch = this.getCurrentIteration();
   private double mu = 0;

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
         BrownianMotion bm = getBrownianMotion(firstClose, mu, sigma.getResult());
         double delta = itoIntegral(bm, 1, nDays, 1)
                 *  outputError
                 * transferFunction.getDerivative(neuronInput);
         neuron.setError(delta);
         this.updateNeuronWeights(neuron);
         i++;
      } // for
   }

   @Override
   protected void reset() {
      super.reset();
      sigma = new StandardDeviation();
      mu = 0;
      firstClose = 0;
      nDays = 0;
      previousEpoch = this.getCurrentIteration();
   }


   @Override
   protected void learnPattern(SupervisedTrainingElement trainingElement) {
      /*
       * soft reset delle variabili che si rinnovano ogni epoca
       */
      if(previousEpoch != getCurrentIteration()){
         nDays = 0;
         sigma = new StandardDeviation();
         firstClose = trainingElement.getDesiredOutput()[0];
         previousEpoch = getCurrentIteration();
      }
      
      sigma.increment(trainingElement.getDesiredOutput()[0]);
      nDays++;
      mu = (nDays == 1)
              ? 0
              :(trainingElement.getDesiredOutput()[0] - firstClose) / (nDays-1);

      super.learnPattern(trainingElement);
   }

   private double itoIntegral(BrownianMotion bm, double delta, int times, double tau) {
      bm.setObservationTimes(delta, times);

      double path[] = bm.generatePath();
      double sum = 0;

      //for (int i = 0; i < path.length - 1; i++) {
      //   sum += path[i + 1] - path[i];
      //}
      sum = path[path.length - 1] - path[0];
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
      for (int i = 0; i < 3; i++) {
         seed[i] = (long) (Math.random() * 4294967087d);
      }
      for (int i = 0; i< 3; i++) {
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

   @Override
   protected void updateNeuronWeights(Neuron neuron) {
        // get the error for specified neuron,
        double neuronError = Math.tanh(neuron.getError());

        // tanh can be used to minimise the impact of big error values, which can cause network instability
        // suggested at https://sourceforge.net/tracker/?func=detail&atid=1107579&aid=3130561&group_id=238532
        // double neuronError = Math.tanh(neuron.getError());

        // iterate through all neuron's input connections
        for (Connection connection : neuron.getInputConnections()) {
            // get the input from current connection
            double input = connection.getInput();
            // calculate the weight change
            double deltaWeight = this.learningRate * neuronError * input;
            // update the weight change
            this.applyWeightChange(connection.getWeight(), deltaWeight);
        }
   }


}
