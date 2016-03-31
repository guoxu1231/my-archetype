package dominus.fastjdbc;

import dominus.intg.datastore.mongodb.Employee;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 *
 * fast insert performance test
 *
 * Local Read Thread MAX
 * Total Records: 300024 Total Seconds: 36 IOPS:8322/n
 * Total Records: 300024 Total Seconds: 35 IOPS:8487/n
 * Total Records: 300024 Total Seconds: 33 IOPS:8847/n
 * <p/>
 * <p/>
 * <p/>
 *
 *
 * [MYSQL INNODB]
 * CDC Write thread MAX IOPS = 18  T[8]=IOPS:18
 * Local Write thread MAX IOPS = 1600  T[8]=IOPS:1555
 * <p/>
 * Total Records: 300024 Total Seconds: 205 IOPS:1463/n
 * Total Records: 300024 Total Seconds: 190 IOPS:1577/n
 * Total Records: 300024 Total Seconds: 188 IOPS:1593/n
 * Total Records: 300024 Total Seconds: 200 IOPS:1495/n
 * <p/>
 * [innodb_flush_log_at_trx_commit = 0]
 * Total Records: 300024 Total Seconds: 82 IOPS:3625/n
 * Total Records: 300024 Total Seconds: 81 IOPS:3680/n
 * Total Records: 300024 Total Seconds: 81 IOPS:3667/n
 * Total Records: 300024 Total Seconds: 82 IOPS:3616/n
 * Total Records: 300024 Total Seconds: 81 IOPS:3701/n
 * Total Records: 300024 Total Seconds: 78 IOPS:3807/n
 *
 *
 * [MONGODB]
 * Total Records: 300024 Total Seconds: 98 IOPS:3039/n
 *
 *
 *
 *
 */
public class IopsTest {

    private static BlockingQueue<Object[]> mQueue = new ArrayBlockingQueue<Object[]>(1000);
    private static AtomicInteger totalInsert = new AtomicInteger(0);
    private static Date start;
    private static int THREAD_SIZE = 20;

