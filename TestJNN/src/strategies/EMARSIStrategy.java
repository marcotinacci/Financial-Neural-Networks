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
import org.neuroph.nnet.MultiLayerPerceptron;

/**
 *
 * @author fcanovai
 */
public class EMARSIStrategy extends StrategyAbstract {

    private static final int EMA_STEP = 5;
    private static final int RSI_SIZE = 14;
    private static final int nNaN = Math.max((getNnetInputLayer() - 2) * EMA_STEP, RSI_SIZE);

    public EMARSIStrategy(List<DayBean> days, LearningRule learningRule) {
        super(days, learningRule);
        setNnetHiddenLayer(12);
        setNnetInputLayer(6);
        setNnetOutputLayer(1);
    }

    @Override
    public NeuralNetwork getTrainedNeuralNetwork(int beginIdx, int todayIdx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected TrainingSet getTrainingSet() {
         TrainingSet trainSet = new TrainingSet();
         
         return trainSet;
    }
}
