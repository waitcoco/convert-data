package boston.convertdata.config;

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
    public VideoInfoGetter videoInfoGetter(@Value("${videoInfo.url}") String videoInfoUrl) {
        return new VideoInfoGetter(videoInfoUrl);
    }
}
