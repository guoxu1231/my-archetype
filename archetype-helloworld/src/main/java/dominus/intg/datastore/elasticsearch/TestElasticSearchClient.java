package dominus.intg.datastore.elasticsearch;


import dominus.framework.junit.DominusJUnit4TestBase;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.junit.Test;
import org.apache.commons.lang.StringUtils;


import java.net.InetAddress;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertTrue;

/**
 * Perform standard index, get, delete and search operations on an existing cluster;
 * Perform administrative tasks on a running cluster
 */
public class TestElasticSearchClient extends DominusJUnit4TestBase {

    Client client;
    final static String TEST_INDEX = "accounts";

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        client = TransportClient.builder().build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(properties.getProperty("elastic.search.address")),
                        Integer.valueOf(properties.getProperty("elastic.search.port"))));
        CreateIndexResponse response = client.admin().indices().create(new CreateIndexRequest(TEST_INDEX)).get();
        assertTrue(response.isAcknowledged());
        out.printf("create test index %s [acknowledged]\n", TEST_INDEX);

        //EE: load test data
        String accounts = IOUtils.toString(resourceLoader.getResource("classpath:data/json/accounts.json").getURI(), "UTF-8");
        String[] accountsArray = accounts.split("\n");
        for (int i = 0; i < accountsArray.length; i += 2) {
            IndexResponse indexResponse = client.prepareIndex(TEST_INDEX, TEST_INDEX, StringUtils.substringBetween(accountsArray[i], "\"_id\":", "}")).
                    setSource(accountsArray[i + 1]).get();
            logger.info(indexResponse.toString());
        }
//        client.search()
    }

    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();
        DeleteIndexResponse response = client.admin().indices().delete(new DeleteIndexRequest(TEST_INDEX)).get();
        assertTrue(response.isAcknowledged());
        out.printf("delete test index %s [acknowledged]\n", TEST_INDEX);
        client.close();
    }

    @Test
    public void testNull() {

    }
}
