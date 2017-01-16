package bgu.cbs.reactivePull.Impl.SubConscience;

import bgu.cbs.reactivePull.SubConscience.SubConscience;
import bgu.cbs.reactivePull.memory.MemoryPull;

import java.util.Map;
import java.util.Random;

/**
 * Pulls context requests by selecting a single word from a pull.
 * The selection is done using the distribution of scores
 */
public class DistributionWalkMSConceptsSubConscience implements SubConscience<String> {
    private MemoryPull<Map<String, Double>, String> memory;
    private static final long WAIT_TIME = 10000;
    private final Random random;
    private String word;

    public DistributionWalkMSConceptsSubConscience(MemoryPull<Map<String, Double>, String> memory, Random random) {
        this.random = random;
        this.memory = memory;
        word = null;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(WAIT_TIME);
                updateSubconscience(word);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }


    @Override
    public void updateSubconscience(String s) {
        if (word == null) {
            // Start the walk from a random cache selection
            Map<String,Double> cacheImage = memory.getCache();
            if (cacheImage != null && cacheImage.size() > 0) {
                word = selectRandom(cacheImage);
            }
        } else {
            word = selectRandom(memory.getByConnection(word));
        }
    }

    private String selectRandom(Map<String, Double> conceptMap) {
/*        double sum = 0.0;
        for (Double val : conceptMap.values()) {
            sum += val;
        }
        double selection = sum * random.nextDouble();
        sum = 0.0;
        for (Map.Entry<String, Double> entry : conceptMap.entrySet()) {
            sum += entry.getValue();
            if (sum >= selection) {
                return entry.getKey();
            }
        }
        return null;
*/      return conceptMap.keySet().toArray(new String[1])[random.nextInt(10)];
    }
}
