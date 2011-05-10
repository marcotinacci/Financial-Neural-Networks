package strategies;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.learning.SupervisedTrainingElement;

/**
 * Interfaccia della strategia che deve essere utilizzata per creare ed
 * addestrare la rete neurale.
 * Questa interfaccia è la parte centrale del design pattern Strategy che
 * viene utilizzato.
 * @author Marco Tinacci
 */
public interface StrategyInterface {
   /**
    * Metodo che addestra una rete neurale sui dati nell'intervallo
    * [beginIdx, todayIdx]
    * @param beginIdx indice giorno di inizio
    * @param todayIdx indice giorno attuale
    * @return rete neurale addestrata
    */
   public NeuralNetwork getTrainedNeuralNetwork(int beginIdx, int todayIdx);

   /**
    * Ritorna gli input della rete e i target rispetto al giorno e alla
    * strategia specificata
    * @param todayIdx indice del giorno
    * @return input e target incapsulati in un SupervisedTrainingElement
    */
   public SupervisedTrainingElement getTestElement(int todayIdx);

   /**
    * Metodo che denormalizza un valore di chiusura rispetto alla strategia
    * utilizzata
    * @param closeValue valore di chiusura normalizzato
    * @return valore di chiusura denormalizzato
    */
   public double getDenormalizedClose(double closeValue);

   /**
    * Metodo che normalizza un valore di chiusura rispetto alla strategia
    * utilizzata
    * @param closeValue valore di chiusura denormalizzato
    * @return valore di chiusura normalizzato
    */
   public double getNormalizedClose(double closeValue);
}
