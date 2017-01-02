package bgu.cbs.reactivePull.Impl.SubConscience;

import bgu.cbs.reactivePull.SubConscience.SubConscience;
import bgu.cbs.reactivePull.memory.MemoryPull;

import java.util.Map;

/**
 * Created by matan on 1/2/2017.
 */
public class RandomWalkMSConceptsSubConscience implements SubConscience<String> {
    private MemoryPull<Map<String, Double>, String> memory;
    private static final long WAIT_TIME = 10000;
    private String word;

    public RandomWalkMSConceptsSubConscience(MemoryPull<Map<String, Double>, String> memory) {
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
            Map<String,Double> cacheImage = memory.getCache();
            if (cacheImage != null && cacheImage.size() > 0) {
                word = selectRandom(cacheImage);
            }
        } else {
            word = selectRandom(memory.getByConnection(word));
        }
    }

    private String selectRandom(Map<String, Double> conceptMap) {
        double sum = 0.0;
        for (Double val : conceptMap.values()) {
            sum += val;
        }
        double selction = sum * Math.random();
        sum = 0.0;
        for (Map.Entry<String, Double> entry : conceptMap.entrySet()) {
            sum += entry.getValue();
            if (sum < selction) {
                return entry.getKey();
            }
        }
        return null;
    }
}
