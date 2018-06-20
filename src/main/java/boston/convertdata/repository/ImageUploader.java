package boston.convertdata.repository;

import lombok.extern.log4j.Log4j2;
import lombok.val;
import okhttp3.*;

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

        // 上传图片链接
        val url = HttpUrl.parse(uploadBaseUrl).newBuilder().addPathSegment(filename).addQueryParameter("token", uploadToken).build();
        Request request = new Request.Builder()
                .url(url)
                .put(requestBody)
                .build();
        log.info("upload video to: " + url);

        Response response = httpClient.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("upload failed. " + response.toString());
        }
        log.info("upload succeeded. ");

        // 返回查看图片链接
        return HttpUrl.parse(playbackUrl).newBuilder().addPathSegment(filename).toString();
    }

}
