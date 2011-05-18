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
public class EMAStrategy extends StrategyAbstract {

    private static final int EMA_STEP = 5;

    public EMAStrategy(List<DayBean> days, LearningRule learningRule) {
        super(days,learningRule);
        setNnetHiddenLayer(10);
        setNnetInputLayer(5);
        setNnetOutputLayer(1);
    }


    @Override
    protected TrainingSet getTrainingSet(int beginIdx, int todayIdx) {
        
        TrainingSet trainSet = new TrainingSet();
        int nNaN = (getNnetInputLayer() - 1) * EMA_STEP;
        // crea il vettore di calori di chiusura
        double[] normalizedDays = new double[todayIdx - beginIdx + 1];
        ArrayList<Double> normalizedDaysList = normalizeDays(beginIdx, todayIdx);
        for (int i = 0; i < normalizedDays.length; i++) {
            normalizedDays[i] = normalizedDaysList.get(i);
        }
        double valsEMA[][] = new double[getNnetInputLayer() - 1][];
        for (int i = 0; i < getNnetInputLayer() - 1; i++) {
            valsEMA[i] = Financial.EMA(normalizedDays, EMA_STEP * (i + 1));
        }
        
        for (int i = 0; i < normalizedDays.length - nNaN; i++) {
            double v[] = new double[getNnetInputLayer()];
            int idx = i + nNaN - 1;
            v[0] = normalizedDays[idx];
            for (int j = 1; j < getNnetInputLayer(); j++) {
                v[j] = valsEMA[j - 1][idx];
            }

            // aggiungi l'input e l'output
            trainSet.addElement(new SupervisedTrainingElement(
                    v, new double[]{normalizedDays[idx + 1]}));
        }
        return trainSet;        
    }

    @Override
    public SupervisedTrainingElement getTestElement(int todayIdx) {
        double inputs[] = new double[getNnetInputLayer()];
        double outputs[] = new double[]{getNormalizedClose(days.get(todayIdx + 1).getClose())};

        //il primo input è la chiusura del giorno
        inputs[0] = getNormalizedClose(days.get(todayIdx).getClose());
        //sceglie le dimensioni dei dati da utilizzare in modo da prendere il 99% del valore della media mobile
        int nel = (int) Math.ceil((getNnetInputLayer() - 2) * EMA_STEP * 3.45);
        //crea un vettore di dati normalizzati
        double closeVals[] = new double[nel];
        for (int i = 0; i < nel; i++) {
            closeVals[nel - i - 1] = getNormalizedClose(days.get(todayIdx - i).getClose());
        }
        for (int i = 0; i < getNnetInputLayer() - 1; i++) {
            inputs[i + 1] = Financial.EMA(closeVals, EMA_STEP * (i + 1))[nel - 1];
        }
        return new SupervisedTrainingElement(inputs, outputs);
    }
}
