package dominus.fastjdbc;


import com.google.common.io.Files;
import dominus.junit.DominusBaseTestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * EE: (ResultSetMetaData, ResultSet) => (NamedRow) => NamedParameterJdbcTemplate
 * EE: equals SQL(INSERT as SELECT) syntax
 * <p/>
 * Execute source sql and target sql in parallel.
 * ProcedureLine
 */
public class ParallelDataMovementTest extends DominusBaseTestCase {

    JdbcTemplate sourceTemplate;
    NamedParameterJdbcTemplate targetTemplate;
    ThreadPoolExecutor executor;
    private static BlockingQueue<Map<String, Object>> mQueue;
    Long totalSourceCount;
    private static AtomicInteger totalInsertCount = new AtomicInteger(0);
    private static int THREAD_SIZE = 5;
    String selectFromSQL;
    String insertToSQL;

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        executor.shutdown();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mQueue = new ArrayBlockingQueue<Map<String, Object>>(1000);
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"jdbc_context.xml"});
        sourceTemplate = new JdbcTemplate((DataSource) context.getBean("mysql_dataSource"));
        targetTemplate = new NamedParameterJdbcTemplate((DataSource) context.getBean("mysql_dataSource_local_iops"));

        System.out.printf("[Target Datastore]: %s records are deleted from target datastore.\n", targetTemplate.getJdbcOperations().update("delete from employees"));
        totalSourceCount = sourceTemplate.queryForObject("select count(*) from employees", Long.class);
        System.out.printf("[Source Datastore]: %s\n", totalSourceCount);
        assertTrue(totalSourceCount > 0);

        selectFromSQL = Files.toString(resourceLoader.getResource("classpath:script/sql/select_from.sql").getFile(), StandardCharsets.UTF_8);
        insertToSQL = Files.toString(resourceLoader.getResource("classpath:script/sql/insert_to.sql").getFile(), StandardCharsets.UTF_8);
        out.printf("[Select From SQL]\n%s\n", selectFromSQL);
        out.printf("[Insert To SQL]\n%s\n", insertToSQL);

        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(THREAD_SIZE);
    }

    public void testLocalMovement() {

        Runnable producer = new RowProducer(sourceTemplate, selectFromSQL);
        executor.execute(producer);
        for (int i = 0; i < THREAD_SIZE; i++) {
            Runnable consumer = new RowConsumer(targetTemplate, insertToSQL);
            executor.execute(consumer);
        }
        try {
            executor.shutdown();
            executor.awaitTermination(20, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(totalSourceCount.longValue(), totalInsertCount.longValue());
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

            template.query(selectSql, new ResultSetExtractor<Integer>() {
                @Override
                public Integer extractData(ResultSet rs) throws SQLException {

                    ResultSetMetaData rsm = rs.getMetaData();

                    while (rs.next()) {
                        Map<String, Object> namedRow = new LinkedHashMap<String, Object>(rsm.getColumnCount());
                        for (int i = 1; i <= rsm.getColumnCount(); i++) {
                            namedRow.put(rsm.getColumnName(i).toUpperCase(), rs.getObject(i));
                        }
                        try {
                            mQueue.put(namedRow);
                            System.out.printf("Row is put into queue..%s\n", rs.getRow());
                        } catch (InterruptedException e) {
                            throw new SQLException(e);
                        }
                    }

                    return mQueue.size();
                }
            });

            try {
                Map<String, Object> emptyMap = Collections.emptyMap();
                //end of jdbc result set
                for (int i = 0; i < THREAD_SIZE; i++) {
                    mQueue.put(emptyMap);
                }
            } catch (InterruptedException e) {
//                throw new SQLException(e); TODO
                e.printStackTrace();
            }
        }
    }

    /**
     * Basic Row Consumer
     */
    static class RowConsumer implements Runnable {
        private NamedParameterJdbcTemplate template;
        private String insertSql;

        public RowConsumer(NamedParameterJdbcTemplate template, String insertSql) {
            this.template = template;
            this.insertSql = insertSql;
        }

        @Override
        public void run() {
            boolean finished = false;
            while (!finished) {
                Map<String, Object> namedRow = Collections.emptyMap();
                try {
                    namedRow = mQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (namedRow.size() > 0) {
                    SqlParameterSource namedParameters = new MapSqlParameterSource(namedRow);
                    template.update(insertSql, namedParameters);
                    totalInsertCount.incrementAndGet();
                    System.out.printf("Row is take from queue...%s\n", Thread.currentThread().getName());
                } else {
                    System.out.printf("[END]-------------------------[END] %s\n", Thread.currentThread().getName());
                    finished = true;
                }
            }
        }
    }


}
