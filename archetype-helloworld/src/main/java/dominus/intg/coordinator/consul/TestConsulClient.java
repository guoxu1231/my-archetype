package dominus.intg.coordinator.consul;


import com.orbitz.consul.*;
import com.orbitz.consul.cache.ServiceHealthCache;
import com.orbitz.consul.model.agent.Agent;
import com.orbitz.consul.model.health.ServiceHealth;
import dominus.framework.junit.DominusJUnit4TestBase;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Required: communicate with remote consul client(-client=0.0.0.0).
 * https://www.consul.io/docs/agent/http.html
 */
public class TestConsulClient extends DominusJUnit4TestBase {

    Consul consul;

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();

        consul = Consul.builder().withUrl(properties.getProperty("consul.url")).build(); // connect to Consul on localhost

    }

    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();
    }

    @Test
    public void testServiceRegisterAndCheck() throws NotRegisteredException {
        AgentClient agentClient = consul.agentClient();
        String serviceName = "MyService";
        String serviceId = "1";

        agentClient.register(8080, 30L, serviceName, serviceId); // registers with a TTL of 30 seconds
        agentClient.pass(serviceId); // check in with Consul, serviceId required only.  client will prepend "service:" for service level checks.

        sleep(2000);

        HealthClient healthClient = consul.healthClient();
        List<ServiceHealth> nodes = healthClient.getHealthyServiceInstances("MyService").getResponse(); // discover only "passing" nodes
        assertEquals("serfHealth", nodes.get(0).getChecks().get(0).getCheckId());
        assertEquals("service:1", nodes.get(0).getChecks().get(1).getCheckId());
        logger.info(Arrays.toString(nodes.get(0).getChecks().toArray()));
    }

    @Test
    public void testStoreKV() {
        KeyValueClient kvClient = consul.keyValueClient();
        kvClient.putValue("foo", "bar");
        assertEquals("bar", kvClient.getValueAsString("foo").get());
    }

    @Test
    public void testServiceHealthSubscribe() throws Exception {
        CountDownLatch isDead = new CountDownLatch(1);
        AgentClient agentClient = consul.agentClient();
        String serviceName = "MyService2";
        String serviceId = "1";

        agentClient.register(8080, 5L, serviceName, serviceId); // registers with a TTL of 3 seconds
        agentClient.pass(serviceId); // check in with Consul, serviceId required only.  client will prepend "service:" for service level checks.

        Agent agent = consul.agentClient().getAgent();
        HealthClient healthClient = consul.healthClient();
        ServiceHealthCache svHealth = ServiceHealthCache.newCache(healthClient, serviceName);
        svHealth.addListener(newValues -> {
            // do Something with updated server map
            if (newValues.size() != 0) {
                out.println(serviceName + " register success!");
                out.println(newValues.toString());
            } else {
                out.println(serviceName + " TTL is over");
                isDead.countDown();
            }

        });
        svHealth.start();
        assertTrue(isDead.await(1, TimeUnit.MINUTES));
    }

    @Test
    public void testBlock() {
        //TODO
    }


}
