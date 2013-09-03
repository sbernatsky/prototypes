package proto.histo.hdr;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.HdrHistogram.AbstractHistogram;
import org.HdrHistogram.AtomicHistogram;
import org.HdrHistogram.HistogramData;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import proto.histo.CompositeRestApi;
import proto.histo.RestApi;

public class UrlHdrRestApiImpl extends CompositeRestApi {
    private final ConcurrentMap<String, AbstractHistogram> histograms = new ConcurrentHashMap<String, AbstractHistogram>(); 

    public UrlHdrRestApiImpl(RestApi parent) {
        super(parent);
    }

    @Override
    public <T> T call(HttpUriRequest request, ResponseHandler<T> responseHandler) throws IOException {
        long start = System.nanoTime();
        try {
            return super.call(request, responseHandler);
        } finally {
            long end = System.nanoTime();
            AbstractHistogram histogram = getHistogramData(request);
            histogram.recordValue(end - start);
        }
    }

    private AbstractHistogram getHistogramData(HttpUriRequest request) {
        String key = new StringBuilder(128)
            .append(request.getMethod())
            .append(':')
            .append(request.getURI())
            .toString();
        AbstractHistogram histogram = histograms.get(key);
        if (histogram == null) {
            histogram = new AtomicHistogram(100L * 1000L * 1000000L, 5);
            AbstractHistogram old = histograms.putIfAbsent(key, histogram);
            if (old != null) {
                histogram = old;
            }
        }
        return histogram;
    }

    public Map<String, HistogramData> getHistogramData() {
        Map<String, HistogramData> result = new HashMap<String, HistogramData>();
        for (Entry<String, AbstractHistogram> entry : histograms.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getHistogramData());
        }
        return result;
    }
}
