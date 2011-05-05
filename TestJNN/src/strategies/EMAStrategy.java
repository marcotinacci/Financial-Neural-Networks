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
import org.neuroph.nnet.learning.SupervisedHebbianLearning;

/**
 *
 * @author fcanovai
 */
public class EMAStrategy extends StrategyAbstract {

    private static final int EMA_STEP = 5;
    private static final int nNaN = (getNnetInputLayer() - 1) * EMA_STEP;

    public EMAStrategy(List<DayBean> days, LearningRule learningRule) {
        super(days,learningRule);
        setNnetHiddenLayer(10);
        setNnetInputLayer(5);
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
