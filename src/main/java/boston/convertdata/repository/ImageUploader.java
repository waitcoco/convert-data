package boston.convertdata.repository;

import lombok.extern.log4j.Log4j2;
import okhttp3.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

@Log4j2
public class ImageUploader {

    private final OkHttpClient httpClient;
    private final String uploadBaseUrl;
    private final String uploadToken;
    private final String cachePath;
    private final String playbackUrl;

    public ImageUploader(String uploadBaseUrl, String uploadToken, String cachePath, String playbackUrl) {
        this.uploadBaseUrl = uploadBaseUrl;
        this.uploadToken = uploadToken;
        this.cachePath = cachePath;
        this.playbackUrl = playbackUrl;
        this.httpClient = new OkHttpClient();
    }

    // byte[] multipart-formdata 直接上传byte数组
    public String uploadImage(File file) throws IOException {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), file))
                .build();

        Request request = new Request.Builder()
                // 处理为jpeg格式上传
                .url(UriComponentsBuilder.fromHttpUrl(uploadBaseUrl).path("/").path(file.getName() + ".jpeg").queryParam("token", uploadToken).toUriString())
                .put(requestBody)
                .build();

        log.info("upload video to: " + UriComponentsBuilder.fromHttpUrl(uploadBaseUrl).path("/").path(file.getName()).queryParam("token", uploadToken).toUriString());

        Response response = httpClient.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("upload failed. " + response.toString());
        }

        log.info("upload succeeded");

        if (file.delete()) {
            log.info("delete succeeded");
        }

        return UriComponentsBuilder.fromHttpUrl(playbackUrl).path("/").path(file.getName()).toUriString();
    }

    public File decoder(String base64Image, String filename) {
        File file = new File(cachePath + "/" + filename);
        try (FileOutputStream imageOutFile = new FileOutputStream(file)) {
            byte[] imageByteArray = Base64.getDecoder().decode(base64Image);
            imageOutFile.write(imageByteArray);
        } catch (FileNotFoundException e) {
            log.info("Image not found" + e);
        } catch (IOException ioe) {
            log.info("Exception while reading the Image " + ioe);
        }
        return file;
    }

}
