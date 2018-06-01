package boston.convertdata;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.val;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
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
    private final String type;
    private final int batchSize;
    private final List<String> queue = new ArrayList<>();
    private int count = 0;
    private static final Logger logger = Logger.getLogger(EsUploader.class.getName());

    public EsUploader(String elasticSearchUrl, String indexName, String type, int batchSize) throws IOException {
        esClient = new RestHighLevelClient(RestClient.builder(HttpHost.create(elasticSearchUrl)));
        this.indexName = indexName;
        this.type = type;
        this.batchSize = batchSize;
    }

    public boolean indexExists() throws IOException {
        val response = esClient.getLowLevelClient().performRequest("HEAD", "/" + indexName);
        int statusCode = response.getStatusLine().getStatusCode();
        return statusCode != 404;
    }

    public void addJsonDocument(String json) throws IOException {
        queue.add(json);
        if (queue.size() >= batchSize) {
            flush();
        }
    }

    public void deleteIndex() throws IOException {
        esClient.indices().delete(new DeleteIndexRequest(indexName));
    }

    public void createIndex(Object... mapping) throws IOException {
        val request = new CreateIndexRequest(indexName);
        if (mapping != null && mapping.length > 0) {
            request.mapping(type, mapping);
        }
        esClient.indices().create(request);
    }

    public void flush() throws IOException {
        if (!queue.isEmpty()) {
            val bulkRequest = new BulkRequest();
            queue.forEach(json -> {
                val indexRequest = new IndexRequest(indexName, type).source(json, XContentType.JSON);
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