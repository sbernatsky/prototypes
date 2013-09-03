package test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.HdrHistogram.HistogramData;
import org.HdrHistogram.HistogramIterationValue;

import proto.histo.hdr.HdrRestApiImpl;

import org.apache.http.client.params.AllClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.log4j.Logger;
import proto.histo.http.RestApiImpl;
import test.histo.Command;

public class Histo {
    private static final Logger LOG = Logger.getLogger(Histo.class);
    private static final int SIZE = 32;
    private static final int COUNT = 10000;

    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(SIZE);
        DefaultHttpClient httpClient = createHttpClient();

        int[] results = new int[COUNT];
        long[] times = new long[COUNT];
        AtomicInteger counter = new AtomicInteger();
        RestApiImpl caller = new RestApiImpl(httpClient);
        HdrRestApiImpl hdrCaller = new HdrRestApiImpl(caller);

        Runnable cmd = new Command(hdrCaller, results, times, counter);
        for (int i = 0; i < SIZE; i++) {
            pool.execute(cmd);
        }

        pool.shutdown();
        pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

        LOG.warn("### RESULTS ###");
        Map<Integer, Integer> resultGrouping = organize(results);
        for (Entry<Integer, Integer> entry : resultGrouping.entrySet()) {
            LOG.warn(String.format("%s -> %s", entry.getKey(), entry.getValue()));
        }
        LOG.warn("#################################################################");

        LOG.warn("### TIMES ###");
        Map<Long, Integer> timesGrouping = organize(times);
        ArrayList<Long> t = new ArrayList<Long>(timesGrouping.keySet());
        Collections.sort(t);
        for (Long l : t) {
            LOG.warn(String.format("%s -> %s", l, timesGrouping.get(l)));
        }
        LOG.warn("#################################################################");

        LOG.warn("### HDR ###");
        timesGrouping = organize(hdrCaller.getHistogramData());
        t = new ArrayList<Long>(timesGrouping.keySet());
        Collections.sort(t);
        for (Long l : t) {
            LOG.warn(String.format("%s -> %s", l, timesGrouping.get(l)));
        }
        LOG.warn("#################################################################");
    }

    private static Map<Long, Integer> organize(HistogramData histogramData) {
        Map<Long, Integer> result = new HashMap<Long, Integer>();
        for (HistogramIterationValue value : histogramData.allValues()) {
            if (value.getCountAtValueIteratedTo() == 0L) {
                continue;
            }
            long cur = (value.getValueIteratedTo() / (1000000L * 5L)) * 5L;
            Integer v = result.get(cur);
            if (v == null) {
                v = 0;
            }
            result.put(cur, v + 1);
        }
        return result;
    }

    private static Map<Integer, Integer> organize(int[] results) {
        Arrays.sort(results);

        Map<Integer, Integer> result = new HashMap<Integer, Integer>();
        result.put(results[0], 1);
        int prev = results[0];
        for (int i = 1; i < results.length; i++) {
            if(results[i] != prev) {
                result.put(results[i], 1);
            } else {
                result.put(results[i], result.get(results[i]) + 1);
            }
            prev = results[i];
        }
        return result;
    }

    private static Map<Long, Integer> organize(long[] results) {
        Arrays.sort(results);

        Map<Long, Integer> result = new HashMap<Long, Integer>();
        long prev = (results[0]/ (1000000L * 5L)) * 5L;
        result.put(prev, 1);
        for (int i = 1; i < results.length; i++) {
            long cur = (results[i] / (1000000L * 5L)) * 5L;
            if(cur != prev) {
                result.put(cur, 1);
            } else {
                result.put(cur, result.get(cur) + 1);
            }
            prev = cur;
        }
        return result;
    }

    private static DefaultHttpClient createHttpClient() {
        BasicHttpParams params = new BasicHttpParams();
        params.setParameter(AllClientPNames.SO_TIMEOUT, 10000);
        params.setParameter(AllClientPNames.CONNECTION_TIMEOUT, 10000);
        params.setParameter(AllClientPNames.HTTP_CONTENT_CHARSET, "UTF-8");
        params.setParameter(AllClientPNames.STALE_CONNECTION_CHECK, true);

        PoolingClientConnectionManager cm = new PoolingClientConnectionManager();
        cm.setDefaultMaxPerRoute(SIZE + 10);
        cm.setMaxTotal(SIZE + 10);

        return new DefaultHttpClient(cm, params);
    }
}
