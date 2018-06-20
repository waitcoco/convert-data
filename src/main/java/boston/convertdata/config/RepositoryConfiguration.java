package boston.convertdata.config;

import boston.convertdata.repository.EsUploader;
import boston.convertdata.repository.ImageUploader;
import boston.convertdata.repository.ImageVectorRepository;
import boston.convertdata.repository.VideoInfoGetter;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class RepositoryConfiguration {
    @Bean
    public RestHighLevelClient restHighLevelClient(@Value("${elasticsearch.host}") String[] elasticsearchHosts) {
        return new RestHighLevelClient(RestClient.builder(Arrays.stream(elasticsearchHosts).map(HttpHost::create).toArray(HttpHost[]::new)));
    }

    @Bean
    public EsUploader esUploader(RestHighLevelClient restHighLevelClient,
                                 @Value("${elasticsearch.indexName}") String indexName,
                                 @Value("${elasticsearch.type:doc}") String type,
                                 @Value("${elasticsearch.batchSize:1000}") int batchSize) {
        return new EsUploader(restHighLevelClient, indexName, type, batchSize);
    }

    @Bean
    public VideoInfoGetter videoInfoGetter(@Value("${videoInfo.url}") String videoInfoUrl) {
        return new VideoInfoGetter(videoInfoUrl);
    }

    @Bean
    public ImageUploader imageUploader(@Value("${image.uploadBaseUrl}") String uploadBaseUrl,
                                       @Value("${image.uploadToken}") String uploadToken,
                                       @Value("${image.playbackUrl}") String playbackUrl) {
        return new ImageUploader(uploadBaseUrl, uploadToken, playbackUrl);
    }

    @Bean
    public ImageVectorRepository imageVectorRepository(@Value("${image.vectorUrl}") String vectorUrl,
                                                       @Value("${image.cachePath}") String cachePath) {
        return new ImageVectorRepository(vectorUrl, cachePath);
    }
}
