/*
 * 
 */
package testjnn;

import beans.DayBean;
import utils.Pair;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.core.learning.TrainingSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.LMS;
import utils.CSVHandler;
import utils.Normalize;
import utils.Financial;

/**
 *
 * @author Marco Tinacci
 */
public class Main {
   static private final int N_LEARN = 1000;
   static private final int N_TEST = 350;

   // dimensioni rete
   static private final int NNET_INPUT_LAYER = 5;
   static private final int NNET_HIDDEN_LAYER = 10;
   static private final int NNET_OUTPUT_LAYER = 1;

   // range di normalizzazione
   static private final double MIN_RANGE = 0.1;
   static private final double MAX_RANGE = 0.9;

   // nome dell'indice da utilizzare (nella cartella "data/")
   static private final String INDEX_FILE = "NASDAQ100";

   static private final int EMA_STEP = 5;

   /**
    * @param args the command line arguments
    */
   public static void main(String[] args) throws Exception {
      Main main = new Main();
      List<DayBean> list = CSVHandler.readAll("data/" + INDEX_FILE + ".csv");
      //System.out.println(list.toString().replace("], [", "]\n["));
      main.testNN(list);
      
      /*
       * corpo procedura generazione moti browniani
      long[] seed = new long[6];
      for(int i = 0; i < 3; i++){
         seed[i] = (long)(Math.random() * 4294967087d);
      }
      for(int i = 0; i < 3; i++){
         seed[i+3] = (long)(Math.random() * 4294944443d);
      }
      MRG32k3a.setPackageSeed(seed);

      MRG32k3a rs = new MRG32k3a();
      BrownianMotion bm = new BrownianMotion(1, 1, 1, rs);
      bm.setObservationTimes(1, 10);
      System.out.println("moto browniano: " + Arrays.toString(bm.generatePath()));
      */

   }

   public void testNN(List<DayBean> days) throws FileNotFoundException, IOException {
      // creazione della rete neurale
      NeuralNetwork nnet = new MultiLayerPerceptron(NNET_INPUT_LAYER,
              NNET_HIDDEN_LAYER, NNET_OUTPUT_LAYER);
      //nnet.setLearningRule(new MomentumBackpropagation());
      nnet.setLearningRule(new TDPBackPropagation());
      //((MomentumBackpropagation) nnet.getLearningRule()).setMomentum(0);
      ((LMS) nnet.getLearningRule()).setLearningRate(0.1);
      ((LMS) nnet.getLearningRule()).setMaxError(0.0001);
      ((LMS) nnet.getLearningRule()).setMaxIterations(1000);

      Pair<Double, Double> minmax = getMinMax(days);
      double min = minmax.fst;
      double max = minmax.snd;
      
      // creazione dell'insieme di dati di addestramento
      TrainingSet trainSet = EMASet(days, 0, N_LEARN, min, max);

      // addestramento della rete
      nnet.learnInSameThread(trainSet);

      // test errore
      double v_days[] = new double[N_TEST];
      double v_values[] = new double[N_TEST];
      double v_targets[] = new double[N_TEST];

      double errors[] = new double[N_TEST];
      double targets[] = new double[N_TEST];
      TrainingSet testSet = EMASet(days, N_LEARN, N_TEST, min, max);
      for(int i = 0; i < testSet.size(); i++){
         nnet.setInput(testSet.elementAt(i).getInput());
         nnet.calculate();
         double[] networkOutput = nnet.getOutput();
         double v1 = days.get(N_LEARN+(NNET_INPUT_LAYER-1)*EMA_STEP+i).getClose();
         double v2 = Normalize.denormalize(networkOutput[0], min, max,
                    MIN_RANGE, MAX_RANGE);
         errors[i] = (Math.abs(v1 - v2) / v1);
         targets[i] = v1;
         
         v_days[i] = N_LEARN+(NNET_INPUT_LAYER-1)*EMA_STEP+i;
         v_values[i] = 0;
         v_targets[i] = v2;
      }

      DescriptiveStatistics ds = new DescriptiveStatistics(errors);
      System.out.println("Errore medio: "+ ds.getMean());
      ds = new DescriptiveStatistics(targets);
      double stdDeviation = ds.getStandardDeviation();
      System.out.println("Deviazione standard: "+ stdDeviation);

      double v[][] = new double[3][];
      v[0] = v_days;
      v[1] = v_values;
      v[2] = v_targets;
      CSVHandler.writeArray(v, "data/result_" + INDEX_FILE + ".csv");

      /*
      // test profitto
      double stocks = 0;
      double seed = 1000000;
      int currentSign = -1; // {-1, +1}
      int nextSign;
      double todayVal = 0;

      double v_days[] = new double[260];
      double v_values[] = new double[260];
      double v_targets[] = new double[260];

      for(int i = 0; i < 260; i++){
         
//         nnet.setInput(testSet.trainingElements().get(i).getInput());
         nnet.calculate();
         double[] networkOutput = nnet.getOutput();
//         todayVal = testSet.trainingElements().get(i)
//                 .getInput()[NNET_INPUT_LAYER-1];
         // TODO segno uguale a zero??
         // calcolo della previsione
         double diff = Normalize.denormalize(networkOutput[0], min, max,
                 MIN_RANGE, MAX_RANGE) - Normalize.denormalize(todayVal, min,
                 max, MIN_RANGE, MAX_RANGE);
//         if(Math.abs(diff)/Normalize.denormalize(todayVal, min,
  //               max, MIN_RANGE, MAX_RANGE) > 0.01){
            nextSign = (int)Math.signum(diff);
            // decisione
            if(currentSign == -1 && nextSign == 1){
               stocks = seed / Normalize.denormalize(todayVal, min, max,
                       MIN_RANGE, MAX_RANGE);
               //System.out.println("giorno: "+ (N_LEARN+NNET_INPUT_LAYER+i) +
               //        " compra "+ stocks +" azioni per "+ seed + "$.");

               seed = 0;
               currentSign = 1;
            }else if(currentSign == 1 && nextSign == -1){
               seed = stocks * Normalize.denormalize(todayVal, min, max,
                     MIN_RANGE, MAX_RANGE);
               System.out.println("giorno: "+ (N_LEARN+NNET_INPUT_LAYER+i) +
                       " vendi "+ stocks +" azioni per "+ seed + "$.");
               stocks = 0;
               currentSign = -1;
            }
         v_days[i] = N_LEARN + NNET_INPUT_LAYER + i;
         v_values[i] = seed;
         v_targets[i] = Normalize.denormalize(networkOutput[0], min, max, MIN_RANGE, MAX_RANGE);
//         }
      }

      double v[][] = new double[3][];
      v[0] = v_days;
      v[1] = v_values;
      v[2] = v_targets;
      CSVHandler.writeArray(v, "data/result_" + INDEX_FILE + ".csv");

      System.out.println("Ricavo finale: " + (seed + stocks *
              Normalize.denormalize(todayVal, min, max, MIN_RANGE, MAX_RANGE)));
//      System.out.println("c1: "+((TDPBackPropagation)nnet.getLearningRule()).c1);
//      System.out.println("c2: "+((TDPBackPropagation)nnet.getLearningRule()).c2);
//      System.out.println("c3: "+((TDPBackPropagation)nnet.getLearningRule()).c3);
//      System.out.println("c4: "+((TDPBackPropagation)nnet.getLearningRule()).c4);
//      System.out.println("cplus: "+((TDPBackPropagation)nnet.getLearningRule()).cplus);
 */
   }

