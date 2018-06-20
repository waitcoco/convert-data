package boston.convertdata.repository;

import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import okhttp3.*;

import java.io.*;

@Log4j2
public class ImageVectorRepository {

    private final OkHttpClient httpClient = new OkHttpClient();
    private final String carUrl;
    private final String personUrl;
    private final String cachePath;

    public ImageVectorRepository(String carVectorUrl, String personVectorUrl, String cachePath) {
        this.carUrl = carVectorUrl;
        this.personUrl = personVectorUrl;
        this.cachePath = cachePath;
    }

    public double[] getImageVector(String objectType, String filename, byte[] imageBytes) throws IOException {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", filename, RequestBody.create(MediaType.parse("application/octet-stream"), imageBytes))
                .build();

        String url = objectType.equals("car") ? carUrl : personUrl;

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        log.info("Request vector from: " + url);

        Response response = httpClient.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("Request vector failed. " + response.toString());
        }
        log.info("Request vector succeeded. ");

        return new Gson().fromJson(response.body() != null ? response.body().string() : null, double[].class);
    }

    public File saveVectorForOneImage(double[] vectors, String filename, String segmentId) throws IOException {
        // 要写入的字符串
        StringBuilder sb = new StringBuilder(segmentId);
        sb.append(" ");
        for (double vector : vectors) {
            sb.append(vector);
            sb.append(" ");
        }
        sb.append("\r\n");

        File file = new File(cachePath + "/" + filename);
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
            log.info("Vector save to file succeeded.");
        }
        return file;
    }

}
