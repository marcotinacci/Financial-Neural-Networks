/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package strategies;

import beans.DayBean;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.learning.LearningRule;
import org.neuroph.core.learning.TrainingSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import utils.Normalize;

/**
 *
 * @author fcanovai
 */
public abstract class StrategyAbstract {
    protected List<DayBean> days;
    protected LearningRule learningRule;
    protected double min;
    protected double max;
    public static final double MIN_RANGE=0.1;
    public static final double MAX_RANGE=0.9;
    private static int nnetOutputLayer;
    private static int nnetInputLayer;
    private static int nnetHiddenLayer;

    protected static int getNnetHiddenLayer() {
        return nnetHiddenLayer;
    }

    protected static void setNnetHiddenLayer(int nnetHiddenLayer) {
        StrategyAbstract.nnetHiddenLayer = nnetHiddenLayer;
    }

    protected static int getNnetInputLayer() {
        return nnetInputLayer;
    }

    protected static void setNnetInputLayer(int nnetInputLayer) {
        StrategyAbstract.nnetInputLayer = nnetInputLayer;
    }

    protected static int getNnetOutputLayer() {
        return nnetOutputLayer;
    }

    protected static void setNnetOutputLayer(int nnetOutputLayer) {
        StrategyAbstract.nnetOutputLayer = nnetOutputLayer;
    }
    
   public void setDays(List<DayBean> days){
       this.days=days;
   };
   public List<DayBean> getDays(){
       return days;
   }

    public LearningRule getLearningRule() {
        return learningRule;
    }

    public void setLearningRule(LearningRule learningRule) {
        this.learningRule = learningRule;
    }
      
    
   public abstract NeuralNetwork getTrainedNeuralNetwork(int beginIdx, int todayIdx);
   
   public StrategyAbstract (List<DayBean> days, LearningRule learningRule){
       this.days=days;
       this.learningRule=learningRule;
   }
   
   protected NeuralNetwork createNNet() {
        NeuralNetwork nnet = new MultiLayerPerceptron(nnetInputLayer,
                nnetHiddenLayer, nnetOutputLayer);
        nnet.setLearningRule(learningRule);
        return nnet;
   }
        
   protected abstract TrainingSet getTrainingSet();
   
   private ArrayList<Double> normalizeDays (int beginIdx, int todayIdx){
       //Fissa minimo e massimo
       max=Double.MIN_VALUE;
       min=Double.MAX_VALUE;
       for (int i = beginIdx; i <= todayIdx; i++) {
         double x = days.get(i).getClose();
         if (x < min) {
            min = x;
         }
         if (x > max) {
            max = x;
         }
      }
       
       ArrayList<Double> normalizedDays=new ArrayList<Double>(todayIdx-beginIdx+1);
       for (int i=0; i<normalizedDays.size();i++){
           normalizedDays.set(i,
                   Normalize.normalize(days.get(i+beginIdx).getClose(), min, max, MIN_RANGE, MAX_RANGE));
       }
       
       return normalizedDays;
   }
}
