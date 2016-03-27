package dominus.language.hotspot;


import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enable flight recorder in jmc(java mission control)
 * -XX:+UnlockCommercialFeatures -XX:+FlightRecorder
 * <p/>
 * Enable GC Log
 * -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:/opt/Development/middleware/logs/gc.log
 * <p/>
 * <p/>
 * Heap Sizing -Xms and -Xmx (or: -XX:InitialHeapSize and -XX:MaxHeapSize)
 * -Xms100m -Xmx100m
 * <p/>
 * <p/>
 * -XX:+UseSerialGC
 * GC log output:
 * <p/>
 * [Default Young]
 * Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
 * Heap
 * def new generation   total 30720K, used 30720K [0xe9000000, 0xeb150000, 0xeb150000)
 * eden space 27328K, 100% used [0xe9000000, 0xeaab0000, 0xeaab0000)
 * from space 3392K, 100% used [0xeaab0000, 0xeae00000, 0xeae00000)
 * to   space 3392K,   0% used [0xeae00000, 0xeae00000, 0xeb150000)
 * tenured generation   total 68288K, used 68283K [0xeb150000, 0xef400000, 0xef400000)
 * the space 68288K,  99% used [0xeb150000, 0xef3fedd8, 0xef3fee00, 0xef400000)
 * compacting perm gen  total 16384K, used 4351K [0xef400000, 0xf0400000, 0xf3400000)
 * the space 16384K,  26% used [0xef400000, 0xef83fdb0, 0xef83fe00, 0xf0400000)
 * No shared spaces configured.
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * -XX:+UseParallelGC
 * <p/>
 * <p/>
 * <p/>
 * GC Logs Explain:
 * [GC [PSYoungGen: 11776K->7056K(23040K)] 36702K->31982K(91392K), 0.0074770 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]
 * not fully which triggers?
 */
public class GC_Trigger {

    public static void main(String[] args) {


        final int mb = 1024 * 1024;
        final int allocSize = 5120; //1k every sleep interval
        final int interval = 5; // 200k every second

        Map<SoftReference<StringBuffer>, Object> alloc_1 = new ConcurrentHashMap<SoftReference<StringBuffer>, Object>();
        Map<StringBuffer, Object> alloc_2 = new HashMap<>();

        Date value = new Date();
        while (true) {


//            alloc_1.put(new SoftReference<StringBuffer>(new StringBuffer(allocSize)), new Date());

            //(PS Eden Space -> PS Survivor Space) => PS Old
            alloc_2.put(new StringBuffer(allocSize), value);

            //Prints JVM memory utilization statistics
//            Runtime runtime = Runtime.getRuntime();
//            System.out.format("##### Heap utilization statistics [MB] #####  Used Memory:%s  Free Memory:%s  Max Memory:%s \r"
//                    , (runtime.totalMemory() - runtime.freeMemory()) / mb, runtime.freeMemory() / mb, runtime.maxMemory() / mb);

            try {

                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }


}
