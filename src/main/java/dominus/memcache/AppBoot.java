package dominus.memcache;

import net.spy.memcached.MemcachedClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * memcached startup configuration:
 * memcached -vvv -p 11211 -U 0 -m 64
 * verbose log, port 11211, max memory 64m
 */

public class AppBoot {

    public static void main(String[] args) {

        // Launch the application
        System.out.println("Hello world");
        ApplicationContext context =
                new ClassPathXmlApplicationContext(new String[]{"memcache_context.xml"});


        //spymemcached, A simple, asynchronous, single-threaded memcached client written in java.
        MemcachedClient client = context.getBean(MemcachedClient.class);
        // Store a value (async) for one hour


        testSlabAllocation(client);

        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Retrieve a value.
        Object myObject = client.get("someKey");

    }

    //Test: Once a page is assigned to a class, it is never moved.
    public static void testSlabAllocation(MemcachedClient client) {

        //slab class  42: chunk size   1048576 perslab       1
        /**
         * Allocate 64 chunks in blow loop
         STAT 42:chunk_size 1048576
         STAT 42:chunks_per_page 1
         STAT 42:total_pages 64
         STAT 42:total_chunks 64
         STAT 42:used_chunks 64
         STAT 42:free_chunks 0
         STAT 42:free_chunks_end 0
         STAT 42:mem_requested 67106998
         STAT 42:get_hits 0
         STAT 42:cmd_set 64
         STAT 42:delete_hits 0
         STAT 42:incr_hits 0
         STAT 42:decr_hits 0
         STAT 42:cas_hits 0
         STAT 42:cas_badval 0
         STAT 42:touch_hits 0
         STAT active_slabs 1
         STAT total_malloced 67108864
         */

        int MAX_VALUE_SIZE = 524209;
        for (int i = 0; i < 64; i++)
            client.set(String.valueOf(i) + "_1024k size", 0, new StringBuilder(MAX_VALUE_SIZE));
        //524288 = 1M, however key + items occupy 74 bytes(=37char) (1048650, max is 1048576), will adjust the value to 524288-37-13(key)-24 char
        //key = 13char,item(cas) = 56bytes
        //An item will use space for the full length of its key, the internal datastructure for an item, and the length of the data.
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        //Test: Once a page is assigned to a class, it is never moved.
        for (int i = 0; i < 6400; i++) {
            int size = ThreadLocalRandom.current().nextInt(1, MAX_VALUE_SIZE + 1);
            System.out.println(size);
            client.set(String.valueOf(i) + "random size", 0, new StringBuilder(size));
        }
        /**
         * Result slab:
         STAT 42:chunk_size 1048576
         STAT 42:chunks_per_page 1
         STAT 42:total_pages 64
         STAT 42:total_chunks 64
         STAT 42:used_chunks 64
         */
    }


}