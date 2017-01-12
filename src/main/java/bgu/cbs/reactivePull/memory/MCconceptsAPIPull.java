package bgu.cbs.reactivePull.memory;

import bgu.cbs.reactivePull.Impl.LogSingleton;
import bgu.cbs.reactivePull.Impl.misc.Pair;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by matan on 1/2/2017.
 */
public class MCconceptsAPIPull implements MemoryPull<Map<String, Double>, String> {
    /*
       API-Key MS concepts
       Key: rZYTT0NcghlJ8zAGPHf5hv02VFhlx4rL
       Email: matandro@post.bgu.ac.il
       Name: Matan Drory Retwitzer
    */
    private static final String API_KEY = "rZYTT0NcghlJ8zAGPHf5hv02VFhlx4rL";
    private static final String API_ADDRESS_PREFIX = "https://concept.research.microsoft.com/api/Concept/";

    private final int TOP_K = 10;
    private final double PMIK = 1.5;
    private final double SMOOTH = 0.0;

    private Map<String, Double> memoryCache = new HashMap<>();
    private final int MAX_CACHE_SIZE = 10;

    public enum Ptype {
        PCE,
        MI,
        PEC,
        NPMI,
        PMIK,
        BLC;

        private static String[] PTYPE_LIST = {"P(c|e)", "MI", "P(e|c)", "NPMI", "PMI^K", "BLC"};

        public static final String[] getPtypeNames() {
            return PTYPE_LIST;
        }

        public static Ptype getPtypeByStr(String ptypeStr) {
            Ptype res = null;
            for (int i = 0; i < PTYPE_LIST.length; ++i) {
                if (PTYPE_LIST[i].equals(ptypeStr)) {
                    res = Ptype.values()[i];
                }
            }
            return res;
        }

        public static String getAPIPtypeName(Ptype ptype) {
            String res = null;
            switch (ptype) {
                case PCE:
                    res = "ScoreByProb";
                    break;
                case MI:
                    res = "ScoreByMI";
                    break;
                case PEC:
                    res = "ScoreByTypi";
                    break;
                case NPMI:
                    res = "ScoreByNPMI";
                    break;
                case PMIK:
                    res = "ScoreByPMIK";
                    break;
                case BLC:
                    res = "ScoreByCross";
                    break;
                default:
            }

            return res;
        }
    }

    private Ptype ptype;

    /* API
        GET api/Concept/ScoreByProb?instance={instance}&topK={topK}
        Get Score by P(c|e)
        GET api/Concept/ScoreByMI?instance={instance}&topK={topK}&smooth={smooth}
        Get Score by MI
        GET api/Concept/ScoreByTypi?instance={instance}&topK={topK}&smooth={smooth}
        Get Score by P(e|c)
        GET api/Concept/ScoreByNPMI?instance={instance}&topK={topK}&smooth={smooth}
        Get Score by NPMI
        GET api/Concept/ScoreByPMIK?instance={instance}&topK={topK}&pmiK={pmiK}&smooth={smooth}
        Get Score by PMI^K
        GET api/Concept/ScoreByCross?instance={instance}&topK={topK}&pmiK={pmiK}&smooth={smooth}
    */

    private static final String APP_JSON = "application/json";
    private static final String HEADER = "accept";
    private static final Integer RESPONSE_OK = 200;

