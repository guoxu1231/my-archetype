package gladiator.kafka;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public class KafkaProducerService {

    protected final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    Properties defaultProps;
    String topic;


    Long total;
    AtomicLong current = new AtomicLong(0);
    AtomicLong error = new AtomicLong(0);
    AtomicBoolean running = new AtomicBoolean(true);
    boolean sync = true;

    ConcurrentMap<String, AtomicInteger> statMap = new ConcurrentHashMap<>();

    public KafkaProducerService(Properties defaultProps, String topic, boolean sync) {
        this.defaultProps = defaultProps;
        this.topic = topic;
        this.sync = sync;
    }

    public String send(long count) throws IOException, InterruptedException, ExecutionException, TimeoutException {

        final String[] app_code = {"JD", "JR"};
        final String[] channel = {"taobao", "sinapay", "caiwu", "antiMoney"};
        ObjectMapper mapper;
        Random rand;
        mapper = new ObjectMapper();// create once, reuse
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        rand = new Random();

        resetCounter();
        this.total = count;
        KafkaProducer producer = new KafkaProducer(defaultProps);

        //TODO lock
        StopWatch watch = new StopWatch("kafka producer send " + count);
        watch.start();
        for (long i = 0L; i < count; i++) {
            Map<String, Object> msgMap = new HashMap<>();
            msgMap.put("job_type", i % 1);
            msgMap.put("business_id", rand.nextInt(10000));
            msgMap.put("app_code", app_code[(int) i % 1]);
            msgMap.put("channel", channel[(int) i % 3]);
            msgMap.put("file_name", channel[(int) i % 3] + "-" + new Date().getTime() + ".dat");
            msgMap.put("begin_time", new Date().getTime());
            msgMap.put("end_time", new Date().getTime() + rand.nextInt(60000));
            msgMap.put("result_code", 1);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            mapper.writeValue(out, msgMap);
            //TODO record exception and query in status
            if (sync) {
                try {
                    producer.send(new ProducerRecord(topic, new String(out.toByteArray()))).get(5000, TimeUnit.SECONDS);
                    current.incrementAndGet();
                } catch (Exception exception) {
                    logger.error(exception.toString());
                    error.incrementAndGet();
                    statMap.putIfAbsent(exception.getClass().getName(), new AtomicInteger(0));
                    statMap.get(exception.getClass().getName()).incrementAndGet();
                }
            } else {
                producer.send(new ProducerRecord(topic, new String(out.toByteArray())), (metadata, exception) -> {
                    if (exception != null) {
                        logger.error(exception.toString());
                        error.incrementAndGet();
                        statMap.putIfAbsent(exception.getClass().getName(), new AtomicInteger(0));
                        statMap.get(exception.getClass().getName()).incrementAndGet();
                    } else
                        current.incrementAndGet();
                });
            }
            if (!running.get()) {
                String msg = String.format("kafka producer stopped. %d/%d, error:%d\n", current.longValue(), total, error.longValue());
                producer.close();
                return msg;
            }
        }
        watch.stop();
        producer.close();
        return watch.toString();
    }

    public String stop() {
        this.running.set(false);
        return "stop finished";
    }

    public String status() {
        return String.format("%s %d/%d, error:%d\n%s", sync ? "sync producer" : "async producer",
                current.longValue(), total, error.longValue(), printStatMap());//TODO message ratio
    }

    public String reload(String path, boolean sync) throws IOException {
        this.running.set(false);
        Properties newProps = new Properties();
        newProps.load(new FileInputStream(path));
        newProps.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, defaultProps.getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        this.defaultProps = newProps;
        this.sync = sync;
        return String.format("kafka config is reload from %s, %s", path, sync ? "sync producer" : "async producer");
    }

    public String topic(String topic) {
        this.topic = topic;
        return "kafka topic " + topic;
    }

    private void resetCounter() {
        current.set(0);
        total = 0L;
        error.set(0);
        running.set(true);
        statMap.clear();
    }

    private String printStatMap() {
        StringBuffer sb = new StringBuffer();
        for (String exception : statMap.keySet()) {
            sb.append(exception + ":" + statMap.get(exception) + ",");
        }
        return sb.toString();
    }

}
