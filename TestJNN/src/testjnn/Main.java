/*
 * 
 */
package testjnn;

import beans.DayBean;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.core.learning.TrainingElement;
import org.neuroph.core.learning.TrainingSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.LMS;
import org.neuroph.nnet.learning.MomentumBackpropagation;
import utils.CSVReader;
import utils.Normalize;

/**
 *
 * @author Marco Tinacci
 */
public class Main {
   static private final int N_LEARN = 1500;
   static private final int N_TEST = 350;

   // dimensioni rete
   static private final int NNET_INPUT_LAYER = 30;
   static private final int NNET_HIDDEN_LAYER = 30;
   static private final int NNET_OUTPUT_LAYER = 1;

   static private final double MIN_RANGE = 0.1;
   static private final double MAX_RANGE = 0.9;

   /**
    * @param args the command line arguments
    */
   public static void main(String[] args) throws Exception {
      Main main = new Main();
      List<DayBean> list = CSVReader.readAll("/Users/Markov/Desktop/MIB.csv");
      //System.out.println(list.toString().replace("], [", "]\n["));
      main.testNN(list);

   }

   public void testNN(List<DayBean> days) {
      // creazione della rete neurale
      NeuralNetwork nnet = new MultiLayerPerceptron(NNET_INPUT_LAYER,
              NNET_HIDDEN_LAYER, NNET_OUTPUT_LAYER);
      //nnet.setLearningRule(new MomentumBackpropagation());
      nnet.setLearningRule(new TDPBackPropagation());
      //((MomentumBackpropagation) nnet.getLearningRule()).setMomentum(0.9);
      ((LMS) nnet.getLearningRule()).setLearningRate(0.25);
      ((LMS) nnet.getLearningRule()).setMaxError(0.0001);
      ((LMS) nnet.getLearningRule()).setMaxIterations(1000);

      double max = 0;
      double min = Double.MAX_VALUE;
      for(int i = 0; i < N_LEARN; i++){
         double x = days.get(i).getClose();
         if(x < min){
            min = x;
         }
         if(x > max){
            max = x;
         }
      }

      
      // creazione dell'insieme di dati di addestramento
      TrainingSet trainSet = new TrainingSet();
      // ciclo su training sets
      for(int i = 0; i < N_LEARN; i++){
         double v[] = new double[NNET_INPUT_LAYER];
         // ciclo sul singolo input
         for(int j = 0; j < NNET_INPUT_LAYER; j++){
            v[j] = Normalize.normalize(days.get(i+j).getClose(), min, max,
                    MIN_RANGE, MAX_RANGE);
         }
         // aggiungi l'input e l'output
         trainSet.addElement(new SupervisedTrainingElement(
            v, new double[]{
                  Normalize.normalize(days.get(i+NNET_INPUT_LAYER).getClose(), 
                          min, max, MIN_RANGE, MAX_RANGE)})
         );
      }

      // addestramento della rete
      nnet.learnInSameThread(trainSet);

      // test errore
      TrainingSet testSet = new TrainingSet();
      for(int i = 0; i < N_TEST; i++){
         double v[] = new double[NNET_INPUT_LAYER];
         for(int j = 0; j < NNET_INPUT_LAYER; j++){
            v[j] = Normalize.normalize(days.get(N_LEARN+i+j).getClose(), min,
                    max, MIN_RANGE, MAX_RANGE);
         }
         testSet.addElement(new TrainingElement(v));
      }

      double errors[] = new double[testSet.trainingElements().size()];
      for(int i = 0; i < testSet.trainingElements().size(); i++){
         nnet.setInput(testSet.trainingElements().get(i).getInput());
         nnet.calculate();
         double[] networkOutput = nnet.getOutput();
         //System.out.println("Input: " + Arrays.toString(
           //      testSet.trainingElements().get(i).getInput()));
         double v1 = days.get(N_LEARN+NNET_INPUT_LAYER+i).getClose();
         double v2 = Normalize.denormalize(networkOutput[0], min, max,
                    MIN_RANGE, MAX_RANGE);
         errors[i] = (Math.abs(v1 - v2) / v1 );
         //System.out.println("Output (" + v1
           //      + "): " + v2 + " ("+ errors[i] +")");
      }
      DescriptiveStatistics ds = new DescriptiveStatistics(errors);
      System.out.println("Errore medio: "+ ds.getMean());
      System.out.println("Varianza: "+ ds.getVariance());

      // test profitto
      double stocks = 0;
      double seed = 1000000;
      int currentSign = -1; // {-1, +1}
      int nextSign;
      double todayVal = 0;
      
      for(int i = 0; i < 20; i++){
         nnet.setInput(testSet.trainingElements().get(i).getInput());
         nnet.calculate();
         double[] networkOutput = nnet.getOutput();
         todayVal = testSet.trainingElements().get(i)
                 .getInput()[NNET_INPUT_LAYER-1];
         // TODO segno uguale a zero??
         // calcolo della previsione
         nextSign = (int)Math.signum(networkOutput[0] - todayVal);
         // decisione
         if(currentSign == -1 && nextSign == 1){
            stocks = seed / Normalize.denormalize(todayVal, min, max,
                    MIN_RANGE, MAX_RANGE);
            seed = 0;
            currentSign = 1;
         }else if(currentSign == 1 && nextSign == -1){
            seed = stocks * Normalize.denormalize(todayVal, min, max,
                    MIN_RANGE, MAX_RANGE);
            stocks = 0;
            currentSign = -1;
         }
      }

      System.out.println("Ricavo finale: " + (seed + stocks *
              Normalize.denormalize(todayVal, min, max, MIN_RANGE, MAX_RANGE)));

   }
}
