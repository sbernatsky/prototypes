package test.histo;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.log4j.Logger;
import proto.histo.RestApi;

public class Command implements Runnable {
    private static final Logger LOG = Logger.getLogger(Command.class);
    private static final ResponseHandler<Integer> HANDLER = new ResponseHandler<Integer>() {
        @Override public Integer handleResponse(HttpResponse response) {
            return response.getStatusLine().getStatusCode();
        }
    };

    private final int[] results;
    private final long[] times;
    private final AtomicInteger counter;
    private final RestApi caller;

    public Command(RestApi caller, int[] results, long[] times, AtomicInteger counter) {
        this.caller = caller;
        this.results = results;
        this.times = times;
        this.counter = counter;
    }

    @Override
    public void run() {
        LOG.info(String.format("task started on %s thread", Thread.currentThread()));
        int count = 0;
        int idx;
        while ((idx = counter.getAndIncrement()) < times.length) {
            long start = System.nanoTime();
            try {
                HttpPost request = new HttpPost("https://id01.dev.ebuddy-office.net/api/123/registerAuthorize/client-generated.json");
                results[idx] = caller.call(request, HANDLER);
            } catch (IOException e) {
                LOG.error("call failed: " + e);
                results[idx] = -1;
            }
            long end = System.nanoTime();
            times[idx] = end - start;
            count++;
            if (count % (times.length/500) == 0) {
                LOG.info(String.format("task on %s thread executed calls: %s", Thread.currentThread(), count));
            }
        }
        LOG.info(String.format("task finished on %s thread; number of calls: %s", Thread.currentThread(), count));
    }

}
