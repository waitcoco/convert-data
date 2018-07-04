package boston.convertdata.repository;

import boston.convertdata.utils.GsonInstances;
import boston.convertdata.model.pipeline.Camera;
import boston.convertdata.model.pipeline.Video;
import com.google.gson.Gson;
import lombok.val;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class VideoInfoGetter {

    private final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .build();

    private final String baseUrl;

    private final Gson gson = GsonInstances.DEFAULT;

    public VideoInfoGetter(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Video getVideo(String videoId) {
        val url = HttpUrl.parse(baseUrl).newBuilder().addPathSegment("videos").addPathSegment(videoId).build();
        val request = new Request.Builder().url(url).get().build();
        return call(request, Video.class);
    }

    public Camera getCamera(String cameraId) {
        val url = HttpUrl.parse(baseUrl).newBuilder().addPathSegment("cameras").addPathSegment(cameraId).build();
        val request = new Request.Builder().url(url).get().build();
        return call(request, Camera.class);
    }

    private <T> T call(Request request, Class<T> classOfT) {
        try {
            val response = okHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new RuntimeException("failed response : " + response.toString());
            }
            return gson.fromJson(response.body().string(), classOfT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
