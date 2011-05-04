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
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.nnet.learning.LMS;
import org.neuroph.nnet.learning.MomentumBackpropagation;
import utils.CSVHandler;
import utils.Normalize;
import utils.Financial;

// TODO apprendimento incrementale
/**
 *
 * @author Marco Tinacci
 */
public class Main {

   enum Strategy {

      std, ema, rsi
   };

   enum LearningRule {

      Backpropagation, TDPBackpropagation, MomentumBackpropagation
   };

   enum Index {

      SP500, NASDAQ100, NIKKEI225, MIB
   };
   static private Strategy STRATEGY = Strategy.rsi;
   static private LearningRule NET_LEARNING_RULE = LearningRule.TDPBackpropagation;
   static private Index INDEX_FILE = Index.MIB;
   //numero di vettori di addestramento
   static private final int N_LEARN = 1000;
   //numero di vettori di test
   static private final int N_TEST = 350;
   // dimensioni rete
   private int nnetInputLayer;
   private int nnetHiddenLayer;
   private int nnetOutputLayer;
   //numero di elementi iniziali dei vettori di input da non considerare
   private int nNaN;
   // range di normalizzazione
   static private final double MIN_RANGE = 0.1;
   static private final double MAX_RANGE = 0.9;
   static private final int EMA_STEP = 5;
   static private final int RSI_SIZE = 14;

