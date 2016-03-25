package dominus.intg.rpc.dubbo;


import dominus.framework.junit.DominusJUnit4TestBase;
import dominus.intg.rpc.dubbo.demo.DemoService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.*;

@ContextConfiguration(locations = {"classpath:spring-container/dubbo_consumer_context.xml"})
public class TestDubboConsumer extends DominusJUnit4TestBase {

    @Autowired
    DemoService demoService;


    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
    }

    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();
    }

    @Test
    public void testInvokeProvider() throws InterruptedException {

        assertEquals("Hello Shawguo", demoService.sayHello("Shawguo"));
    }


}
