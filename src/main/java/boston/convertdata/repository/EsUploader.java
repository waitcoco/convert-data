package boston.convertdata.repository;

import lombok.val;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class EsUploader {
    private final RestHighLevelClient esClient;
    private final String indexName;
    private final String type;
    private final int batchSize;
    private static final Logger logger = Logger.getLogger(EsUploader.class.getName());

    public EsUploader(RestHighLevelClient restHighLevelClient, String indexName, String type, int batchSize) {
        esClient = restHighLevelClient;
        this.indexName = indexName;
        this.type = type;
        this.batchSize = batchSize;
    }

    public boolean indexExists() throws IOException {
        val response = esClient.getLowLevelClient().performRequest("HEAD", "/" + indexName);
        int statusCode = response.getStatusLine().getStatusCode();
        return statusCode != 404;
    }

    public void deleteIndex() throws IOException {
        esClient.indices().delete(new DeleteIndexRequest(indexName));
        logger.info("Deleted index: " + indexName);
    }

    public void createIndex(Object... mapping) throws IOException {
        val request = new CreateIndexRequest(indexName);
        if (mapping != null && mapping.length > 0) {
            request.mapping(type, mapping);
        }
        esClient.indices().create(request);
        logger.info("Created index: " + indexName);
    }

    public Session newSession() {
        return new Session();
    }

    public class Session implements AutoCloseable {
        private final Map<String, String> queue = new HashMap<>();
        private int count = 0;

        public void addJsonDocument(String id, String json) throws IOException {
            queue.put(id, json);
            if (queue.size() >= batchSize) {
                flush();
            }
        }

        public void flush() throws IOException {
            if (!queue.isEmpty()) {
                val bulkRequest = new BulkRequest();
                queue.forEach((id, json) -> {
                    val indexRequest = new IndexRequest(indexName, type, id).source(json, XContentType.JSON);
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
            flush();
        }
    }
}