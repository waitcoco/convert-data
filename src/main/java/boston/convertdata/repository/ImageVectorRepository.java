package boston.convertdata.repository;

import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import okhttp3.*;

import java.io.*;
import java.nio.file.Paths;
import java.util.Map;

@Log4j2
public class ImageVectorRepository {

    private final OkHttpClient httpClient = new OkHttpClient();
    private final String vectorUrl;
    private final String cachePath;

    public ImageVectorRepository(String vectorUrl, String cachePath) {
        this.vectorUrl = vectorUrl;
        this.cachePath = cachePath;
    }

    public double[] getImageVector(String filename, byte[] imageBytes) throws IOException {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", filename, RequestBody.create(MediaType.parse("application/octet-stream"), imageBytes))
                .build();

        Request request = new Request.Builder()
                .url(vectorUrl)
                .post(requestBody)
                .build();
        log.info("Request vector from: " + vectorUrl);

        Response response = httpClient.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("Request vector failed. " + response.toString());
        }
        log.info("Request vector succeeded. ");

        return new Gson().fromJson(response.body() != null ? response.body().string() : null, double[].class);
    }

    public void saveVectorToFile(Map<String, double[]> vectorMap, String videoId) throws IOException {
        // 写入文件
        File file = new File(Paths.get(cachePath, videoId + ".txt.tmp").toString()); // videoId作文件名, .tmp避免正在写入时被扫描
        try (PrintWriter out = new PrintWriter(Paths.get(cachePath, videoId + ".txt.tmp").toString())) {
            for (String segmentId : vectorMap.keySet()) {
                out.write(segmentId);
                for (double vector : vectorMap.get(segmentId)) {
                    out.write(" " + vector);
                }
                out.write("\n");
            }
        }
        // 写完后, 将文件名最后.tmp去掉
        file.renameTo(new File(Paths.get(cachePath, videoId + ".txt").toString())); // 移除.tmp
        log.info("Vector save to file succeeded.");
    }

}
