/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rn.financialneuralnetwork.learningRules;

import org.neuroph.core.Neuron;
import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.core.transfer.TransferFunction;
import org.neuroph.nnet.learning.BackPropagation;

/**
 *
 * @author Marco Tinacci
 */
public class TDPBackPropagation extends BackPropagation {

   private double previousOutput[] = null;
   private double previousDesiredOutput[] = null;
   private int counter = 0;
   private int previousEpoch = this.getCurrentIteration();

   private static final double DISCOUNT_RATE = 6;
   private static final double a[] = {0.5, 0.8, 1.2, 1.5};

   public int c1 = 0;
   public int c2 = 0;
   public int c3 = 0;
   public int c4 = 0;
   public int cplus;

   // TODO caso deltaT o deltaO nulli

   public TDPBackPropagation(){
      super();
   }


   private double timeDependentDirectionalProfit(int trainIndex,
           double discountRate, int n, int outputIndex){
      // calcola w(p)
      double w = 1. / (1 + Math.exp(discountRate
              - 2 * trainIndex * discountRate / n));
      // calcola f_DP
      if(trainIndex == 0){
         return w;
      }else{
         double deltaT = ((SupervisedTrainingElement)this.getTrainingSet()
                 .elementAt(trainIndex)).getDesiredOutput()[outputIndex]
                 - previousDesiredOutput[outputIndex];
         double deltaO = neuralNetwork.getOutput()[outputIndex]
                 - previousOutput[outputIndex];

         double k;
         if (deltaO>0)
                 cplus++;
         if(deltaT * deltaO > 0){
            if(Math.abs(deltaT) / previousDesiredOutput[outputIndex] > 0.01){
//            if(Math.abs(deltaT) > sigma){
               k = a[0];
               c1++;
            }else{
               k = a[1];
               c2++;
            }
         }else{
            if(Math.abs(deltaT) / previousDesiredOutput[outputIndex] <= 0.01){
            //if(Math.abs(deltaT) <= sigma){
               k = a[2];
               c3++;
            }else{
               k = a[3];
               c4++;
            }
         }
         return k * w;
         }
   }

   @Override
   protected void reset() {
      /*
       * hard reset delle variabili che si rinnovano ad ogni training set
       */
      super.reset();
      previousOutput = null;
      previousDesiredOutput = null;
      counter = 0;
      previousEpoch = this.getCurrentIteration();
   }


   @Override
   protected void learnPattern(SupervisedTrainingElement trainingElement) {
      /*
       * soft reset delle variabili che si rinnovano ogni epoca
       */
      if(previousEpoch != getCurrentIteration()){
         // reset contatore
         counter = 0;
         // aggiorna indice epoca
         previousEpoch = getCurrentIteration();
      }
      super.learnPattern(trainingElement);
      
      double[] previousInput = trainingElement.getInput();
      this.neuralNetwork.setInput(previousInput);
      this.neuralNetwork.calculate();
      previousOutput = this.neuralNetwork.getOutput();
      previousDesiredOutput = trainingElement.getDesiredOutput();

      counter++;

   }

   @Override
   protected void adjustOutputNeurons(double[] patternError) {
       int i = 0;
       for(Neuron neuron : neuralNetwork.getOutputNeurons()) {
               double outputError = patternError[i];
               if (outputError == 0) {
                       neuron.setError(0);
                       i++;
                       continue;
               }

               TransferFunction transferFunction = neuron.getTransferFunction();
               double neuronInput = neuron.getNetInput();
               // TODO
               double delta = timeDependentDirectionalProfit(counter, 
                       DISCOUNT_RATE, this.getTrainingSet().size(), i)
                       * outputError
                       * transferFunction.getDerivative(neuronInput);
               neuron.setError(delta);
               this.updateNeuronWeights(neuron);
               i++;
       } // for
   }


}
