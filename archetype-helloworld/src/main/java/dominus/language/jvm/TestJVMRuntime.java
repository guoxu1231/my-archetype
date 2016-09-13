package dominus.language.jvm;


import dominus.framework.junit.DominusJUnit4TestBase;
import org.junit.Test;

public class TestJVMRuntime extends DominusJUnit4TestBase {

    @Test
    public void testShutdownHook() {

        Runtime.getRuntime().addShutdownHook(new Thread("JVMShutdownHook") {
            @Override
            public void run() {
                System.out.println("JVMShutdownHook is invoked.");
            }
        });
        int i = 0;
        while (true) {
            System.out.println("loop...." + i++);
            sleep(1000);
            if (i == 10) Runtime.getRuntime().exit(0);
        }
    }

}
