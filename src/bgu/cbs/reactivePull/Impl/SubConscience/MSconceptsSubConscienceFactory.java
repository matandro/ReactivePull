package bgu.cbs.reactivePull.Impl.SubConscience;

import bgu.cbs.reactivePull.SubConscience.SubConscience;
import bgu.cbs.reactivePull.SubConscience.SubConscienceFactory;
import bgu.cbs.reactivePull.memory.MemoryPull;

import java.util.Map;
import java.util.Random;

/**
 * Created by matan on 1/2/2017.
 */
public class MSconceptsSubConscienceFactory implements SubConscienceFactory<String> {
    private MemoryPull<Map<String,Double>,String> memory;
    private Random myRandom = new Random();

    public MSconceptsSubConscienceFactory(MemoryPull<Map<String,Double>,String> memory) {
        this.memory = memory;

    }

    @Override
    public SubConscience makeSubConscience(String selectionMethod) {
        SubConscience<String> subConscience = new DistributionWalkMSConceptsSubConscience(memory, myRandom);
        return subConscience;
    }
}
