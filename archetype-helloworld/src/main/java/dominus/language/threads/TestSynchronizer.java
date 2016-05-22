package dominus.language.threads;


import dominus.framework.junit.DominusJUnit4TestBase;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class TestSynchronizer extends DominusJUnit4TestBase {

    @Test
    public void testCountDownLatch() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch((int) 100);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    latch.countDown();
                    logger.info("countDown..{}..........CountDownLatch", latch.getCount());
                    if (latch.getCount() == 0) break;
                }
            }
        }).start();
        //EE: without this latch, the testcase will be exit immediately
        assertEquals(true, latch.await(5000, TimeUnit.SECONDS));
        System.out.println("I'm Done");
    }
}
