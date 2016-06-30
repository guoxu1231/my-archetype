package dominus.intg.jmx

import dominus.framework.junit.DominusJUnit4TestBase
import org.junit.Test

import javax.management.remote.JMXConnectorFactory as JmxFactory
import javax.management.remote.JMXServiceURL as JmxUrl
import static org.junit.Assert.*

public class TestGroovyMBean extends DominusJUnit4TestBase {

    def jmxServer

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp()
        println "JMX URL [${super.properties.getProperty("kafka.jmx.url")}]"
        jmxServer = JmxFactory.connect(new JmxUrl(super.properties.getProperty("kafka.jmx.url"))).MBeanServerConnection
    }

    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown()
    }

    @Test
    public void testKafkaMBean() {

        def underReplicatedPartitions = new GroovyMBean(jmxServer, 'kafka.server:type=ReplicaManager,name=UnderReplicatedPartitions')
        println underReplicatedPartitions
        assertEquals(0, underReplicatedPartitions.Value)
        println "\n\n\n"
        def activeControllerCount = new GroovyMBean(jmxServer, 'kafka.controller:type=KafkaController,name=ActiveControllerCount')
        println activeControllerCount
        assertEquals(1, activeControllerCount.Value)
    }

}