   /**
    * @param args the command line arguments
    */
   public static void main(String[] args) throws Exception {
      Main main = new Main();
      /*
      for (Index idx : Index.values()) {
         INDEX_FILE = idx;
         List<DayBean> list = CSVHandler.readAll("data/" + INDEX_FILE.name() + ".csv");
         for (Strategy stg : Strategy.values()) {
            STRATEGY = stg;
            for (LearningRule lr : LearningRule.values()) {
               NET_LEARNING_RULE = lr;
               System.out.println(" - " + INDEX_FILE + ", " + STRATEGY
                       + ", " + NET_LEARNING_RULE);
               main.testNN(list);
            }
         }
      }*/

      List<DayBean> list = CSVHandler.readAll("data/" + INDEX_FILE.name() + ".csv");


      NeuralNetwork nnet = main.neuralNetworkTraining(list, 0, N_LEARN);
      main.neuralNetworkTesting(nnet, list, 0, N_LEARN, N_TEST);

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

   private NeuralNetwork createNNet(LearningRule learningRule, Strategy strategy) {
      nnetOutputLayer = 1;
      switch (strategy) {
         case std:
            nnetInputLayer = 30;
            nnetHiddenLayer = 30;
            nNaN = nnetInputLayer - 1;
            break;
         case ema:
            nnetInputLayer = 5;
            nnetHiddenLayer = 12;
            nNaN = (nnetInputLayer - 1) * EMA_STEP;
            break;
         case rsi:
            nnetInputLayer = 6;
            nnetHiddenLayer = 20;
            nNaN = Math.max((nnetInputLayer - 2) * EMA_STEP, RSI_SIZE);
            break;
      }

      NeuralNetwork nnet = new MultiLayerPerceptron(nnetInputLayer,
              nnetHiddenLayer, nnetOutputLayer);
      switch (learningRule) {
         case Backpropagation:
            nnet.setLearningRule(new BackPropagation());
            break;
         case MomentumBackpropagation:
            nnet.setLearningRule(new MomentumBackpropagation());
            ((MomentumBackpropagation) nnet.getLearningRule()).setMomentum(0.5);
            break;
         case TDPBackpropagation:
            nnet.setLearningRule(new TDPBackPropagation());
            break;
      }

      ((LMS) nnet.getLearningRule()).setLearningRate(0.1);
      ((LMS) nnet.getLearningRule()).setMaxError(0.0001);
      ((LMS) nnet.getLearningRule()).setMaxIterations(1000);

      return nnet;
   }

   private TrainingSet createTrainingSet(Strategy strategy, List<DayBean> days,
           int nLearn, int beginIdx, double min, double max) {

      TrainingSet ts = null;
      switch (strategy) {
         case std:
            ts = standardSet(days, min, max, nLearn, beginIdx);
            break;
         case ema:
            ts = EMASet(days, beginIdx, nLearn, min, max);
            break;
         case rsi:
            ts = EMARSISet(days, beginIdx, nLearn, min, max);
            break;
      }
      return ts;
   }

   public NeuralNetwork neuralNetworkTraining(List<DayBean> days, int beginIdx,
           int nLearn){
      // creazione della rete neurale
      NeuralNetwork nnet = createNNet(NET_LEARNING_RULE, STRATEGY);

      // intervallo su cui calcolare i parametri di normalizzazione
      Pair<Double, Double> minmaxTrain = getMinMax(days, beginIdx, nLearn);
      // creazione dell'insieme di dati di addestramento
      TrainingSet trainSet = createTrainingSet(STRATEGY, days, nLearn, beginIdx,
              minmaxTrain.fst, minmaxTrain.snd);
      // addestramento della rete
      nnet.learnInSameThread(trainSet);
      return nnet;
   }

   public double incrementalOutput(NeuralNetwork nnet, List<DayBean> days,
         int beginIdx, int nLearn)
           throws FileNotFoundException, IOException{

      // intervallo su cui calcolare i parametri di normalizzazione
      Pair<Double, Double> minmaxTest = getMinMax(days, beginIdx,
              beginIdx + nLearn);

      nnet.setInput(EMARSIElement(days, beginIdx + nLearn + 1,
              MIN_RANGE, MIN_RANGE).getInput());
      nnet.calculate();
      double[] networkOutput = nnet.getOutput();

      // TODO siamo rimasti qui
      
      return 0d;
   }

   public void neuralNetworkTesting(NeuralNetwork nnet, List<DayBean> days, 
           int beginIdx, int nLearn, int nTest)
           throws FileNotFoundException, IOException{

      // test errore
      double v_days[] = new double[nTest - nNaN];
      double v_output[] = new double[nTest - nNaN];
      double v_targets[] = new double[nTest - nNaN];
      double errors[] = new double[nTest - nNaN];

      // intervallo su cui calcolare i parametri di normalizzazione
      Pair<Double, Double> minmaxTest = getMinMax(days, beginIdx,
              beginIdx + nLearn + nTest);
      //crea un set per eseguire dei test
      TrainingSet testSet = createTrainingSet(STRATEGY, days, nTest, 
              beginIdx + nLearn, minmaxTest.fst, minmaxTest.snd);

      for (int i = 0; i < testSet.size(); i++) {
         nnet.setInput(testSet.elementAt(i).getInput());
         nnet.calculate();
         double[] networkOutput = nnet.getOutput();
         double v1 = days.get(beginIdx + nLearn + nNaN + i).getClose();
         double v2 = Normalize.denormalize(networkOutput[0], minmaxTest.fst,
                 minmaxTest.snd, MIN_RANGE, MAX_RANGE);

         errors[i] = (Math.abs(v1 - v2) / v1);

         v_days[i] = beginIdx + nLearn + nNaN + i;
         v_output[i] = v2;
         v_targets[i] = v1;
      }

      DescriptiveStatistics ds = new DescriptiveStatistics(errors);
      System.out.println("Errore medio: " + ds.getMean());

      double v[][] = new double[3][];
      v[0] = v_days;
      v[1] = v_output;
      v[2] = v_targets;

      CSVHandler.writeArray(v, "data/result_" + INDEX_FILE.name()
              + "_" + STRATEGY.name() + "_" + NET_LEARNING_RULE.name() + ".csv");

      evaluateProfit(v_output, v_targets);

   }

   private void evaluateProfit(double[] outputs, double[] targets) {
      
      double stocks = 0;
      double money = 1000000;
      int position = -1; // {-1, +1}
      int nextSign;

      for (int i = 1; i < outputs.length; i++) {

         double diff = outputs[i] - targets[i-1];

         //if(Math.abs(diff)/targets[i-1] > 0.01){
         nextSign = (int) Math.signum(diff);
         // decisione
         if (position == -1 && nextSign == 1) {
            stocks = money / targets[i-1];
            System.out.println("giorno: "+ i +
                    " compra "+ stocks +" azioni per "+ money + "$.");
            money = 0;
            position = 1;
         } else if (position == 1 && nextSign == -1) {
            money = stocks * targets[i-1];
            System.out.println("giorno: " + i
                    + " vendi " + stocks + " azioni per " + money + "$.");
            stocks = 0;
            position = -1;
         }
      }
      System.out.println("Ricavo finale: " +
              (money + stocks * targets[targets.length-1]));

   }

   private TrainingSet standardSet(List<DayBean> days, double min, double max, int nLearn, int beginIdx) {
      TrainingSet trainSet = new TrainingSet();

      // ciclo su training sets
      for (int i = beginIdx; i < beginIdx + nLearn - nNaN; i++) {
         double v[] = new double[nnetInputLayer];
         // ciclo sul singolo input
         for (int j = 0; j < nnetInputLayer; j++) {
            v[j] = Normalize.normalize(days.get(i + j).getClose(), min, max,
                    MIN_RANGE, MAX_RANGE);
         }
         // aggiungi l'input e l'output
         trainSet.addElement(new SupervisedTrainingElement(
                 v, new double[]{
                    Normalize.normalize(days.get(i + nnetInputLayer).getClose(),
                    min, max, MIN_RANGE, MAX_RANGE)}));
      }
      return trainSet;
   }

   private TrainingSet EMASet(List<DayBean> days, int begin, int nel,
           double min, double max) {
      TrainingSet trainSet = new TrainingSet();
      // crea il vettore di calori di chiusura
      double closeVals[] = new double[nel];
      for (int i = 0; i < nel; i++) {
         closeVals[i] = Normalize.normalize(days.get(begin + i).getClose(), min,
                 max, MIN_RANGE, MAX_RANGE);
      }

      double valsEMA[][] = new double[nnetInputLayer - 1][];
      // calcola gli EMA per ogni nodo di input dedicato
      for (int i = 0; i < nnetInputLayer - 1; i++) {
         valsEMA[i] = Financial.EMA(closeVals, EMA_STEP * (i + 1));
      }

      // composizione del train set
      for (int i = 0; i < nel - nNaN; i++) {
         double v[] = new double[nnetInputLayer];
         int idx = i + nNaN - 1;
         v[0] = closeVals[idx];
         for (int j = 1; j < nnetInputLayer; j++) {
            v[j] = valsEMA[j - 1][idx];
         }

         // aggiungi l'input e l'output
         trainSet.addElement(new SupervisedTrainingElement(
                 v, new double[]{closeVals[idx + 1]}));
      }

      return trainSet;
   }

   private Pair<Double, Double> getMinMax(List<DayBean> days, int beginIdx, int nel) {
      double max = 0;
      double min = Double.MAX_VALUE;
      for (int i = beginIdx; i < beginIdx + nel; i++) {
         double x = days.get(i).getClose();
         if (x < min) {
            min = x;
         }
         if (x > max) {
            max = x;
         }
      }
      return new Pair<Double, Double>(min, max);
   }

   private TrainingSet EMARSISet(List<DayBean> days, int begin, int nel,
           double min, double max) {
      TrainingSet trainSet = new TrainingSet();
      // crea il vettore di valori di chiusura
      double closeVals[] = new double[nel];
      for (int i = 0; i < nel; i++) {
         closeVals[i] = Normalize.normalize(days.get(begin + i).getClose(), min,
                 max, MIN_RANGE, MAX_RANGE);
      }
      double valsRSI[] = Financial.RSI(closeVals, RSI_SIZE);

      double valsEMA[][] = new double[nnetInputLayer - 2][];
      for (int i = 0; i < nnetInputLayer - 2; i++) {
         valsEMA[i] = Financial.EMA(closeVals, EMA_STEP * (i + 1));
      }

      for (int i = 0; i < nel - nNaN; i++) {
         double v[] = new double[nnetInputLayer];
         int idx = i + nNaN - 1;
         v[0] = closeVals[idx];
         v[1] = valsRSI[idx];
         for (int j = 2; j < nnetInputLayer; j++) {
            v[j] = valsEMA[j - 2][idx];
         }

         // aggiungi l'input e l'output
         trainSet.addElement(new SupervisedTrainingElement(
                 v, new double[]{closeVals[idx + 1]}));
      }

      return trainSet;
   }

   private SupervisedTrainingElement EMARSIElement(List<DayBean> days, int idx, 
           double min, double max){

      double v[] = new double[nnetInputLayer];
      int nel = Math.max(
              RSI_SIZE, (int)Math.ceil((nnetInputLayer-2)*EMA_STEP*3.45));
      double closeVals[] = new double[nel];
      v[0] = Normalize.normalize(days.get(idx).getClose(), min,
                 max, MIN_RANGE, MAX_RANGE);

      for (int i=0; i < nel; i++){
         closeVals[nel-i-1] = days.get(idx-i).getClose();
      }

      v[1] = Financial.RSI(closeVals, RSI_SIZE)[nel-1];

      for (int i = 0; i < nnetInputLayer - 2; i++) {
         v[i+2] = Financial.EMA(closeVals, EMA_STEP * (i + 1))[nel-1];
      }
      return new SupervisedTrainingElement(
                 v, new double[]{days.get(idx + 1).getClose()});
   }
}
