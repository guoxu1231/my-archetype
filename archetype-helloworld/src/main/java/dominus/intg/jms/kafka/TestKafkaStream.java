package dominus.intg.jms.kafka;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.test.context.ContextConfiguration;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@ContextConfiguration(locations = {"classpath:spring-container/mysql_jdbc_context.xml"})
public class TestKafkaStream extends KafkaZBaseTestCase {
    public static final String TEST_TOPIC_PREFIX = "kafka_stream_";
    String testTopicName;


    KStreamBuilder builder;
    StreamsConfig config;
    @Autowired
    @Qualifier("mysql_dataSource")
    DataSource sourceMysqlDS;
    JdbcTemplate jdbcTemplate;

    ObjectMapper mapper;

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        testTopicName = TEST_TOPIC_PREFIX + new Date().getTime();
        this.createTestTopic(testTopicName, 1, 1);

        mapper = new ObjectMapper();// create once, reuse
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "my-stream-processing-application-" + new Date().getTime());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getProperty("bootstrap.servers"));
        props.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, properties.getProperty("zkQuorum"));
        props.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        config = new StreamsConfig(props);

        //prepare stream data from employees table
        new Thread(() -> {
            sleep(5 * Second);
            jdbcTemplate = new JdbcTemplate(sourceMysqlDS);
            jdbcTemplate.query("select * from employees order by employees.emp_no asc limit 1000", rs -> {
                ProducerRecord record = null;
                record = new ProducerRecord(testTopicName, String.valueOf(rs.getInt("emp_no")), resultSet2Json(rs, rs.getRow()));
                _producer.send(record);
            });
        }).start();

    }

    @Override
    protected void doTearDown() throws Exception {
        deleteTestTopic(testTopicName);
        super.doTearDown();

    }

    @Test
    public void testSimpleFromTo() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1000);
        KStreamBuilder builder = new KStreamBuilder();
        builder.stream(testTopicName).filter((K, V) -> {
            latch.countDown();
            logger.info("K={}, V={}", K, V);
            return true;
        }).mapValues(V -> {
            return "HELLO KAFKA STREAM";
        }).to(testTopicName + "-output");
        KafkaStreams streams = new KafkaStreams(builder, config);
        streams.start();
        Assert.assertTrue(latch.await(2, TimeUnit.MINUTES));
    }


    String resultSet2Json(ResultSet rs, int rowNum) {
        ObjectNode objectNode = mapper.createObjectNode();
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            objectNode.put("rowNum", rowNum);

            for (int index = 1; index <= columnCount; index++) {
                String column = JdbcUtils.lookupColumnName(rsmd, index);
                Object value = rs.getObject(column);
                if (value == null) {
                    objectNode.putNull(column);
                } else if (value instanceof Integer) {
                    objectNode.put(column, (Integer) value);
                } else if (value instanceof String) {
                    objectNode.put(column, (String) value);
                } else if (value instanceof Boolean) {
                    objectNode.put(column, (Boolean) value);
                } else if (value instanceof java.sql.Date) {
                    objectNode.put(column, simpleDateFormat.format(value));
                } else if (value instanceof Long) {
                    objectNode.put(column, (Long) value);
                } else if (value instanceof Double) {
                    objectNode.put(column, (Double) value);
                } else if (value instanceof Float) {
                    objectNode.put(column, (Float) value);
                } else if (value instanceof BigDecimal) {
                    objectNode.put(column, (BigDecimal) value);
                } else if (value instanceof Byte) {
                    objectNode.put(column, (Byte) value);
                } else if (value instanceof byte[]) {
                    objectNode.put(column, (byte[]) value);
                } else {
                    throw new IllegalArgumentException("Unmappable object type: " + value.getClass());
                }
            }
            return mapper.writeValueAsString(objectNode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
