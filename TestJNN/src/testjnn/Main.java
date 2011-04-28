/*
 * 
 */
package testjnn;

import beans.DayBean;
import utils.Pair;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.core.learning.TrainingSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.nnet.learning.LMS;
import org.neuroph.nnet.learning.MomentumBackpropagation;
import utils.CSVHandler;
import utils.Normalize;
import utils.Financial;

/**
 *
 * @author Marco Tinacci
 */
public class Main {

    // nome dell'indice da utilizzare (nella cartella "data/")
    static private final String INDEX_FILE = "SP500";
    // nome del tipo di rete da usare (backpropagation, TDPBackpropagation, 
    // momentumBackpropagation)
    static private final String NET_LEARNING_RULE = "TDPBackpropagation";
    // nome della strategia da usare (standard, EMA, RSI)
    static private final String STRATEGY = "RSI";
    //numero di vettori di addestramento
    static private final int N_LEARN = 1000;
    //numero di vettori di test
    static private final int N_TEST = 350;
    // dimensioni rete
    private int nnetInputLayer;
    private int nnetHiddenLayer;
    private int nnetOutputLayer;
  //numero di elementi iniziali dei vettori di input da non considerare
    private int nNaN;
    // range di normalizzazione
    static private final double MIN_RANGE = 0.1;
    static private final double MAX_RANGE = 0.9;
    static private final int EMA_STEP = 5;
    static private final int RSI_SIZE = 14;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        Main main = new Main();
        List<DayBean> list = CSVHandler.readAll("data/" + INDEX_FILE + ".csv");
        //System.out.println(list.toString().replace("], [", "]\n["));
        main.testNN(list);

