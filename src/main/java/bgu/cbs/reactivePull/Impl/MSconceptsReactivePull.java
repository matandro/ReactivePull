package bgu.cbs.reactivePull.Impl;

import bgu.cbs.reactivePull.Conscience.Conscience;
import bgu.cbs.reactivePull.Impl.Conscience.MCConceptsDistributionConscience;
import bgu.cbs.reactivePull.Impl.Conscience.MCConceptsMaxConscience;
import bgu.cbs.reactivePull.Impl.SubConscience.MSconceptsSubConscienceFactory;
import bgu.cbs.reactivePull.SubConscience.SubConscienceFactory;
import bgu.cbs.reactivePull.memory.MCconceptsAPIPull;
import bgu.cbs.reactivePull.memory.MemoryPull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by matan on 1/2/2017.
 */
public class MSconceptsReactivePull {
    public static void main (String[] args) {
        // Setup memory pool
        MemoryPull<Map<String,Double>, String> memory = null;
        int noThreads = -1;
        String subConscinceType = null;
        String ptypeStr = null;
        try {
            ptypeStr = args[0];
            noThreads = Integer.valueOf(args[1]);
            subConscinceType = args[2];
            memory = new MCconceptsAPIPull(ptypeStr);
        } catch (Exception e) {
            use();
            System.exit(-1);
        }

        // init conscience
        Conscience<String, String> decisionMaker = new MCConceptsMaxConscience(memory);//new MCConceptsDistributionConscience(memory);

        // Init sub consciences
        SubConscienceFactory<String> subConscienceFactory = new MSconceptsSubConscienceFactory(memory);
        ExecutorService executor = null;
        if (noThreads > 0)
            executor = Executors.newFixedThreadPool(noThreads);
        for (int i = 0 ; i < noThreads ; ++i) {
            executor.submit(subConscienceFactory.makeSubConscience(subConscinceType));
        }

        // Run program
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in));) {
            String line;
            while ((line = br.readLine()) != null) {
                if ("exit".equals(line)) {
                    System.out.println("Exiting...");
                    if (executor != null) {
                        executor.shutdownNow();
                        executor.awaitTermination(5, TimeUnit.SECONDS);
                    }
                    break;
                }
                System.out.println(decisionMaker.MakeDecision(line));
            }
        } catch (IOException e) {
            System.err.println("IOException: " + e);
        } catch (InterruptedException ie) {
            System.err.println("Failed to wait for threads to shutdown");
        }
    }

    private static void use() {
        System.out.println("Run arguments: <mc concept selection type> <No of sub consciences threads> <Sub conscience type>");

        System.out.print("mc concept selection type: ");
        String[] ptypeLst = MCconceptsAPIPull.Ptype.getPtypeNames();
        for (int i = 0 ; i < ptypeLst.length ; ++i) {
            System.out.print(ptypeLst[i]);
            if (i + 1 < ptypeLst.length) {
                System.out.print(", ");
            }
        }
        System.out.println();

        System.out.println("Sub conscience types: ");
    }
}
