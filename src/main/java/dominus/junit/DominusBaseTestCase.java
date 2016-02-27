package dominus.junit;


import junit.framework.TestCase;
import org.apache.commons.lang.reflect.FieldUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.PrintStream;


/**
 * EE:
 * [Spring] ResourceLoader
 */
public class DominusBaseTestCase extends TestCase {


    protected ResourceLoader resourceLoader = new DefaultResourceLoader();
    protected PrintStream out = System.out;

    @Override
    protected void setUp() throws Exception {
        out.println("*************************DominusBaseTestCase.setUp***************************");
    }

    @Override
    protected void tearDown() throws Exception {
        out.println("*************************DominusBaseTestCase.tearDown************************");
    }

    @Override
    protected void runTest() throws Throwable {
        out.printf("------------------------------------%s begin------------------------------------\n", super.getName());
        super.runTest();
        out.printf("------------------------------------%s end  ------------------------------------\n", super.getName());
    }
}
