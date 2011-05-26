/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rn.financialneuralnetwork.strategies;

import org.rn.financialneuralnetwork.beans.DayBean;
import java.util.ArrayList;
import java.util.List;
import org.neuroph.core.learning.LearningRule;
import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.core.learning.TrainingSet;
import org.rn.financialneuralnetwork.utils.Financial;

/**
 *
 * @author fcanovai
 */
public class EMARSIStrategy extends StrategyAbstract {

    

    private static final int EMA_STEP = 5;
    private static final int RSI_SIZE = 14;

    public EMARSIStrategy(List<DayBean> days, LearningRule learningRule) {
        super(days, learningRule);
        setNnetHiddenLayer(12);
        setNnetInputLayer(6);
        setNnetOutputLayer(1);
    }

    @Override
    protected TrainingSet getTrainingSet(int beginIdx, int todayIdx) {
        //definisce il TrainingSet da ritornare
        TrainingSet trainSet = new TrainingSet();
        int nNaN = Math.max((getNnetInputLayer() - 2) * EMA_STEP, RSI_SIZE);
        //ottiene un vettore di chiusure normalizzate
        double[] normalizedDays = new double[todayIdx - beginIdx + 1];
        ArrayList<Double> normalizedDaysList = normalizeDays(beginIdx, todayIdx);
        for (int i = 0; i < normalizedDaysList.size(); i++) {
            normalizedDays[i] = normalizedDaysList.get(i);
        }
        //calcola RSI sul vettore normalizzato
        double valsRSI[] = Financial.RSI(normalizedDays, RSI_SIZE);
        //calcola le diverse EMA
        double valsEMA[][] = new double[getNnetInputLayer() - 2][];
        for (int i = 0; i < getNnetInputLayer() - 2; i++) {
            valsEMA[i] = Financial.EMA(normalizedDays, EMA_STEP * (i + 1));
        }

        for (int i = 0; i < normalizedDays.length - nNaN; i++) {
            double v[] = new double[getNnetInputLayer()];
            int idx = i + nNaN - 1;
            v[0] = normalizedDays[idx];
            v[1] = valsRSI[idx];
            for (int j = 2; j < getNnetInputLayer(); j++) {
                v[j] = valsEMA[j - 2][idx];
            }

            // aggiungi l'input e l'output
            if(DIRECTION_FORECAST){
               trainSet.addElement(new SupervisedTrainingElement(
                    v, new double[]{getDirForecastOutput(idx)}));
            }else{
               trainSet.addElement(new SupervisedTrainingElement(
                    v, new double[]{normalizedDays[idx + 1]}));
            }
        }
        return trainSet;
    }

    private double getDirForecastOutput(int idx){
       return (days.get(idx).getClose() - days.get(idx+1).getClose()) > 0 ?
          0d : 1d;
    }

    @Override
    public SupervisedTrainingElement getTestElement(int todayIdx) {
        double inputs[] = new double[getNnetInputLayer()];
        double outputs[];
        if(DIRECTION_FORECAST){
           outputs = new double[]{getDirForecastOutput(todayIdx)};
        }else{
           outputs = new double[]{getNormalizedClose(days.get(todayIdx + 1).getClose())};
        }

        //il primo input è la chiusura del giorno
        inputs[0] = getNormalizedClose(days.get(todayIdx).getClose());
        //sceglie le dimensioni dei dati da utilizzare in modo da prendere il 99% del valore della media mobile
        int nel = Math.max(
                RSI_SIZE, (int) Math.ceil((getNnetInputLayer() - 2) * EMA_STEP * 3.45));
        //crea un vettore di dati normalizzati
        double closeVals[] = new double[nel];
        for (int i = 0; i < nel; i++) {
            closeVals[nel - i - 1] = getNormalizedClose(days.get(todayIdx - i).getClose());
        }
        //il secondo input è l'RSI del giorno
        inputs[1] = Financial.RSI(closeVals, RSI_SIZE)[nel - 1];

        for (int i = 0; i < getNnetInputLayer() - 2; i++) {
            inputs[i + 2] = Financial.EMA(closeVals, EMA_STEP * (i + 1))[nel - 1];
        }
        return new SupervisedTrainingElement(inputs, outputs);
    }
}
