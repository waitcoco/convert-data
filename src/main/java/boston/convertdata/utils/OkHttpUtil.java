package boston.convertdata.utils;

import okhttp3.*;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;

public class OkHttpUtil {
    static private OkHttpClient client = new OkHttpClient();
    static private MediaType JSON = MediaType.parse("application/json");

    static private String getResult(Request request) {
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new RuntimeException(" Response code:" + response.code() + "Response body" + response.body().string());
            }
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // TODO: DEFAULT / LOWERCASE
    static public <T> T getResponseBodyStringWithPost(URIBuilder uriBuilder, Object body, Class<T> tClass) {
        Request request = new Request.Builder()
                .url(uriBuilder.toString())
                .post(RequestBody.create(JSON, GsonInstances.DEFAULT.toJson(body)))
                .build();
        return GsonInstances.DEFAULT.fromJson(getResult(request), tClass);
    }
}