package proto.histo;

import java.io.IOException;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;

import proto.histo.RestApi;

public abstract class CompositeRestApi implements RestApi {
    private final RestApi parent;

    protected CompositeRestApi(RestApi parent) {
        this.parent = parent;
    }

    @Override
    public <T> T call(HttpUriRequest request, ResponseHandler<T> responseHandler) throws IOException {
        return parent.call(request, responseHandler);
    }

}
