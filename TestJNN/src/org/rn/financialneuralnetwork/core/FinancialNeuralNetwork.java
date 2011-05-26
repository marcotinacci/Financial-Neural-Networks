package org.rn.financialneuralnetwork.core;

import org.rn.financialneuralnetwork.learningRules.STEBackPropagation;
import org.rn.financialneuralnetwork.learningRules.TDPBackPropagation;
import org.rn.financialneuralnetwork.beans.DayBean;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.nnet.learning.LMS;
import org.rn.financialneuralnetwork.strategies.*;
import org.rn.financialneuralnetwork.utils.CSVHandler;

/**
 *
 * @author Marco Tinacci
 */
public class FinancialNeuralNetwork {

    enum Strategy {

        ema,
        rsi,
        std
    };

    enum LearningRule {

        Backpropagation,
        TDPBackpropagation,
        STEBackpropagation
    };

    enum Index {

        SP500,
        NASDAQ100,
        NIKKEI225,
        MIB
    };
    static private Strategy STRATEGY = Strategy.rsi;
    static private LearningRule NET_LEARNING_RULE = LearningRule.STEBackpropagation;
    static private Index INDEX_FILE = Index.NASDAQ100;
    // giorno iniziale di addestramento
    static private final int TRAIN_START = 0;
    // numero minimo di giorni di addestramento
    static private final int NTRAIN = 1000;
    static private final int NTEST = 250;
    static private PrintStream file = null;

    public static void main(String[] args) throws Exception {
     for(Strategy s : Strategy.values()){
         STRATEGY = s;
            for(LearningRule lr : LearningRule.values()){
               NET_LEARNING_RULE = lr;
                  for(Index idx : Index.values()){
                     INDEX_FILE = idx;
                     if(s == Strategy.rsi){
                        System.out.println("- "+s+","+lr+","+idx);
                        doForecast();
                     }
                  }
               }
      }
    }

    private static void doForecast() throws Exception {
        Date now = new Date();
        file = new PrintStream(new FileOutputStream("logs/" + INDEX_FILE.name()
                + "_" + STRATEGY.name() + "_" + NET_LEARNING_RULE.name() + "_"
                + DateFormat.getDateTimeInstance(
                DateFormat.LONG, DateFormat.LONG).format(now) + ".txt"));

        List<DayBean> days = CSVHandler.readAll("index/" + INDEX_FILE.name() + ".csv");

        org.neuroph.core.learning.LearningRule lr = null;
        switch (NET_LEARNING_RULE) {
            case Backpropagation:
                lr = new BackPropagation();
                break;
            case TDPBackpropagation:
                lr = new TDPBackPropagation();
                break;
            case STEBackpropagation:
                lr = new STEBackPropagation();
                break;
        }

        ((LMS) lr).setLearningRate(0.1);
        ((LMS) lr).setMaxError(0.0001);
        ((LMS) lr).setMaxIterations(1000);

        double[] forecasts = new double[NTEST];
        double[] closes = new double[NTEST];

        StrategyInterface strat = null;
        switch (STRATEGY) {
            case std:
                strat = new StandardStrategy(days, lr);
                break;
            case ema:
                strat = new EMAStrategy(days, lr);
                break;
            case rsi:
                strat = new EMARSIStrategy(days, lr);
                break;
        }

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
            // estrai l'effettivo valore della chiusura dell'i-esima giornata
            closes[i] = days.get(i + TRAIN_START + NTRAIN).getClose();
            // stampa la previsione fatta il giorno i (per la chiusura del giorno i+1)
            System.out.println("Giorno " + (i + 1) + " di " + NTEST);
            file.println("Giorno " + (i + 1) + " di " + NTEST);
        }

        // vettore dei profitti
        double dailyProfit[];
        // simula la valutazione del profitto finale con la nostra strategia decisionale
        dailyProfit = evaluateProfit(forecasts, closes, true);
        // dati da stampare sul CSV
        double[][] v = new double[3][];
        // vettore delle chiusure reali
        v[0] = closes;
        // vettore delle previsioni
        v[1] = forecasts;
        // vettore dei profitti
        v[2] = dailyProfit;
        // stampa su file CSV le previsioni incrementali, i valori reali e i profitti
        CSVHandler.writeArray(v, "data/" + INDEX_FILE.name()
                + "_" + STRATEGY.name() + "_" + NET_LEARNING_RULE.name() + ".csv");

