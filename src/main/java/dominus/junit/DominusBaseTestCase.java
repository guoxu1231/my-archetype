package dominus.junit;


import junit.framework.TestCase;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.PrintStream;
import java.util.Properties;


/**
 * EE:
 * [Spring] ResourceLoader
 */
public class DominusBaseTestCase extends TestCase {


    protected ResourceLoader resourceLoader = new DefaultResourceLoader();
    protected PrintStream out = System.out;
    protected Properties properties;

    //color stdout
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public DominusBaseTestCase() {
        super();
    }

    public DominusBaseTestCase(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        properties = PropertiesLoaderUtils.loadProperties(resourceLoader.getResource("classpath:cdh.properties"));
        PropertiesLoaderUtils.fillProperties(properties, resourceLoader.getResource("classpath:jdbc.properties"));
        assertTrue(properties.size() > 0);
        out.println("[Global Properties]:" + properties.size());
    }

    @Override
    protected void tearDown() throws Exception {

        //console color workaround
        Thread.sleep(500);
    }

    @Override
    protected void runTest() throws Throwable {
        out.printf("\t\t\t\t\t\t[%s] %s begin\n", this.getClass().getSimpleName(), super.getName());
        super.runTest();
        out.printf("\t\t\t\t\t\t[%s] %s end\n", this.getClass().getSimpleName(), super.getName());
    }

    @Override
    public void runBare() throws Throwable {
        out.printf(ANSI_CYAN + "*************************[%s] %s.setUp*************************\n", this.getClass().getSimpleName(), super.getName());
        setUp();
        out.printf("*************************[%s] %s.setUp*************************\n" + ANSI_RESET, this.getClass().getSimpleName(), super.getName());
        try {
            runTest();
        } finally {
            out.printf(ANSI_CYAN + "*************************[%s] %s.tearDown*************************\n", this.getClass().getSimpleName(), super.getName());
            tearDown();
            out.printf("*************************[%s] %s.tearDown*************************\n" + ANSI_RESET, this.getClass().getSimpleName(), super.getName());
        }
    }
}
