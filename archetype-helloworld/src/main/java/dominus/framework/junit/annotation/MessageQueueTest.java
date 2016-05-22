package dominus.framework.junit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MessageQueueTest {
    boolean produceTestMessage() default false;

    int count() default 0;

    String queueName() default "";

    String consumerGroupId() default "";
}
