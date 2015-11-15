package dominus.fastjdbc;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import sun.nio.ch.ThreadPool;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ParallelDataMovement {

    private static BlockingQueue<Object[]> mQueue = new ArrayBlockingQueue<Object[]>(100);

    public static void main(String[] args) {
        ApplicationContext context =
                new ClassPathXmlApplicationContext(new String[]{"jdbc_context.xml"});

        JdbcTemplate mstemplate = new JdbcTemplate((DataSource) context.getBean("mysql_dataSource"));
        JdbcTemplate otemplate = new JdbcTemplate((DataSource) context.getBean("oracle_dataSource"));

        otemplate.execute("delete from employees");
        String total = mstemplate.queryForObject("select count(*) from employees", String.class);
        System.out.println(total);


        try {
            Thread.currentThread().sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        Runnable producer = new RowProducer(mstemplate, "select * from employees");
        executor.execute(producer);

        for (int i = 0; i < 9; i++) {
            Runnable consumer1 = new RowConsumer(otemplate, "insert into employees(emp_no,birth_date,first_name,last_name,gender,hire_date) values (?,?,?,?,?,?)");
            executor.execute(consumer1);
        }
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
                mQueue.put(new Object[0]);
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
                    System.out.printf("Row is take from queue...%s\n", Thread.currentThread().getName());

                } else
                    finished = true;
            }
        }
    }
}
