package dominus.intg.datastore.elasticsearch;


import dominus.framework.junit.DominusJUnit4TestBase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.Test;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

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
        //EE: load test data
        if (!client.admin().indices().exists(new IndicesExistsRequest(new String[]{TEST_INDEX})).get().isExists()) {
            CreateIndexResponse response = client.admin().indices().create(new CreateIndexRequest(TEST_INDEX)).get();
            assertTrue(response.isAcknowledged());
            out.printf("create test index %s [acknowledged]\n", TEST_INDEX);

            String accounts = IOUtils.toString(resourceLoader.getResource("classpath:data/json/accounts.json").getURI(), "UTF-8");
            String[] accountsArray = accounts.split("\n");
            for (int i = 0; i < accountsArray.length; i += 2) {
                IndexResponse indexResponse = client.prepareIndex(TEST_INDEX, TEST_INDEX_TYPE, StringUtils.substringBetween(accountsArray[i], "\"_id\":", "}")).
                        setSource(accountsArray[i + 1]).get();
                logger.info(indexResponse.toString());
            }
            //wait for index analyzing. //TODO indexing status??
            Thread.sleep(10 * Second);
        } else {
            GetIndexResponse getIndexResponse = client.admin().indices().getIndex(new GetIndexRequest().indices(TEST_INDEX)).get();
            out.printf("[Index]:%s [Settings]:%s\n", TEST_INDEX, Arrays.toString(getIndexResponse.settings().get(TEST_INDEX).getAsStructuredMap().entrySet().toArray()));
        }

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
    public void testAnalyzer() {
        AnalyzeRequest request = (new AnalyzeRequest(TEST_INDEX).text("XHDK-A-1293-#fJ3")).analyzer("standard");
        List<AnalyzeResponse.AnalyzeToken> tokens = client.admin().indices().analyze(request).actionGet().getTokens();
        for (AnalyzeResponse.AnalyzeToken token : tokens) {
            //All letters have been lowercased.
            //We lost the hyphen and the hash (#) sign.
            out.println(token.getTerm());
        }
    }


    SearchResponse search(String index, QueryBuilder queryBuilder, int size) {
        println(ANSI_BLUE, "[JSON GENERATED QUERY]\n" + queryBuilder);
        SearchResponse response = client.prepareSearch(TEST_INDEX).setTypes(TEST_INDEX_TYPE)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(queryBuilder)
                .setFrom(0).setSize(size).setExplain(false).execute().actionGet();
        logger.info(response.toString());
        return response;
    }
}
