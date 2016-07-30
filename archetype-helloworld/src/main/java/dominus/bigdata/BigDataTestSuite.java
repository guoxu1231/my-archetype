package dominus.bigdata;


import dominus.connector.MysqlBinlogConnectorTest;
import dominus.framework.retry.TestRetryTemplate;
import dominus.intg.datastore.elasticsearch.TestElasticSearchClient;
import dominus.intg.datastore.mongodb.MongoIntgTest;
import dominus.intg.datastore.mysql.MySqlDDLTest;
import dominus.intg.datastore.mysql.MySqlJDBCShardTest;
import dominus.intg.datastore.mysql.MySqlMetaDataTest;
import dominus.intg.datastore.persistent.mybatis.TestMyBatisDao;
import dominus.intg.datastore.redis.TestRedis;
import dominus.intg.datastore.zookeeper.TestZKClient;
import dominus.intg.jms.kafka.KafkaAdminTestCase;
import dominus.intg.jms.kafka.KafkaConsumerTestcase;
import dominus.intg.jms.kafka.KafkaEmbeddedServerTestCase;
import dominus.intg.jms.kafka.KafkaProducerTestcase;
import dominus.intg.scripting.GroovyIntgTest;
import dominus.framework.binding.TestXmlBinding;
import dominus.language.threads.TestSynchronizer;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

//EE: Aggregating tests in suites
@RunWith(Suite.class)
@Suite.SuiteClasses({MySqlDDLTest.class, MySqlMetaDataTest.class, MySqlJDBCShardTest.class, MysqlBinlogConnectorTest.class,
        GroovyIntgTest.class, TestXmlBinding.class, TestRetryTemplate.class,
        KafkaEmbeddedServerTestCase.class, KafkaProducerTestcase.class, KafkaConsumerTestcase.class, KafkaAdminTestCase.class,
        MongoIntgTest.class, TestRedis.class, TestElasticSearchClient.class, TestZKClient.class,
        TestMyBatisDao.class, TestSynchronizer.class})
public class BigDataTestSuite {

    /**JUnit 3.8.x static Test suite() method.
     public static Test suite() {
     TestSuite suite = new TestSuite();
     suite.addTest(new OozieActionTest());
     return suite;
     }
     **/
}
