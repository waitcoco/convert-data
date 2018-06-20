package boston.convertdata.repository;

import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import okhttp3.*;

import java.io.*;
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
        // 要写入的字符串
        StringBuilder sb = new StringBuilder();
        for (String segmentId : vectorMap.keySet()) {
            sb.append(segmentId);
            sb.append(" ");
            for (double vector : vectorMap.get(segmentId)) {
                sb.append(vector);
                sb.append(" ");
            }
            sb.append("\r\n");
        }

        // 写入文件
        File file = new File(cachePath + "/" + videoId + ".text.temp"); // videoId作文件名, .temp避免正在写入时被扫描
        FileOutputStream fos;
        if (!file.exists()) {
            log.info("Create new file.");
            file.createNewFile();
            fos = new FileOutputStream(file);
        } else {
            log.info("File already exists, append to the end.");
            fos = new FileOutputStream(file, true);
        }
        try (OutputStreamWriter osw = new OutputStreamWriter(fos)) {
            osw.write(sb.toString());
        }

        // 写完后, 将文件名最后.temp去掉
        int end = file.getAbsolutePath().length() - 5;
        file.renameTo(new File(file.getAbsolutePath().substring(0, end))); // 移除.temp

        log.info("Vector save to file succeeded.");
    }

}
