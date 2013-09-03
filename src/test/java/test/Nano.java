package test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.HdrHistogram.AbstractHistogram;
import org.HdrHistogram.HistogramData;
import org.HdrHistogram.IntHistogram;
import org.apache.log4j.Logger;

import com.affinity.CPU;
import com.affinity.ThreadAffinityUtils;

public class Nano {
    private static final Logger LOG = Logger.getLogger(Nano.class);
    private static final int COUNT = 100000000;
    private static final double[] PERCENTILES = {99.0d, 99.9d, 99.99d, 99.999d};

    public static void main(String[] args) throws Exception {
        CPU cpu = ThreadAffinityUtils.defaultLayoutService().cpu(1);
        ThreadAffinityUtils.defaultAffinityService().restrictCurrentThreadTo(cpu);

        long[] times = new long[COUNT];

        int j = 0;
        while(true) {
            LOG.warn("### COMPUTING (" + j + ") ###");
            for (int i = 0; i < times.length; i++) {
                times[i] = System.nanoTime();
            }
            j++;

            LOG.warn("### DELTAS ###");
            AbstractHistogram histogram = convert(times);
            HistogramData data = histogram.getHistogramData();
            LOG.warn(String.format("min val: %9d ns (%s op)", data.getMinValue(), data.getCountBetweenValues(0L, data.getMinValue())));
            LOG.warn(String.format("mean   : %9.5f ns", data.getMean()));
            for (int i = 0; i < PERCENTILES.length; i++) {
                double p = PERCENTILES[i];
                long v = data.getValueAtPercentile(p);
                LOG.warn(String.format("%6.3f%%: %9d ns (%s op)", p, v, data.getCountBetweenValues(0L, v)));
            }
            LOG.warn(String.format("max val: %9d ns (%s op)", data.getMaxValue(), data.getTotalCount()));
            LOG.warn("#################################################################");
//            Map<Long, Integer> deltasGrouping = organizeToDeltas(times);
//            dump(deltasGrouping);
//            LOG.warn("#################################################################");
        }
//        LOG.warn("### INVOCATION TIMES ###");
//        Map<Long, Integer> grouping = organizeToInvcations(times);
//        dump(grouping);
//        LOG.warn("#################################################################");
    }

    private static void dump(Map<Long, Integer> grouping) {
        final int _100_limit = grouping.remove(Long.MIN_VALUE);
        final int _99_limit = (int) (_100_limit * 0.99d);
        final int _999_limit = (int) (_100_limit * 0.999d);
        final int _9999_limit = (int) (_100_limit * 0.9999d);
        final int _99999_limit = (int) (_100_limit * 0.99999d);
        ArrayList<Long> t = new ArrayList<Long>(grouping.keySet());
        Collections.sort(t);
        int count = 0;
        long _99pt = 0L, _999pt = 0L, _9999pt = 0L, _99999pt = 0L;
        for (Long l : t) {
//            LOG.warn(String.format("%s -> %s", l, grouping.get(l)));
            int old = count;
            count += grouping.get(l);

            if (old < _99_limit && count >= (_99_limit)) {
                _99pt = l;
            }
            if (old < _999_limit && count >= (_999_limit)) {
                _999pt = l;
            }
            if (old < _9999_limit && count >= (_9999_limit)) {
                _9999pt = l;
            }
            if (old < _99999_limit && count >= (_99999_limit)) {
                _99999pt = l;
            }
        }
//        LOG.warn("#################################################################");
        LOG.warn("    99%: " + _99pt + " ns (" + _99_limit + " op)");
        LOG.warn("  99.9%: " + _999pt + " ns (" + _999_limit + " op)");
        LOG.warn(" 99.99%: " + _9999pt + " ns (" + _9999_limit + " op)");
        LOG.warn("99.999%: " + _99999pt + " ns (" + _99999_limit + " op)");
        LOG.warn("100.00%: " + t.get(t.size() -1) + " ns (" + _100_limit + " op)");
    }

    /** Delta -> number of occurences */
    private static Map<Long, Integer> organizeToDeltas(long[] times) {
        int total = 0;
        Map<Long, Integer> result = new HashMap<Long, Integer>();
        long prev = times[0];
        for (int i = 1; i < times.length; i++) {
            if (times[i] != prev) {
                long delta = (times[i] - prev);
                Integer cnt = result.get(delta);
                if(cnt == null) {
                    cnt = 0;
                }
                result.put(delta, cnt + 1);
                total++;

                prev = times[i];
            }
        }
        result.put(Long.MIN_VALUE, total);
        return result;
    }

    /** Single invocation time -> number of occurences */
    private static Map<Long, Integer> organizeToInvcations(long[] times) {
        int total = 0;
        Map<Long, Integer> result = new HashMap<Long, Integer>();
        long prev = (times[0]);
        int idx = 0;
        for (int i = 1; i < times.length; i++) {
            if (times[i] != prev) {
                long time = ((times[i] - prev) / (i - idx));

                Integer cnt = result.get(time);
                if(cnt == null) {
                    cnt = 0;
                }
                result.put(time, cnt + 1);
                total++;

                prev = times[i];
                idx = i;
            }
        }
        result.put(Long.MIN_VALUE, total);
        return result;
    }

    /** Delta -> number of occurences */
    private static AbstractHistogram convert(long[] times) {
        // A Histogram covering the range from 1 nsec to 0x1000000 nsec with 1 decimal point resolution:
        IntHistogram histogram = new IntHistogram(0x1000000L, 1);
        //LOG.warn(String.format("histo highest value:       %s", histogram.getHighestTrackableValue()));
        //LOG.warn(String.format("histo estimated footprint: %s", histogram.getEstimatedFootprintInBytes()));
        for (int i = 1; i < times.length; i++) {
            long delta = (times[i] - times[i - 1]);
            if (delta == 0L) {
                continue;
            }

            try {
                histogram.recordValue(delta);
            } catch (RuntimeException e) {
                LOG.error("failed to add time: " + delta + " (" + times[i - 1] + ";" + times[i] + "); highest: " + histogram.getHighestTrackableValue());
//                throw e;
            }
        }
        return histogram;
    }
}
