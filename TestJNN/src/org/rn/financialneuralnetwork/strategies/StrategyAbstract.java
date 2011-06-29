package org.rn.financialneuralnetwork.strategies;

import org.rn.financialneuralnetwork.beans.DayBean;
import java.util.ArrayList;
import java.util.List;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.Neuron;
import org.neuroph.core.learning.LearningRule;
import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.core.learning.TrainingSet;
import org.neuroph.core.transfer.Linear;
import org.neuroph.core.transfer.TransferFunction;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.rn.financialneuralnetwork.utils.Normalize;

/**
 *
 * @author fcanovai
 */
public abstract class StrategyAbstract implements StrategyInterface {

   protected static final boolean DIRECTION_FORECAST = false;

   protected List<DayBean> days;
   protected LearningRule learningRule;
   protected double min;
   protected double max;
   public static final double MIN_RANGE = 0.3;
   public static final double MAX_RANGE = 0.7;
   private int nnetOutputLayer;
   private int nnetInputLayer;
   private int nnetHiddenLayer;

   protected int getNnetHiddenLayer() {
      return nnetHiddenLayer;
   }

   protected void setNnetHiddenLayer(int nnetHiddenLayer) {
      this.nnetHiddenLayer = nnetHiddenLayer;
   }

   protected int getNnetInputLayer() {
      return nnetInputLayer;
   }

   protected void setNnetInputLayer(int nnetInputLayer) {
      this.nnetInputLayer = nnetInputLayer;
   }

   protected int getNnetOutputLayer() {
      return nnetOutputLayer;
   }

   protected void setNnetOutputLayer(int nnetOutputLayer) {
      this.nnetOutputLayer = nnetOutputLayer;
   }

   public void setDays(List<DayBean> days) {
      this.days = days;
   }

   public List<DayBean> getDays() {
      return days;
   }

   public LearningRule getLearningRule() {
      return learningRule;
   }

   public void setLearningRule(LearningRule learningRule) {
      this.learningRule = learningRule;
   }

   public StrategyAbstract(List<DayBean> days, LearningRule learningRule) {
      this.days = days;
      this.learningRule = learningRule;
   }

   @Override
   public NeuralNetwork getTrainedNeuralNetwork(int beginIdx, int todayIdx) {
      NeuralNetwork nnet = createNNet();
      TrainingSet ts = getTrainingSet(beginIdx, todayIdx);
      nnet.learnInSameThread(ts);
      return nnet;
   }

   protected NeuralNetwork createNNet() {
      NeuralNetwork nnet = new MultiLayerPerceptron(nnetInputLayer,
              nnetHiddenLayer, nnetOutputLayer);
      nnet.setLearningRule(learningRule);
//      for(Neuron n : nnet.getOutputNeurons()){
//         n.setTransferFunction(new Linear());
//      }
      return nnet;
   }

   protected ArrayList<Double> normalizeDays(int beginIdx, int todayIdx) {
      // Fissa minimo e massimo
      max = Double.MIN_VALUE;
      min = Double.MAX_VALUE;
      for (int i = beginIdx; i <= todayIdx; i++) {
         double x = days.get(i).getClose();
         if (x < min) {
            min = x;
         }
         if (x > max) {
            max = x;
         }
      }

      ArrayList<Double> normalizedDays = new ArrayList<Double>(todayIdx - beginIdx + 1);
      for (int i = 0; i < todayIdx - beginIdx + 1; i++) {
         normalizedDays.add(
                 Normalize.normalize(days.get(i + beginIdx).getClose(), min, max, MIN_RANGE, MAX_RANGE));
      }

      return normalizedDays;
   }

   public double getDenormalizedClose(double closeValue) {
      return Normalize.denormalize(closeValue, min, max, MIN_RANGE, MAX_RANGE);
   }

   public double getNormalizedClose(double closeValue) {
      return Normalize.normalize(closeValue, min, max, MIN_RANGE, MAX_RANGE);
   }

   protected abstract TrainingSet getTrainingSet(int beginIdx, int todayIdx);

   @Override
   public abstract SupervisedTrainingElement getTestElement(int todayIdx);
}
