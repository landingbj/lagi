package ai.bigdata.impl;

import ai.bigdata.IBigdata;
import ai.bigdata.pojo.TextIndexData;
import ai.config.pojo.BigdataConfig;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ElasticSearchAdapter implements IBigdata {
    private final ElasticsearchClient client;
    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchAdapter.class);

    public ElasticSearchAdapter(BigdataConfig config) {
        RestClientBuilder builder;
        if (config.getUsername() == null || config.getPassword() == null) {
            builder = RestClient.builder(new HttpHost(config.getHost(), config.getPort()));
        } else {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(config.getUsername(), config.getPassword()));
            builder = RestClient.builder(new HttpHost(config.getHost(), config.getPort())).setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }
        RestClient restClient = builder.build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        this.client = new ElasticsearchClient(transport);
    }

    @Override
    public boolean upsert(TextIndexData data) {
        boolean result = false;
        try {
            IndexResponse response = client.index(i -> i.index(data.getCategory()).id(data.getId()).document(data));
            if (response.result().equals(Result.Created) || response.result().equals(Result.Updated)) {
                result = true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public List<TextIndexData> search(String keyword, String category) {
        // 检查索引是否存在
        BooleanResponse indexExistsResponse = null;
        try {
            indexExistsResponse = client.indices().exists(i -> i.index(category));
        } catch (IOException e) {
            logger.error("Error while checking index existence", e);
            return new ArrayList<>();
        }

        if (!indexExistsResponse.value()) {
            logger.warn("Index {} does not exist", category);
            return new ArrayList<>();
        }
        SearchResponse<TextIndexData> searchResponse = null;
        try {
            searchResponse = client.search(s -> s.index(category).
                    size(1000).query(q -> q.matchPhrase(t -> t.field("text").query(keyword))), TextIndexData.class);
        } catch (IOException | ElasticsearchException e) {
            logger.error("Error while searching", e);
        }
        if (searchResponse == null) {
            return new ArrayList<>();
        }
        List<Hit<TextIndexData>> hits = searchResponse.hits().hits();
        List<TextIndexData> result = new ArrayList<>();
        for (Hit<TextIndexData> hit : hits) {
            result.add(hit.source());
        }
        return result;
    }

    @Override
    public boolean delete(String category) {
        boolean result = false;
        try {
            DeleteIndexResponse response = client.indices().delete(i -> i.index(category));
            result = response.acknowledged();
        } catch (IOException | ElasticsearchException e) {
            logger.error("Error while deleting", e);
        }
        return result;
    }
}
