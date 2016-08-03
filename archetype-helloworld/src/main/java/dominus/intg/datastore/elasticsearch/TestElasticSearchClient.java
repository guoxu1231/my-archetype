package dominus.intg.datastore.elasticsearch;


import dominus.framework.junit.DominusJUnit4TestBase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Perform standard index, get, delete and search operations on an existing cluster;
 * Perform administrative tasks on a running cluster
 */
public class TestElasticSearchClient extends DominusJUnit4TestBase {

    Client client;
    final static String TEST_INDEX = "test_index_bank";
    final static String TEST_INDEX_TYPE = "default_type";

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        client = TransportClient.builder().build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(properties.getProperty("elastic.search.address")),
                        Integer.valueOf(properties.getProperty("elastic.search.port"))));
        //EE: load test data(manual refresh)
        if (!client.admin().indices().exists(new IndicesExistsRequest(new String[]{TEST_INDEX})).get().isExists()) {
            CreateIndexResponse response = client.admin().indices().prepareCreate(TEST_INDEX).
                    setSettings(Settings.builder()
                            .put("index.number_of_shards", 3)
                            .put("index.number_of_replicas", 1)
                            .put("index.refresh_interval", -1)).
                    get();
            assertTrue(response.isAcknowledged());
            out.printf("create test index %s [acknowledged]\n", TEST_INDEX);

            String accounts = IOUtils.toString(resourceLoader.getResource("classpath:data/json/accounts.json").getURI(), "UTF-8");
            String[] accountsArray = accounts.split("\n");
            for (int i = 0; i < accountsArray.length; i += 2) {
                IndexResponse indexResponse = client.prepareIndex(TEST_INDEX, TEST_INDEX_TYPE, StringUtils.substringBetween(accountsArray[i], "\"_id\":", "}")).
                        setSource(accountsArray[i + 1]).get();
                logger.info(indexResponse.toString());
            }

            //EE: refresh index segment to make it searchable
            assertEquals(0, search(TEST_INDEX, matchAllQuery(), 3).getHits().getTotalHits());
            client.admin().indices().prepareRefresh(TEST_INDEX).get();
            assertEquals(1000, search(TEST_INDEX, matchAllQuery(), 3).getHits().getTotalHits());
        }

        GetIndexResponse getIndexResponse = client.admin().indices().getIndex(new GetIndexRequest().indices(TEST_INDEX)).get();
        out.printf("[Index]:%s [Settings]:%s\n", TEST_INDEX, Arrays.toString(getIndexResponse.settings().get(TEST_INDEX).getAsStructuredMap().entrySet().toArray()));
        //TODO import employee & department one-to-many relationshop.
//        client.search()
    }

    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();
        client.close();
    }

    /**
     * The term query looks for the exact term in the field’s inverted index
     */

    @Test
    public void testQueryDSL() {

        //EE:Term level queries, the term-level queries operate on the exact terms that are stored in the inverted index.
        // term query, range query
        QueryBuilder rangeQuery = rangeQuery("age").from(40).includeLower(true);
        /**
         * Every fields are analyzed by default. It means that "ABC" will be indexed as "abc" (lower case).
         You have to use term query or term filter with string in LOWER CASE.
         */
        QueryBuilder termQuery = termQuery("firstname", "alexandra");
        //age=40 and (gender = 'f' or gender = 'm')
        QueryBuilder boolQuery1 = boolQuery().must(termQuery("age", 40)).
                must(boolQuery().should(termQuery("gender", "f")).should(termQuery("gender", "m")));

        assertEquals(45, search(TEST_INDEX, rangeQuery, 3).getHits().getTotalHits());
        assertEquals(1, search(TEST_INDEX, termQuery, 3).getHits().getTotalHits());
        assertEquals(45, search(TEST_INDEX, boolQuery1, 3).getHits().getTotalHits());

        //EE: Full text queries,
        //apply each field’s analyzer (or search_analyzer) to the query string before executing.
        QueryBuilder matchQuery = matchQuery("address", "JOVAL Fenimore WilliamsburG");

        assertEquals(3, search(TEST_INDEX, matchQuery, 3).getHits().getTotalHits());
    }


    @Test
    public void testAggregation() {

        AbstractAggregationBuilder aggregation =
                AggregationBuilders
                        .terms("age_terms").field("age");
//                        .field("age");
//        SearchResponse response = client.prepareSearch(TEST_INDEX).setTypes(TEST_INDEX_TYPE)
//                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
//                .setQuery(matchAllQuery())
//                .addAggregation(aggregation)
//                .setSize(0).setExplain(false).execute().actionGet();
        aggregateSearch(TEST_INDEX, matchAllQuery(), aggregation);
//        logger.info(response.toString());

    }


    @Test
    public void testAnalyzer() {
        AnalyzeRequest request = (new AnalyzeRequest(TEST_INDEX).text("XHDK-A-1293-#fJ3")).analyzer("standard");
        List<AnalyzeResponse.AnalyzeToken> tokens = client.admin().indices().analyze(request).actionGet().getTokens();
        for (AnalyzeResponse.AnalyzeToken token : tokens) {
            //All letters have been lowercased.
            //We lost the hyphen and the hash (#) sign.
            out.println(token.getTerm());
        }
    }

    @Test
    public void testBulKLoad() throws IOException, InterruptedException {
        String indexName = TEST_INDEX + "-" + new Date().getTime();

        CreateIndexResponse response = client.admin().indices().prepareCreate(indexName).get();
        assertTrue(response.isAcknowledged());

        BulkProcessor bulkProcessor = BulkProcessor.builder(client, new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {

            }
        }).
                setBulkActions(1001).
                setConcurrentRequests(1).build();
        String accounts = IOUtils.toString(resourceLoader.getResource("classpath:data/json/accounts.json").getURI(), "UTF-8");
        String[] accountsArray = accounts.split("\n");
        for (int i = 0; i < accountsArray.length; i += 2) {
            bulkProcessor.add(new IndexRequest(indexName, TEST_INDEX_TYPE, StringUtils.substringBetween(accountsArray[i], "\"_id\":", "}")).source(accountsArray[i + 1]));
        }
        assertEquals(0, search(indexName, matchAllQuery(), 3).getHits().getTotalHits());
        bulkProcessor.awaitClose(1, TimeUnit.MINUTES);
        assertEquals(1000, search(TEST_INDEX, matchAllQuery(), 3).getHits().getTotalHits());
        client.admin().indices().prepareDelete(indexName).get();
    }


    SearchResponse search(String index, QueryBuilder queryBuilder, int size) {
        SearchRequestBuilder builder = client.prepareSearch(index).setTypes(TEST_INDEX_TYPE)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(queryBuilder)
                .setFrom(0).setSize(size).setExplain(false);
        println(ANSI_BLUE, "[JSON GENERATED QUERY]\n" + builder);
        SearchResponse response = builder.execute().actionGet();
        logger.info(response.toString());
        return response;
    }

    Aggregation aggregateSearch(String index, QueryBuilder queryBuilder, AbstractAggregationBuilder aggBuilder) {

        SearchRequestBuilder builder = client.prepareSearch(index).setTypes(TEST_INDEX_TYPE)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(queryBuilder)
                .addAggregation(aggBuilder)
                .setFrom(0).setSize(0).setExplain(false);
        println(ANSI_BLUE, "[JSON GENERATED QUERY]\n" + builder);
        SearchResponse response = builder.execute().actionGet();
        logger.info(response.toString());
        return response.getAggregations().get(aggBuilder.getName());
    }
}