        /*
         * corpo procedura generazione moti browniani
        long[] seed = new long[6];
        for(int i = 0; i < 3; i++){
        seed[i] = (long)(Math.random() * 4294967087d);
        }
        for(int i = 0; i < 3; i++){
        seed[i+3] = (long)(Math.random() * 4294944443d);
        }
        MRG32k3a.setPackageSeed(seed);
        
        MRG32k3a rs = new MRG32k3a();
        BrownianMotion bm = new BrownianMotion(1, 1, 1, rs);
        bm.setObservationTimes(1, 10);
        System.out.println("moto browniano: " + Arrays.toString(bm.generatePath()));
         */

    }

    private NeuralNetwork createNNet(String learningRule, String strategy) {
        nnetOutputLayer = 1;
        if ("standard".equals(strategy)) {
            nnetInputLayer = 30;
            nnetHiddenLayer = 30;
        }
        if ("EMA".equals(strategy)) {
            nnetInputLayer = 5;
            nnetHiddenLayer = 12;
            nNaN=(nnetInputLayer - 1) * EMA_STEP;
        }
        if ("RSI".equals(strategy)) {
            nnetInputLayer = 6;
            nnetHiddenLayer = 20;
            nNaN=Math.max((nnetInputLayer - 2) * EMA_STEP, RSI_SIZE);
        }
        NeuralNetwork nnet = new MultiLayerPerceptron(nnetInputLayer,
                nnetHiddenLayer, nnetOutputLayer);
        if ("TDPBackpropagation".equals(learningRule)) {
            nnet.setLearningRule(new TDPBackPropagation());
        }
        if ("momentumBackpropagation".equals(learningRule)) {
            nnet.setLearningRule(new MomentumBackpropagation());
            ((MomentumBackpropagation) nnet.getLearningRule()).setMomentum(0);
        }
        if ("backpropagation".equals(learningRule)) {
            nnet.setLearningRule(new BackPropagation());
        }
        ((LMS) nnet.getLearningRule()).setLearningRate(0.1);
        ((LMS) nnet.getLearningRule()).setMaxError(0.0001);
        ((LMS) nnet.getLearningRule()).setMaxIterations(1000);

        return nnet;
    }
    //TODO: modificare la strategia standard per utilizzare beginIdx
    private TrainingSet createTrainingSet(String strategy, List <DayBean> days, int nLearn, int beginIdx){
        Pair<Double, Double> minmax = getMinMax(days,beginIdx,nLearn);
        double min = minmax.fst;
        double max = minmax.snd;
        TrainingSet ts=null;
        if ("standard".equals(strategy)){
            ts=standardSet(days, min, max, nLearn,beginIdx);
        }
        if ("EMA".equals(strategy)){
            ts=EMASet(days, beginIdx, nLearn , min, max);
        }
        if ("RSI".equals(strategy)){
            ts=EMARSISet(days, beginIdx, nLearn , min, max);
        }
        return ts;
    }

    public void testNN(List<DayBean> days) throws FileNotFoundException, IOException {
        // creazione della rete neurale
        NeuralNetwork nnet = createNNet(NET_LEARNING_RULE, STRATEGY);
        // creazione dell'insieme di dati di addestramento
        TrainingSet trainSet = createTrainingSet(STRATEGY, days, N_LEARN,0);
         // addestramento della rete
        nnet.learnInSameThread(trainSet);

        // test errore
        double v_days[] = new double[N_TEST - nNaN];
        double v_values[] = new double[N_TEST - nNaN];
        double v_targets[] = new double[N_TEST - nNaN];

        double errors[] = new double[N_TEST-nNaN];
        double targets[] = new double[N_TEST-nNaN];
        //crea un set per eseguire dei test
        TrainingSet testSet=createTrainingSet(STRATEGY, days, N_TEST, N_LEARN);
        
        Pair<Double, Double> minmax = getMinMax(days,N_LEARN,N_TEST);
        double min=minmax.fst;
        double max=minmax.snd;
        
        for (int i = 0; i < testSet.size(); i++) {
            nnet.setInput(testSet.elementAt(i).getInput());
            nnet.calculate();
            double[] networkOutput = nnet.getOutput();
            double v1 = days.get(N_LEARN + nNaN + i).getClose();
            double v2 = Normalize.denormalize(networkOutput[0], min, max,
                    MIN_RANGE, MAX_RANGE);
            errors[i] = (Math.abs(v1 - v2) / v1);
            targets[i] = v1;

            v_days[i] = N_LEARN + nNaN + i;
            v_values[i] = 0;
            v_targets[i] = v2;
        }

        DescriptiveStatistics ds = new DescriptiveStatistics(errors);
        System.out.println("Errore medio: " + ds.getMean());

        double v[][] = new double[3][];
        v[0] = v_days;
        v[1] = v_values;
        v[2] = v_targets;
        CSVHandler.writeArray(v, "data/result_" + INDEX_FILE + ".csv");

        /*
        // test profitto
        double stocks = 0;
        double seed = 1000000;
        int currentSign = -1; // {-1, +1}
        int nextSign;
        double todayVal = 0;
        
        double v_days[] = new double[260];
        double v_values[] = new double[260];
        double v_targets[] = new double[260];
        
        for(int i = 0; i < 260; i++){
        
        //         nnet.setInput(testSet.trainingElements().get(i).getInput());
        nnet.calculate();
        double[] networkOutput = nnet.getOutput();
        //         todayVal = testSet.trainingElements().get(i)
        //                 .getInput()[NNET_INPUT_LAYER-1];
        // TODO segno uguale a zero??
        // calcolo della previsione
        double diff = Normalize.denormalize(networkOutput[0], min, max,
        MIN_RANGE, MAX_RANGE) - Normalize.denormalize(todayVal, min,
        max, MIN_RANGE, MAX_RANGE);
        //         if(Math.abs(diff)/Normalize.denormalize(todayVal, min,
        //               max, MIN_RANGE, MAX_RANGE) > 0.01){
        nextSign = (int)Math.signum(diff);
        // decisione
        if(currentSign == -1 && nextSign == 1){
        stocks = seed / Normalize.denormalize(todayVal, min, max,
        MIN_RANGE, MAX_RANGE);
        //System.out.println("giorno: "+ (N_LEARN+NNET_INPUT_LAYER+i) +
        //        " compra "+ stocks +" azioni per "+ seed + "$.");
        
        seed = 0;
        currentSign = 1;
        }else if(currentSign == 1 && nextSign == -1){
        seed = stocks * Normalize.denormalize(todayVal, min, max,
        MIN_RANGE, MAX_RANGE);
        System.out.println("giorno: "+ (N_LEARN+NNET_INPUT_LAYER+i) +
        " vendi "+ stocks +" azioni per "+ seed + "$.");
        stocks = 0;
        currentSign = -1;
        }
        v_days[i] = N_LEARN + NNET_INPUT_LAYER + i;
        v_values[i] = seed;
        v_targets[i] = Normalize.denormalize(networkOutput[0], min, max, MIN_RANGE, MAX_RANGE);
        //         }
        }
        
        double v[][] = new double[3][];
        v[0] = v_days;
        v[1] = v_values;
        v[2] = v_targets;
        CSVHandler.writeArray(v, "data/result_" + INDEX_FILE + ".csv");
        
        System.out.println("Ricavo finale: " + (seed + stocks *
        Normalize.denormalize(todayVal, min, max, MIN_RANGE, MAX_RANGE)));
        //      System.out.println("c1: "+((TDPBackPropagation)nnet.getLearningRule()).c1);
        //      System.out.println("c2: "+((TDPBackPropagation)nnet.getLearningRule()).c2);
        //      System.out.println("c3: "+((TDPBackPropagation)nnet.getLearningRule()).c3);
        //      System.out.println("c4: "+((TDPBackPropagation)nnet.getLearningRule()).c4);
        //      System.out.println("cplus: "+((TDPBackPropagation)nnet.getLearningRule()).cplus);
         */
    }

    private TrainingSet standardSet(List<DayBean> days, double min, double max, int nLearn, int beginIdx) {
        TrainingSet trainSet = new TrainingSet();

        // ciclo su training sets
        for (int i = beginIdx; i < beginIdx+nLearn; i++) {
            double v[] = new double[nnetInputLayer];
            // ciclo sul singolo input
            for (int j = 0; j < nnetInputLayer; j++) {
                v[j] = Normalize.normalize(days.get(i + j).getClose(), min, max,
                        MIN_RANGE, MAX_RANGE);
            }
            // aggiungi l'input e l'output
            trainSet.addElement(new SupervisedTrainingElement(
                    v, new double[]{
                        Normalize.normalize(days.get(i + nnetInputLayer).getClose(),
                        min, max, MIN_RANGE, MAX_RANGE)}));
        }
        return trainSet;
    }

    private TrainingSet EMASet(List<DayBean> days, int begin, int nel,
            double min, double max) {
        TrainingSet trainSet = new TrainingSet();
        // crea il vettore di calori di chiusura
        double closeVals[] = new double[nel];
        for (int i = 0; i < nel; i++) {
            closeVals[i] = Normalize.normalize(days.get(begin + i).getClose(), min,
                    max, MIN_RANGE, MAX_RANGE);
        }


        double valsEMA[][] = new double[nnetInputLayer - 1][];
        for (int i = 0; i < nnetInputLayer - 1; i++) {
            valsEMA[i] = Financial.EMA(closeVals, EMA_STEP * (i + 1));
        }

        for (int i = 0; i < nel - ((nnetInputLayer - 1) * EMA_STEP); i++) {
            double v[] = new double[nnetInputLayer];
            int idx = i + (nnetInputLayer - 1) * EMA_STEP - 1;
            v[0] = closeVals[idx];
            for (int j = 1; j < nnetInputLayer; j++) {
                v[j] = valsEMA[j - 1][idx];
            }

            // aggiungi l'input e l'output
            trainSet.addElement(new SupervisedTrainingElement(
                    v, new double[]{closeVals[idx + 1]}));
        }

        return trainSet;
    }
    
    private Pair<Double, Double> getMinMax(List<DayBean> days, int beginIdx, int  nel) {
        double max = 0;
        double min = Double.MAX_VALUE;
        for (int i = beginIdx; i < beginIdx+nel; i++) {
            double x = days.get(i).getClose();
            if (x < min) {
                min = x;
            }
            if (x > max) {
                max = x;
            }
        }
        return new Pair<Double, Double>(min, max);
    }

    private TrainingSet EMARSISet(List<DayBean> days, int begin, int nel,
            double min, double max) {
        TrainingSet trainSet = new TrainingSet();
        // crea il vettore di valori di chiusura
        double closeVals[] = new double[nel];
        for (int i = 0; i < nel; i++) {
            closeVals[i] = Normalize.normalize(days.get(begin + i).getClose(), min,
                    max, MIN_RANGE, MAX_RANGE);
        }
        double valsRSI[] = Financial.RSI(closeVals, RSI_SIZE);

        double valsEMA[][] = new double[nnetInputLayer - 2][];
        for (int i = 0; i < nnetInputLayer - 2; i++) {
            valsEMA[i] = Financial.EMA(closeVals, EMA_STEP * (i + 1));
        }

        for (int i = 0; i < nel - nNaN; i++) {
            double v[] = new double[nnetInputLayer];
            int idx = i + nNaN - 1;
            v[0] = closeVals[idx];
            v[1] = valsRSI[idx];
            for (int j = 2; j < nnetInputLayer; j++) {
                v[j] = valsEMA[j - 2][idx];
            }

            // aggiungi l'input e l'output
            trainSet.addElement(new SupervisedTrainingElement(
                    v, new double[]{closeVals[idx + 1]}));
        }

        return trainSet;
    }
}