    @Override
    public Map<String, Double> getByConnection(String input) {
        Map<String, Double> res = null;

        try {
            LogSingleton.getInstance().println("Starting pull: " + input);
            // Setup API call
            String urlStr = API_ADDRESS_PREFIX + Ptype.getAPIPtypeName(this.ptype);
            urlStr += "?" + URLEncodedUtils.format(addInfo(input), "utf8");
            URL url = new URL(urlStr);

            // Setup HTTP request
            HttpClient httpClient = HttpClients.createDefault();
            HttpGet getResuest = new HttpGet(urlStr);
            getResuest.addHeader(HEADER, APP_JSON);

            // execute web request
            HttpResponse response = httpClient.execute(getResuest);

            if (response.getStatusLine().getStatusCode() == 414) {
                System.err.println("Line too long: " + urlStr);
            } else if (response.getStatusLine().getStatusCode() != RESPONSE_OK) {
                throw new RuntimeException("Failed : HTTP error code: " + response.getStatusLine().getStatusCode() + "\nRequest: " + urlStr);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            String result = "";
            String line;
            while ((line = br.readLine()) != null) {
                result += line + "\n";
            }
            LogSingleton.getInstance().println("Pull complete: " + input);
            // Analyze json
            res = analyzeJason(result);
            LogSingleton.getInstance().println("Pool results: " + res);
            if (res != null && !res.isEmpty()) {
            // Advise Cache
                res = queryCache(res);
            }
            LogSingleton.getInstance().println("Result in context: " + res);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }

    @Override
    public synchronized Map<String, Double> getCache() {
        return new HashMap<>(memoryCache);
    }

    public synchronized Map<String, Double> queryCache(Map<String, Double> currentPull) {
        Map<String, Double> res = null;
        LogSingleton.getInstance().println("MemoryCache before: " + memoryCache);

        if (memoryCache.isEmpty()) {
            for (Map.Entry<String, Double> entry : currentPull.entrySet()) {
                memoryCache.put(entry.getKey(), entry.getValue());
            }
            res = currentPull;
        } else {
            Double newSum = 0.0;
            // Add new values and calculate sum
            for (Map.Entry<String, Double> entry : currentPull.entrySet()) {
                Double tempProb;
                Double newValue = entry.getValue();
                if ((tempProb = memoryCache.get(entry.getKey())) != null) {
                    newValue += tempProb;
                }
                newSum += newValue;
                memoryCache.put(entry.getKey(), newValue);
            }
            // get only the top MAX_CACHE_SIZE items
            memoryCache = getTopK(memoryCache, MAX_CACHE_SIZE);
            newSum = 0.0;
            for (Map.Entry<String, Double> entry : memoryCache.entrySet()) {
                newSum += entry.getValue();
            }
            // Fix memory cache to sum 1
            for (Map.Entry<String, Double> entry : memoryCache.entrySet()) {
                memoryCache.put(entry.getKey(), entry.getValue() / newSum);
            }
            // get ready results
            res = getTopK(memoryCache, TOP_K);
        }
        LogSingleton.getInstance().println("MemoryCache After: " + memoryCache);
        return res;
    }

    private Map<String, Double> getTopK(Map<String, Double> memoryCache, int amount) {
        Map<String, Double> res = new HashMap<>(amount);
        LinkedList<Pair<String, Double>> topTen = new LinkedList<>();
        // find top ten
        for (Map.Entry<String, Double> entry : memoryCache.entrySet()) {
            int i = 0;
            for (; i < topTen.size(); ++i) {
                if (entry.getValue() < topTen.get(i).getRight()) {
                    break;
                }
            }
            if (i > 0) {
                if (topTen.size() >= amount) {
                    topTen.removeFirst();
                }
                topTen.add(i - 1, new Pair<>(entry.getKey(), entry.getValue()));
            } else if (topTen.size() < amount) {
                topTen.addFirst(new Pair<>(entry.getKey(), entry.getValue()));
            }
        }
        double testSum = 0.0;
        // copy top / bottom amount
        for (Pair<String, Double> pair : topTen) {
            res.put(pair.getLeft(), pair.getRight());
            testSum += pair.getRight();
        }
        return res;
    }

    private Map<String, Double> analyzeJason(String result) {
        Type type = new TypeToken<Map<String, Double>>() {
        }.getType();
        Map<String, Double> res = null;
        Gson gson = new GsonBuilder().create();
        res = gson.fromJson(result, type);
        return res;
    }

    private List<NameValuePair> addInfo(String input) {
        List<NameValuePair> res = new ArrayList<>();
        res.add(new BasicNameValuePair("instance", input));
        res.add(new BasicNameValuePair("topK", Integer.toString(TOP_K)));
        if (ptype == Ptype.PMIK ||
                ptype == Ptype.BLC)
            res.add(new BasicNameValuePair("pmlK", Double.toString(PMIK)));
        if (ptype != Ptype.PCE)
            res.add(new BasicNameValuePair("smooth", Double.toString(SMOOTH)));
        res.add(new BasicNameValuePair("api_key", API_KEY));
        return res;
    }

    public MCconceptsAPIPull() {
        this.ptype = Ptype.PCE;
    }

    public MCconceptsAPIPull(String ptypeStr) {
        this.ptype = Ptype.getPtypeByStr(ptypeStr);
    }
}
