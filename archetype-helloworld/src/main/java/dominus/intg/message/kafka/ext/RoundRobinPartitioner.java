package dominus.intg.message.kafka.ext;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.common.Cluster;
import org.apache.log4j.Logger;
import org.apache.kafka.clients.producer.Partitioner;

/**
 * Round robin partitioner using a simple thread safe AotmicInteger
 */
public class RoundRobinPartitioner implements Partitioner {
    private static final Logger log = Logger.getLogger(dominus.intg.message.kafka.ext.RoundRobinPartitioner.class);

    final AtomicInteger counter = new AtomicInteger(0);

    public RoundRobinPartitioner() {
    }

    @Override
    public void configure(Map<String, ?> configs) {
        log.trace("Instatiated the Round Robin Partitioner class");
        //TODO configure advanced partition strategy
    }

    @Override
    public void close() {
    }

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        int partitionId = counter.incrementAndGet() % cluster.partitionsForTopic(topic).size();
        if (counter.get() > 65536) {
            counter.set(0);
        }
        return partitionId;
    }

    /**
     * Take key as value and return the partition number
     */
    @Deprecated //0.8.x old method
    public int partition(Object key, int partitions) {

        int partitionId = counter.incrementAndGet() % partitions;
        if (counter.get() > 65536) {
            counter.set(0);
        }
        return partitionId;
    }
}
