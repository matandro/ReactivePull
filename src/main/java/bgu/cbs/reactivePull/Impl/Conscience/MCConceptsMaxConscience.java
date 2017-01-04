package bgu.cbs.reactivePull.Impl.Conscience;

import bgu.cbs.reactivePull.Conscience.Conscience;
import bgu.cbs.reactivePull.Impl.LogSingleton;
import bgu.cbs.reactivePull.memory.MemoryPull;

import java.util.Map;

/**
 * Created by matan on 1/2/2017.
 */
public class MCConceptsMaxConscience implements Conscience<String, String> {
    private MemoryPull<Map<String, Double>, String> memory;

    public MCConceptsMaxConscience(MemoryPull<Map<String, Double>, String> memory) {
        this.memory = memory;
    }

    @Override
    public String MakeDecision(String t) {
        double max = 0.0;
        String res = null;
        for (Map.Entry<String, Double> entry : memory.getByConnection(t).entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                res = entry.getKey();
            }
        }
        LogSingleton.getInstance().println("Selected res=" + res);
        return res;
    }
}