        /*
      // vettore dei profitti
      double dailyProfit[];
      // simula la valutazione del profitto finale con la nostra strategia decisionale
      dailyProfit = evaluateProfit2(forecasts, closes, true);
      // dati da stampare sul CSV
      double[][] v = new double[3][];
      // vettore delle chiusure reali
      v[0] = new double[closes.length];
      for(int i = 0; i < v[0].length; i++){
         v[0][i] = (days.get(i + TRAIN_START + NTRAIN).getClose() -
            days.get(i + TRAIN_START + NTRAIN+1).getClose()) > 0 ? 0d : 1d;
      }
      // vettore delle previsioni
      v[1] = new double[closes.length];
      for(int i = 0; i < v[0].length; i++){
         v[1][i] = forecasts[i] < 0.5d ? 0d : 1d;
      }
      // vettore dei profitti
      v[2] = dailyProfit;
      // stampa su file CSV le previsioni incrementali, i valori reali e i profitti
      CSVHandler.writeArray(v, "data/dir_" + INDEX_FILE.name()
              + "_" + STRATEGY.name() + "_" + NET_LEARNING_RULE.name() + ".csv");
*/

        // calcola l'errore commesso come media degli errori relativi commessi a ogni chiusura
        double[] errors = new double[NTEST];
        for (int i = 1; i < NTEST; i++) {
            errors[i] = Math.abs(forecasts[i - 1] - closes[i]) / closes[i];
        }
        DescriptiveStatistics ds = new DescriptiveStatistics(errors);
        System.out.println("Errore medio: " + ds.getMean());
        file.println("Errore medio: " + ds.getMean());
        file.close();
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
    private static double[] evaluateProfit(double[] tomorrowForecasts,
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
        double stopLossPerc = 2;
        // ultimo prezzo su cui abbiamo eseguito un'azione di acquisto/vendita
        double lastPrice = 0;

        for (int i = 0; i < tomorrowForecasts.length; i++) {
            double diff = tomorrowForecasts[i] - todayCloses[i];
            nextSign = (int) Math.signum(diff);
            // decisione
            if (position == -1 && (nextSign == 1
                    || todayCloses[i] > lastPrice + lastPrice * stopLossPerc)) {
                stocks = money / todayCloses[i];
                if (print) {
                    System.out.println("giorno: " + i + " compra "
                            + stocks + " azioni per " + money + "$.");
                    file.println("giorno: " + i + " compra "
                            + stocks + " azioni per " + money + "$.");
                    if (nextSign != 1) {
                        System.out.println("stoploss");
                        file.println("stoploss");
                    }
                }
                money = 0;
                position = 1;
                lastPrice = todayCloses[i];
            } else if (position == 1 && (nextSign == -1
                    || todayCloses[i] < lastPrice - lastPrice * stopLossPerc)) {
                money = stocks * todayCloses[i];
                if (print) {
                    System.out.println("giorno: " + i + " vendi "
                            + stocks + " azioni per " + money + "$.");
                    file.println("giorno: " + i + " vendi "
                            + stocks + " azioni per " + money + "$.");
                    if (nextSign != -1) {
                        System.out.println("stoploss");
                        file.println("stoploss");
                    }
                }
                stocks = 0;
                position = -1;
                lastPrice = todayCloses[i];
            }
            profit[i] = money + stocks * todayCloses[i];
        }

        if (print) {
            System.out.println("Ricavo finale: "
                    + (money + stocks * todayCloses[todayCloses.length - 1]));
            file.println("Ricavo finale: "
                    + (money + stocks * todayCloses[todayCloses.length - 1]));
        }
        return profit;
    }


   private static double[] evaluateProfit2(double[] tomorrowForecasts,
           double[] todayCloses, boolean print) {

      // lista dei profitti giornalieri
      double profit[] = new double[tomorrowForecasts.length];

      // numero di azioni possedute
      double stocks = 0;
      // capitale iniziale
      double money = 1000000;
      // posizione di acquisto (+1) o vendita (-1)
      int position = -1;

      for (int i = 0; i < tomorrowForecasts.length; i++) {
         boolean buy = tomorrowForecasts[i] < 0.5 ? false : true;
         // decisione
         if (position == -1 && buy)
         {
            stocks = money / todayCloses[i];
            if(print){
               System.out.println("giorno: " + i + " compra "
                    + stocks + " azioni per " + money + "$.");
               file.println("giorno: " + i + " compra "
                    + stocks + " azioni per " + money + "$.");
            }
            money = 0;
            position = 1;
         } else if (position == 1 && !buy)
         {
            money = stocks * todayCloses[i];
            if(print){
               System.out.println("giorno: " + i + " vendi "
                    + stocks + " azioni per " + money + "$.");
               file.println("giorno: " + i + " vendi "
                    + stocks + " azioni per " + money + "$.");
            }
            stocks = 0;
            position = -1;
         }
         profit[i] = money + stocks * todayCloses[i];
      }

      if(print)
      {
         System.out.println("Ricavo finale: "
              + (money + stocks * todayCloses[todayCloses.length - 1]));
         file.println("Ricavo finale: "
              + (money + stocks * todayCloses[todayCloses.length - 1]));
      }
      return profit;
   }
}
