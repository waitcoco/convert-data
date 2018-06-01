package boston.convertdata;

import lombok.val;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class EsUploader implements AutoCloseable {
    private final RestHighLevelClient esClient;
    private final String indexName;
    private final int batchSize;
    private final List<String> queue = new ArrayList<>();
    private int count = 0;
    private static final Logger logger = Logger.getLogger(EsUploader.class.getName());

    public EsUploader(String elasticSearchUrl, String indexName, int batchSize) throws IOException {
        esClient = new RestHighLevelClient(RestClient.builder(HttpHost.create(elasticSearchUrl)));
        this.indexName = indexName;
        this.batchSize = batchSize;
        //ensureIndexNotExist();
    }

    private void ensureIndexNotExist() throws IOException {
        val response = esClient.getLowLevelClient().performRequest("HEAD", "/" + indexName);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 404) {
            throw new RuntimeException("Index " + indexName + " already exists.");
        }
    }

    public void addJsonDocument(String json) throws IOException {
        queue.add(json);
        if (queue.size() >= batchSize) {
            flush();
        }
    }

    public void deleteIndex() throws IOException {
        val response = esClient.getLowLevelClient().performRequest("DELETE", "/" + indexName);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode < 200 || statusCode >= 400) {
            throw new RuntimeException("Failed to delete index. Status code: " + statusCode);
        }
    }

    public void flush() throws IOException {
        if (!queue.isEmpty()) {
            val bulkRequest = new BulkRequest();
            queue.forEach(json -> {
                val indexRequest = new IndexRequest(indexName, "doc").source(json, XContentType.JSON);
                bulkRequest.add(indexRequest);
            });
            BulkResponse bulkResponse;
            bulkResponse = esClient.bulk(bulkRequest);
            if (bulkResponse.hasFailures()) {
                throw new RuntimeException(bulkResponse.buildFailureMessage());
            }

            count += queue.size();
            queue.clear();
            logger.info("Imported " + count + " documents.");
        }
    }

    @Override
    public void close() throws Exception {
        esClient.close();
    }
}