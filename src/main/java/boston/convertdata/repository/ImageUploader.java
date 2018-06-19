package boston.convertdata.repository;

import lombok.extern.log4j.Log4j2;
import okhttp3.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Log4j2
public class ImageUploader {

    private final OkHttpClient httpClient;
    private final String uploadBaseUrl;
    private final String uploadToken;
    private final String playbackUrl;

    public ImageUploader(String uploadBaseUrl, String uploadToken, String playbackUrl) {
        this.uploadBaseUrl = uploadBaseUrl;
        this.uploadToken = uploadToken;
        this.playbackUrl = playbackUrl;
        this.httpClient = new OkHttpClient();
    }

    // byte[] multipart-formdata 直接上传byte数组
    public String uploadImage(byte[] bytes, String filename) throws IOException {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", filename, RequestBody.create(MediaType.parse("application/octet-stream"), bytes))
                .build();

        Request request = new Request.Builder()
                .url(UriComponentsBuilder.fromHttpUrl(uploadBaseUrl).path("/").path(filename).queryParam("token", uploadToken).toUriString())
                .put(requestBody)
                .build();

        log.info("upload video to: " + UriComponentsBuilder.fromHttpUrl(uploadBaseUrl).path("/").path(filename).queryParam("token", uploadToken).toUriString());

        Response response = httpClient.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("upload failed. " + response.toString());
        }

        log.info("upload succeeded. ");

        return UriComponentsBuilder.fromHttpUrl(playbackUrl).path("/").path(filename).toUriString();
    }


}
