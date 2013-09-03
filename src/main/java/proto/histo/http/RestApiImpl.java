package proto.histo.http;

import java.io.IOException;

import org.apache.http.client.HttpClient;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;

import proto.histo.RestApi;

public class RestApiImpl implements RestApi {
    private final HttpClient httpClient;

    public RestApiImpl(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public <T> T call(HttpUriRequest request, ResponseHandler<T> responseHandler) throws IOException {
        return httpClient.execute(request, responseHandler);
    }

}
