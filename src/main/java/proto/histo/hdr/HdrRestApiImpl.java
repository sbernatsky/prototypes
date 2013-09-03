package proto.histo.hdr;

import java.io.IOException;

import org.HdrHistogram.AtomicHistogram;
import org.HdrHistogram.HistogramData;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import proto.histo.CompositeRestApi;
import proto.histo.RestApi;

public class HdrRestApiImpl extends CompositeRestApi {
    private final AtomicHistogram histogram = new AtomicHistogram(100L*1000L*1000000L, 5);

    public HdrRestApiImpl(RestApi parent) {
        super(parent);
    }

    @Override
    public <T> T call(HttpUriRequest request, ResponseHandler<T> responseHandler) throws IOException {
        long start = System.nanoTime();
        try {
            return super.call(request, responseHandler);
        } finally {
            long end = System.nanoTime();
            histogram.recordValue(end - start);
        }
    }

    public HistogramData getHistogramData() {
        return histogram.getHistogramData();
    }
}
