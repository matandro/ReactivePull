package bgu.cbs.reactivePull.Impl.Conscience;

import bgu.cbs.reactivePull.Conscience.Conscience;
import bgu.cbs.reactivePull.memory.MemoryPull;

import java.util.Map;
import java.util.Random;

/**
 * Created by matan on 1/2/2017.
 */
public class MCConceptsDistributionConscience implements Conscience<String, String> {
    private MemoryPull<Map<String, Double>, String> memory;
    private final Random random;

    public MCConceptsDistributionConscience(MemoryPull<Map<String, Double>, String> memory) {
        this.memory = memory;
        this.random = new Random();
    }

    @Override
    public String MakeDecision(String t) {
        double sum = 0.0;
        double random = this.random.nextDouble();
        String res = null;
        for (Map.Entry<String, Double> entry : memory.getByConnection(t, 1).entrySet()) {
            sum += entry.getValue();
            if (random >= sum) {
                res = entry.getKey();
            }
        }
        return res;
    }
}