    public static void main(String[] args) {
        ApplicationContext context =
                new ClassPathXmlApplicationContext(new String[]{"jdbc_context.xml", "mongodb_context.xml"});

        JdbcTemplate mstemplate = new JdbcTemplate((DataSource) context.getBean("mysql_dataSource"));
        JdbcTemplate mscdctemplate = new JdbcTemplate((DataSource) context.getBean("mysql_dataSource_cdc_iops"));
        JdbcTemplate mslocaltemplate = new JdbcTemplate((DataSource) context.getBean("mysql_dataSource_local_iops"));
        JdbcTemplate otemplate = new JdbcTemplate((DataSource) context.getBean("oracle_dataSource"));
//        MongoOperations mongoOps = context.getBean(MongoTemplate.class);

        otemplate.execute("delete from employees");
        mscdctemplate.execute("delete from employees");
        mslocaltemplate.execute("delete from employees");
//        mongoOps.dropCollection("employee");
        String total = mstemplate.queryForObject("select count(*) from employees", String.class);
        System.out.println(total);


        try {
            Thread.currentThread().sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(20);
        Runnable producer = new RowProducer(mstemplate, "select * from employees order by employees.emp_no asc");
        executor.execute(producer);

        start = new Date();

        //oracle(cdc) test
        /**
         for (int i = 0; i < THREAD_SIZE; i++) {
         Runnable consumer1 = new RowConsumer(otemplate, "insert into employees(emp_no,birth_date,first_name,last_name,gender,hire_date) values (?,?,?,?,?,?)");
         executor.execute(consumer1);
         }
        **/

        //mysql(cdc) test

         for (int i = 0; i < THREAD_SIZE; i++) {
         Runnable consumer1 = new RowConsumer(mscdctemplate, "insert into employees(emp_no,birth_date,first_name,last_name,gender,hire_date) values (?,?,?,?,?,?)");
         executor.execute(consumer1);
         }


        //mysql(local) test
        /**
        for (int i = 0; i < THREAD_SIZE; i++) {
//          Runnable consumer1 = new RowConsumer(mslocaltemplate, "insert into employees(emp_no,birth_date,first_name,last_name,gender,hire_date) values (?,?,?,?,?,?)");
            Runnable consumer1 = new MongodbRowConsumer(mongoOps);
            executor.execute(consumer1);
        }
         **/


        executor.shutdown();
        try {
            executor.awaitTermination(20, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.printf("Total Records: %s Total Seconds: %d IOPS:%d/n", total, (new Date().getTime() - start.getTime()) / 1000,
                totalInsert.get() * 1000 / (new Date().getTime() - start.getTime()));

    }

    static class RowProducer implements Runnable {

        private JdbcTemplate template;
        private String selectSql;

        public RowProducer(JdbcTemplate template, String selectSql) {
            this.template = template;
            this.selectSql = selectSql;
        }

        @Override
        public void run() {

            template.query(selectSql, new RowCallbackHandler() {
                @Override
                public void processRow(ResultSet rs) throws SQLException {
                    int columnSize = rs.getMetaData().getColumnCount();
                    Object[] row = new Object[columnSize];
                    for (int i = 0; i < columnSize; i++)
                        row[i] = rs.getObject(i + 1);

                    try {
                        mQueue.put(row);
                        System.out.printf("Row is put into queue..%s\n", rs.getRow());
                    } catch (InterruptedException e) {
                        throw new SQLException(e);
                    }
                }
            });

            try {
                //end of jdbc result set
                for (int i = 0; i < THREAD_SIZE; i++) {
                    mQueue.put(new Object[0]);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Basic Row Consumer
     */
    static class RowConsumer implements Runnable {
        private JdbcTemplate template;
        private String insertSql;

        public RowConsumer(JdbcTemplate template, String insertSql) {
            this.template = template;
            this.insertSql = insertSql;
        }

        @Override
        public void run() {
            boolean finished = false;
            while (!finished) {
                Object[] row = new Object[0];
                try {
                    row = mQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (row.length > 0) {
                    template.update(insertSql, row);
                    totalInsert.incrementAndGet();
                    System.out.printf("Row is take from queue...%s\n", Thread.currentThread().getName());

                } else {
                    System.out.printf("[END]-------------------------[END] %s\n", Thread.currentThread().getName());
                    finished = true;
                }
            }
        }
    }

    /**
     * Mongodb Row Consumer
     */
    static class MongodbRowConsumer implements Runnable {
        private MongoOperations mongoOps;

        public MongodbRowConsumer(MongoOperations mongoOps) {
            this.mongoOps = mongoOps;
        }

        @Override
        public void run() {
            boolean finished = false;
            while (!finished) {
                Object[] row = new Object[0];
                try {
                    row = mQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (row.length > 0) {
//                    template.update(insertSql, row);
                    Employee employee = new Employee((Integer) row[0], new java.util.Date(((java.sql.Date) row[1]).getTime()),
                            (String) row[2], (String) row[3], (String) row[4], new java.util.Date(((java.sql.Date) row[5]).getTime()));
                    //class java.lang.Integer,class java.sql.Date,class java.lang.String,class java.lang.String,class java.lang.String,class java.sql.Date
//                    System.out.printf("%s,%s,%s,%s,%s,%s\n", row[0].getClass(), row[1].getClass(), row[2].getClass(), row[3].getClass(), row[4].getClass(),row[5].getClass());
                    mongoOps.insert(employee);
                    totalInsert.incrementAndGet();
                    System.out.printf("Row is take from queue...%s\n", Thread.currentThread().getName());

                } else {
                    System.out.printf("[END]-------------------------[END] %s\n", Thread.currentThread().getName());
                    finished = true;
                }
            }
        }
    }
}
