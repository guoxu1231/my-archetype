package dominus.framework.metrics;


import com.codahale.metrics.*;

import dominus.framework.junit.DominusJUnit4TestBase;
import org.junit.Test;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertEquals;


public class TestMetricsIntg extends DominusJUnit4TestBase {

    //The starting point for Metrics is the MetricRegistry class, which is a collection of all the metrics for your application
    static final MetricRegistry metrics = new MetricRegistry();

    MBeanServer mbs;

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(1, TimeUnit.SECONDS);

        final JmxReporter jmxReport = JmxReporter.forRegistry(metrics).build();
        jmxReport.start();

        mbs = ManagementFactory.getPlatformMBeanServer();
    }

    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();
    }

    @Test
    public void testNull() {
        Meter requests = metrics.meter("requests");
        requests.mark();
        sleep(5000);

    }

    //A gauge is the simplest metric type. It just returns a value.
    @Test
    public void testGauge() throws MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        metrics.register("dominus-properties", (Gauge<Integer>) () -> properties.size());
        Object value = mbs.getAttribute(new ObjectName("metrics:name=dominus-properties"), "Value");
        assertEquals(properties.size(), new Long(value.toString()).longValue());
        sleep(3000);
    }
}
