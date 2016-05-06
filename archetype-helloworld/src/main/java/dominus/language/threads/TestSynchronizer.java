package dominus.language.threads;


import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class TestSynchronizer {

    @Test
    public void testCountDownLatch() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch((int) 100);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    latch.countDown();
                    System.out.printf("countDown..%d..........CountDownLatch\n", latch.getCount());
                }
            }
        }).start();
        //EE: without this latch, the testcase will be exit immediately
        assertEquals(true, latch.await(5000, TimeUnit.SECONDS));
        System.out.println("I'm Done");
    }
}
