/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.util.LinkedList;
import org.apache.commons.math.stat.descriptive.moment.Mean;

/**
 *
 * @author marco tinacci
 */
public class Financial {

    /**
     * Exponential Moving Average
     * @param vin
     * @param timePer
     * @return vout
     */
    static public double[] EMA(double[] vin, int timePer) {
        double k = 2. / (timePer + 1);
        double oneK = 1 - k;
        double vout[] = new double[vin.length];
        Mean mean = new Mean();
        vout[timePer - 1] = mean.evaluate(vin, 0, timePer);

        double kvin[] = new double[vin.length];
        for (int i = timePer - 1; i < vin.length; i++) {
            kvin[i - timePer + 1] = k * vin[i];
        }

        for (int i = 0; i < timePer - 1; i++) {
            vout[i] = Double.NaN;
        }

        vout[timePer - 1] = kvin[0] + vout[timePer - 1] * oneK;
        for (int i = timePer; i < vout.length; i++) {
            vout[i] = kvin[i - timePer + 1] + vout[i - 1] * oneK;
        }
        return vout;
    }

    //RSI = 100 * U / (U + D) dove
    //U = media delle chiusure al rialzo di X giorni
    //D = media delle chiusure al ribasso di X giorni
    static public double[] RSI(double[] vin, int timePer) {

        double[] vout = new double[vin.length];                         //vettore di ritorno
        LinkedList<Double> closePrice = new LinkedList();      //prezzi di chiusura
        LinkedList<Double> diffPrice = new LinkedList();        //differenze di prezzo dal giorno precedente
        LinkedList<Double> advance = new LinkedList();        //differenze positive
        LinkedList<Double> decline = new LinkedList();          //valore assoluto delle differenze negative

        double diff;
        for (int i = 0; i < timePer; i++) {
            vout[i] = Double.NaN;
            closePrice.addLast(vin[i]);
            if (i != 0) {
                diff = closePrice.getLast() - closePrice.get(i - 1);
                diffPrice.addLast(diff);
                if (diff > 0) {
                    advance.addLast(diff);
                } else {
                    decline.addLast(Math.abs(diff));
                }
            }
        }

        for (int i = timePer; i < vin.length; i++) {
            closePrice.addLast(vin[i]);
            diff = closePrice.getLast() - closePrice.get(closePrice.size() - 2);
            diffPrice.addLast(diff);
            if (diff > 0) {
                advance.addLast(diff);
            } else {
                decline.addLast(Math.abs(diff));
            }
            //Calcola U
            double uSum = 0;
            for (int j = 0; j < advance.size(); j++) {
                uSum = uSum+advance.get(j);
            }
            double uMean = uSum / advance.size();

            //Calcola D
            double dSum = 0;
            for (int j = 0; j < decline.size(); j++) {
                dSum += decline.get(j);
            }
            double dMean = dSum / decline.size();

            
            if (Double.isNaN(uMean))
                uMean=0;
            if (Double.isNaN(dMean))
                dMean=0;
            //inserisce il valore di RSI nell'array di ritorno
            vout[i] = uMean / (uMean + dMean);

            //rimuove gli elementi in testa alle liste
            closePrice.removeFirst();
            if (diffPrice.getFirst() > 0) {
                advance.removeFirst();
            } else {
                decline.removeFirst();
            }
            diffPrice.removeFirst();
        }

        return vout;
    }
    //static public int Mean(double[] vin, int begin, int nel){
    //
    //}
}
