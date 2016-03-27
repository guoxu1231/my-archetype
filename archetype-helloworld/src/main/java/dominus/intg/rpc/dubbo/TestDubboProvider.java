package dominus.intg.rpc.dubbo;


import dominus.framework.junit.DominusJUnit4TestBase;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

/**
 * [INFO] +- com.alibaba:dubbo:jar:2.5.4-SNAPSHOT:compile
 * [INFO] |  +- org.springframework:spring:jar:2.5.6.SEC03:compile
 * [INFO] |  +- org.jboss.netty:netty:jar:3.2.5.Final:compile
 * [INFO] |  \- org.javassist:javassist:jar:3.15.0-GA:compile
 */
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
