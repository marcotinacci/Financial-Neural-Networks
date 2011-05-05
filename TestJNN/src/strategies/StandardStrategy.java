/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package strategies;

import beans.DayBean;
import java.util.ArrayList;
import java.util.List;
import org.neuroph.core.learning.LearningRule;
import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.core.learning.TrainingSet;

/**
 *
 * @author fcanovai
 */
public class StandardStrategy extends StrategyAbstract {

    public StandardStrategy(List<DayBean> days, LearningRule learningRule) {
        super(days, learningRule);
        setNnetHiddenLayer(30);
        setNnetInputLayer(30);
        setNnetOutputLayer(1);
    }

    @Override
    protected TrainingSet getTrainingSet(int beginIdx, int todayIdx) {

        TrainingSet trainSet = new TrainingSet();
        int nNaN = getNnetInputLayer();
        ArrayList<Double> normalizedDays = normalizeDays(beginIdx, todayIdx);
        // ciclo su training sets
        for (int i = 0; i < normalizedDays.size() - nNaN; i++) {
            double v[] = new double[getNnetInputLayer()];
            // ciclo sul singolo input
            for (int j = 0; j < getNnetInputLayer(); j++) {
                v[j] = normalizedDays.get(i + j);
            }
            // aggiungi l'input e l'output
            trainSet.addElement(new SupervisedTrainingElement(
                    v, new double[]{normalizedDays.get(i + getNnetInputLayer())}));
        }
        return trainSet;

    }

    @Override
    public SupervisedTrainingElement getTestElement(int todayIdx) {
        double inputs[] = new double[getNnetInputLayer()];
        double outputs[] = new double[]{getNormalizedClose(days.get(todayIdx + 1).getClose())};
        
        for (int i = 0; i < getNnetInputLayer(); i++) {
                inputs[i] = getNormalizedClose(days.get(todayIdx-getNnetInputLayer()+1+i).getClose());
            }
         return new SupervisedTrainingElement(inputs, outputs);
    }
}
