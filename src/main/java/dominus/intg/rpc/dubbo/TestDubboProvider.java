package dominus.intg.rpc.dubbo;


import dominus.framework.junit.DominusJUnit4TestBase;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(locations = {"classpath:spring-container/dubbo_provider_context.xml"})
public class TestDubboProvider extends DominusJUnit4TestBase {


    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
    }

    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();
    }


    @Test
    public void testExposeProvider() throws InterruptedException {

        Thread.sleep(5 * Minute);

    }


}
