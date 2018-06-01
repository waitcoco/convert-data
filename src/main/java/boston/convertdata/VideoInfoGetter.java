package boston.convertdata;

import lombok.Data;
import org.apache.commons.httpclient.HttpClient;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

@Data

public class VideoInfoGetter {

    public String getVideoStartTime(int videoId) {
        String videoStartTime = "";
        try {
            String httpRequestURL = "http://localhost:8080/getvideodetail?videoId=" + Integer.toString(videoId);
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(httpRequestURL);
            CloseableHttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            videoStartTime = EntityUtils.toString(entity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return videoStartTime;
    }

    public String getCameraId(int videoId){
        String cameraId = "";
        try{
            String httpRequestURL = "http://loaclhost:8080/getcameraid?videoId="+Integer.toString(videoId);
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(httpRequestURL);
            CloseableHttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            cameraId = EntityUtils.toString(entity);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return cameraId;
    }

    public String getCameraName(int videoId){
        String cameraName = "";
        try{
            String httpRequestURL = "http://localhost:8080/getcameraname?videoId=" + Integer.toString(videoId);
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(httpRequestURL);
            CloseableHttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            cameraName = EntityUtils.toString(entity);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return cameraName;
    }

    public String getCameraLat(int videoId){
        String cameraLat = "";
        try{
            String httpRequestURL = "http://loaclhost:8080/getcameralat?videoId="+Integer.toString(videoId);
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(httpRequestURL);
            CloseableHttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            cameraLat = EntityUtils.toString(entity);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return cameraLat;
    }

    public String getCameraLon(int videoId){
        String cameraLon = "";
        try{
            String httpRequestURL = "http://loaclhost:8080/getcameralon?videoId="+Integer.toString(videoId);
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(httpRequestURL);
            CloseableHttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            cameraLon = EntityUtils.toString(entity);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return cameraLon;
    }
}
