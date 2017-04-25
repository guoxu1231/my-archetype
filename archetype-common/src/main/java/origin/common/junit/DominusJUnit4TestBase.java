package origin.common.junit;


import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * For JUnit 4.x
 * <p/>
 * EE:
 * [Spring] ResourceLoader
 * [HDFS] hdfs client
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-container/junit4_base_context.xml")
@ActiveProfiles("travis-ci")
public class DominusJUnit4TestBase {

    @Resource(name = "globalProps")
    protected Properties properties;

    @Autowired
    Environment environment;

    @Rule //The TestName Rule makes the current test name available inside test methods
    public TestName name = new TestName();

    protected final Logger logger = LoggerFactory.getLogger(DominusJUnit4TestBase.class);

    protected static ResourceLoader resourceLoader = new DefaultResourceLoader();
    protected static PrintStream out = System.out;

    protected static final String TEST_SCHEMA = "employees";
    protected static final String STAGE_SCHEMA = "iops_schema";
    protected static final String TEST_TABLE = "employees";
    protected DataSource sourceMysqlDS;
    protected DataSource stageMysqlDS;

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

    protected Random random = new Random();

    protected ExecutorService executorService = Executors.newFixedThreadPool(4);

    public static void println(String color, Object x) {
        out.print(color);
        out.println(x);
        out.print(ANSI_RESET);
    }

    public static void printf(String color, String format, Object... args) {
        out.print(color);
        out.printf(format, args);
        out.print(ANSI_RESET);
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected static final int KB = 1024;
    protected static final int MB = 1048576;
    protected static final long GB = 1073741824L;
    protected static final long oneDay = 1000 * 60 * 60 * 24;
    protected static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");

    protected static final long Minute = 60000;
    protected static final long Second = 1000;

    public DominusJUnit4TestBase() {
        super();
    }

    protected boolean isLocalEnvironment() {
        return environment.getActiveProfiles()[0].contains("local");
    }

    protected String activeProfile() {
        return environment.getActiveProfiles()[0];
    }

    protected File createSampleFile(int size) throws IOException {
        File file = File.createTempFile("DominusBaseTestCase", ".txt");
        file.deleteOnExit();

        Writer writer = new OutputStreamWriter(new FileOutputStream(file));
        //total 50 char
        for (int i = 0; i < size / 50; i++) {
            writer.write("abcdefghijklmnopqrstuvwxyz\n");
            writer.write("0123456789011234567890\n");
        }
        writer.close();
        logger.info("Create Sample File: {}b", size);
        return file;
    }

    @Before
    public void setUp() throws Exception {

        out.print(ANSI_CYAN);
        out.printf("*************************[%s] %s setUp*************************\n", this.getClass().getSimpleName(), name.getMethodName());
        out.printf("[Spring Active Profile] %s\n", environment.getActiveProfiles()[0]);
        assertTrue("[Global Properties] is empty", properties.size() > 0);
        out.println("[Global Properties]:" + properties.size());


        assertNotNull(properties);
        doSetUp();
        out.printf("*************************[%s] %s setUp*************************\n", this.getClass().getSimpleName(), name.getMethodName());
        out.print(ANSI_RESET);
    }

    protected void doSetUp() throws Exception {

    }

    protected void doTearDown() throws Exception {

    }


    @After
    public void tearDown() throws Exception {

        out.print(ANSI_CYAN);
        out.printf("*************************[%s] %s tearDown*************************\n", this.getClass().getSimpleName(), name.getMethodName());
        doTearDown();
        //console color workaround
        Thread.sleep(500);
        out.printf("*************************[%s] %s tearDown*************************\n", this.getClass().getSimpleName(), name.getMethodName());
        out.print(ANSI_RESET);
    }


    //Redirect console output to string
    protected static ByteArrayOutputStream baos = new ByteArrayOutputStream();
    protected static PrintStream ps1 = new PrintStream(baos);
    protected static ByteArrayOutputStream errors = new ByteArrayOutputStream();
    protected static PrintStream ps2 = new PrintStream(errors);
    protected static PrintStream stdOut = System.out;
    protected static PrintStream stdError = System.err;

    public static void preCapturedStdout() {
        System.setOut(ps1);
        System.setErr(ps2);
    }

    public static String capturedStdout() {
        System.out.flush();
        System.err.flush();
        System.setOut(stdOut);
        System.setErr(stdError);
        String output = baos.toString();
        String error = errors.toString();
        baos.reset();
        errors.reset();
        println(ANSI_RED, error);
        return output;
    }

    public int sum(int[] intArray) {
        int sum = 0;
        for (int i : intArray) {
            sum += i;
        }
        return sum;
    }
}
