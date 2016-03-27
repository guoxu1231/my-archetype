package dominus.intg.rpc.dubbo;


import dominus.framework.junit.DominusJUnit4TestBase;
import dominus.intg.rpc.dubbo.demo.DemoService;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertEquals;

/**
 * Startup dubbo server in test startup, invoke injvm service provider in each testcase.
 */
@ContextConfiguration(locations = {"classpath:spring-container/dubbo_provider_context.xml"})
public class TestDubboLocalInvoke extends DominusJUnit4TestBase {


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
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath:spring-container/dubbo_consumer_context.xml"});
        DemoService demoService = context.getBean(DemoService.class);
        assertEquals("Hello Shawguo", demoService.sayHello("Shawguo"));
    }
}