   private TrainingSet standardSet(List<DayBean> days, double min, double max){
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
      return trainSet;
   }

   private TrainingSet EMASet(List<DayBean> days, int begin, int nel,
           double min, double max){
      TrainingSet trainSet = new TrainingSet();
      // crea il vettore di calori di chiusura
      double closeVals[] = new double[nel];
      for(int i = 0; i < nel; i++){
         closeVals[i] = Normalize.normalize(days.get(begin+i).getClose(), min, 
                 max, MIN_RANGE, MAX_RANGE);
      }

      double valsEMA[][] = new double[NNET_INPUT_LAYER-1][];
      for(int i = 0; i < NNET_INPUT_LAYER-1; i++){
         valsEMA[i] = Financial.EMA(closeVals, EMA_STEP*(i+1));
      }

      for(int i = 0; i < nel - ((NNET_INPUT_LAYER-1) * EMA_STEP) ; i++){
         double v[] = new double[NNET_INPUT_LAYER];
         int idx = i + (NNET_INPUT_LAYER-1) * EMA_STEP-1;
         v[0] = closeVals[idx];
         for(int j = 1; j < NNET_INPUT_LAYER; j++){
            v[j] = valsEMA[j-1][idx];
         }

         // aggiungi l'input e l'output
         trainSet.addElement(new SupervisedTrainingElement(
            v, new double[]{closeVals[idx+1]})
         );
      }

      return trainSet;
   }
/*
   private TrainingSet EMASet(List<DayBean> days, double min, double max){
      TrainingSet trainSet = new TrainingSet();
      // crea il vettore di calori di chiusura
      double closeVals[] = new double[N_LEARN];
      for(int i = 0; i < N_LEARN; i++){
         closeVals[i] = Normalize.normalize(days.get(i).getClose(), min, max,
                 MIN_RANGE, MAX_RANGE);
      }

      double valsEMA[][] = new double[NNET_INPUT_LAYER-1][];
      for(int i = 0; i < NNET_INPUT_LAYER-1; i++){
         valsEMA[i] = Financial.EMA(closeVals, EMA_STEP*(i+1));
      }

      for(int i = 0; i < N_LEARN - ((NNET_INPUT_LAYER-1) * EMA_STEP) ; i++){
         double v[] = new double[NNET_INPUT_LAYER];
         int idx = i + (NNET_INPUT_LAYER-1) * EMA_STEP-1;
         v[0] = closeVals[idx];
         for(int j = 1; j < NNET_INPUT_LAYER; j++){
            v[j] = valsEMA[j-1][idx];
         }

         // aggiungi l'input e l'output
         trainSet.addElement(new SupervisedTrainingElement(
            v, new double[]{closeVals[idx+1]})
         );
      }

      return trainSet;
   }
   */
   private Pair<Double, Double> getMinMax(List<DayBean> days){
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
      return new Pair<Double, Double>(min, max);
   }
}
