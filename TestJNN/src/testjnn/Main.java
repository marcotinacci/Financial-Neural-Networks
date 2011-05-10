/*
 * 
 */
package testjnn;

import beans.DayBean;
import java.util.List;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.nnet.learning.LMS;
import org.neuroph.nnet.learning.MomentumBackpropagation;
import strategies.*;
import utils.CSVHandler;

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
   static private Index INDEX_FILE = Index.SP500;
   // giorno iniziale di addestramento
   static private final int TRAIN_START = 1000;
   // numero minimo di giorni di addestramento
   static private final int NTRAIN = 100;
   static private final int NTEST = 50;

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

      List<DayBean> days = CSVHandler.readAll("data/" + INDEX_FILE.name() + ".csv");

      org.neuroph.core.learning.LearningRule lr = null;
      switch (NET_LEARNING_RULE) {
         case Backpropagation:
            lr = new BackPropagation();
            break;
         case MomentumBackpropagation:
            lr = new MomentumBackpropagation();
            ((MomentumBackpropagation) lr).setMomentum(0.5);
            break;
         case TDPBackpropagation:
            lr = new TDPBackPropagation();
            break;
      }

      ((LMS) lr).setLearningRate(0.1);
      ((LMS) lr).setMaxError(0.0001);
      ((LMS) lr).setMaxIterations(1000);

      double[] forecasts = new double[NTEST];
      double[] closes = new double[NTEST];

      StrategyInterface strat = new EMAStrategy(days, lr);

      // per ogni giorno di test
      for (int i = 0; i < NTEST; i++) {
         // crea una rete addestrata fino al giorno i (relativo)
         NeuralNetwork nnet = strat.getTrainedNeuralNetwork(TRAIN_START, i + TRAIN_START + NTRAIN);
         // inserisci l'input per la previsione del giorno successivo
         nnet.setInput(strat.getTestElement(i + TRAIN_START + NTRAIN).getInput());
         // calcola l'output
         nnet.calculate();
         // estrai e denormalizza l'i-esimo output di previsione
         forecasts[i] = strat.getDenormalizedClose(nnet.getOutput()[0]);
         // estrai l'effettivo valore della chiusura dell'i+1-esima giornata
         closes[i] = days.get(i + TRAIN_START + NTRAIN).getClose();
         // stampa la previsione fatta il giorno i (per la chiusura del giorno i+1)
         System.out.println("Giorno " + (i + 1) + " di " + NTEST);
      }

      // vettore dei profitti
      double dailyProfit[];
      // simula la valutazione del profitto finale con la nostra strategia decisionale
      dailyProfit = main.evaluateProfit(forecasts, closes, true);
      // dati da stampare sul CSV
      double[][] v = new double[3][];
      // vettore delle chiusure reali
      v[0] = closes;
      // vettore delle previsioni
      v[1] = forecasts;
      // vettore dei profitti
      v[2] = dailyProfit;
      // stampa su file CSV le previsioni incrementali, i valori reali e i profitti
      CSVHandler.writeArray(v, "data/result_incremental_" + INDEX_FILE.name()
              + "_" + STRATEGY.name() + "_" + NET_LEARNING_RULE.name() + ".csv");

      // calcola l'errore commesso come media degli errori relativi commessi a ogni chiusura
      double[] errors = new double[NTEST];
      for (int i = 0; i < NTEST; i++) {
         errors[i] = Math.abs(forecasts[i] - closes[i]) / closes[i];
      }
      DescriptiveStatistics ds = new DescriptiveStatistics(errors);
      System.out.println("Errore medio: " + ds.getMean());

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

   /**
    * Valuta il profitto applicando una strategia decisionale di acquisto e
    * vendita totali su crescita e decrescita del valore dell'azione oltre una
    * certa soglia.
    * @param tomorrowForecasts vettore dei valori delle previsioni
    * @param todayCloses vettore dei valori target reali
    * @param print se true stampa a terminale le varie azioni effettuate
    * @return vettore del profitto giornaliero
    */
   private double[] evaluateProfit(double[] tomorrowForecasts,
           double[] todayCloses, boolean print) {

      // lista dei profitti giornalieri
      double profit[] = new double[tomorrowForecasts.length];
      
      // numero di azioni possedute
      double stocks = 0;
      // capitale iniziale
      double money = 1000000;
      // posizione di acquisto (+1) o vendita (-1)
      int position = -1;
      // previsione di andamento del giorno successivo
      int nextSign;
      // soglia di decisione
      double stopLossPerc = 0.02;
      // ultimo prezzo su cui abbiamo eseguito un'azione di acquisto/vendita
      double lastPrice = 0;

      for (int i = 0; i < tomorrowForecasts.length; i++) {
         double diff = tomorrowForecasts[i] - todayCloses[i];
         nextSign = (int) Math.signum(diff);
         // decisione
         if (position == -1 && (nextSign == 1
                 || todayCloses[i] > lastPrice + lastPrice * stopLossPerc))
         {
            stocks = money / todayCloses[i];
            if(print) System.out.println("giorno: " + i + " compra "
                    + stocks + " azioni per " + money + "$.");
            money = 0;
            position = 1;
            lastPrice = todayCloses[i];
         } else if (position == 1 && (nextSign == -1
                 || todayCloses[i] < lastPrice - lastPrice * stopLossPerc))
         {
            money = stocks * todayCloses[i];
            if(print) System.out.println("giorno: " + i + " vendi "
                    + stocks + " azioni per " + money + "$.");
            stocks = 0;
            position = -1;
            lastPrice = todayCloses[i];
         }
         profit[i] = money + stocks * todayCloses[todayCloses.length - 1];
      }
      if(print) System.out.println("Ricavo finale: "
              + (money + stocks * todayCloses[todayCloses.length - 1]));
      return profit;
   }
}
