package dominus.intg.coordinator.consul;


import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import dominus.framework.junit.DominusJUnit4TestBase;
import org.junit.Test;

public class TestConsulClient extends DominusJUnit4TestBase {

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();

        Consul consul = Consul.builder().withUrl("http://10.253.11.216:8500").build(); // connect to Consul on localhost
        AgentClient agentClient = consul.agentClient();

        String serviceName = "MyService";
        String serviceId = "1";

        agentClient.register(8080, 3L, serviceName, serviceId); // registers with a TTL of 3 seconds
        agentClient.pass(serviceId); // check in with Consul, serviceId required only.  client will prepend "service:" for service level checks.
    }

    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();
    }

    @Test
    public void testNull() {

    }
}
