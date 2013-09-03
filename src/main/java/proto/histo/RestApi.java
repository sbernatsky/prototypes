package proto.histo;

import java.io.IOException;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;

public interface RestApi {

    /** Calls a RESTful API endpoint specified by the provided request. A response handler processes it processes a response. */
    <T> T call(HttpUriRequest request, ResponseHandler<T> responseHandler) throws IOException;

}
