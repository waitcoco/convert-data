package boston.convertdata.config;

import boston.convertdata.repository.EsUploader;
import boston.convertdata.repository.VideoInfoGetter;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfiguration {
    @Bean
    public RestHighLevelClient restHighLevelClient(@Value("${elasticsearch.url}") String elasticsearchUrl) {
        return new RestHighLevelClient(RestClient.builder(HttpHost.create(elasticsearchUrl)));
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
}
