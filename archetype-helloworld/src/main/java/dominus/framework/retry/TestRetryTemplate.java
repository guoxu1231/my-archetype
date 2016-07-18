package dominus.framework.retry;


import com.google.common.collect.ImmutableMap;
import dominus.framework.junit.DominusJUnit4TestBase;
import org.junit.Test;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.ExponentialRandomBackOffPolicy;
import org.springframework.retry.backoff.ThreadWaitSleeper;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.StopWatch;

import java.net.SocketTimeoutException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestRetryTemplate extends DominusJUnit4TestBase {


    //Simple retry policy that retries a fixed number of times for a set of named exceptions (and subclasses)
    @Test
    public void testSimpleRetryPolicy() throws SocketTimeoutException {
        RetryTemplate template = new RetryTemplate();
        SimpleRetryPolicy policy = new SimpleRetryPolicy(3, (Map) ImmutableMap.of(SocketTimeoutException.class, true));
        template.setRetryPolicy(policy);

        assertEquals("hello world", template.execute(new RetryCallback<String, SocketTimeoutException>() {
            public String doWithRetry(RetryContext context) throws SocketTimeoutException {
                logger.info(context.toString());
                // Do stuff that might fail, e.g. webservice operation
                if (context.getRetryCount() < 2) //0,1,2
                    throw new SocketTimeoutException();
                else
                    return "hello world";
            }
        }));
    }

    @Test
    public void testExponentialRandomBackOffPolicy() throws SocketTimeoutException {
        RetryTemplate template = new RetryTemplate();

        SimpleRetryPolicy simplePolicy = new SimpleRetryPolicy(10, (Map) ImmutableMap.of(SocketTimeoutException.class, true));
        ExponentialRandomBackOffPolicy backOffPolicy = new ExponentialRandomBackOffPolicy();
        backOffPolicy.setInitialInterval(50);
        backOffPolicy.setMultiplier(2);
        backOffPolicy.setMaxInterval(3000);
        backOffPolicy.setSleeper(new ThreadWaitSleeper());

        template.setBackOffPolicy(backOffPolicy);
        template.setRetryPolicy(simplePolicy);
        StopWatch watch = new StopWatch();
        watch.start();
        assertEquals("hello world", template.execute(new RetryCallback<String, SocketTimeoutException>() {
            public String doWithRetry(RetryContext context) throws SocketTimeoutException {
                logger.info(context.toString());
                // Do stuff that might fail, e.g. webservice operation
                if (context.getRetryCount() < 9)
                    throw new SocketTimeoutException();
                else
                    return "hello world";
            }
        }));
        watch.stop();
        printf(ANSI_BLUE, "Total Time Consumed: %d\n", watch.getLastTaskTimeMillis());
    }

}
