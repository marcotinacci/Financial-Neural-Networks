/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package strategies;

import beans.DayBean;
import java.util.List;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.learning.LearningRule;
import org.neuroph.core.learning.TrainingSet;

/**
 *
 * @author fcanovai
 */
public class StandardStrategy extends StrategyAbstract{
    public StandardStrategy(List<DayBean> days, LearningRule learningRule) {
        super(days,learningRule);
        setNnetHiddenLayer(30);
        setNnetInputLayer(30);
        setNnetOutputLayer(1);
    }

    @Override
    public NeuralNetwork getTrainedNeuralNetwork(int beginIdx, int todayIdx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected TrainingSet getTrainingSet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
