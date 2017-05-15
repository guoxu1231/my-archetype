package dominus.intg.datastore;


import dominus.framework.junit.DominusJUnit4TestBase;
import org.apache.commons.io.FileUtils;
import org.h2.jdbc.JdbcSQLException;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

public class RDMS_H2_TestH2Database extends DominusJUnit4TestBase {

    Connection h2Connection;

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();

        Class.forName("org.h2.Driver");
        h2Connection = DriverManager.getConnection("jdbc:h2:/tmp/h2database/mytest", "sa", "");
        String s = "CREATE TABLE test  (id INTEGER, name char(50), last_name char(50), age INTEGER)";
        try {
            Statement sst = h2Connection.createStatement();
            sst.executeUpdate(s);
        } catch (JdbcSQLException e) {
            assertTrue(e.toString().contains("Table \"TEST\" already exists; SQL statement:"));
        }
        assertTrue(new File("/tmp/h2database/mytest.mv.db").exists());
    }

    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();
        h2Connection.close();
        assertTrue(FileUtils.deleteQuietly(new File("/tmp/h2database")));
        out.println("/tmp/h2database is deleted...");
    }

    @Test
    public void testReadWrite() {
        JdbcTemplate template = new JdbcTemplate(new SingleConnectionDataSource(h2Connection, false));
        assertEquals(1, template.update("insert into test values(1, 'shawguo','guo',30)"));
        assertEquals("shawguo", template.queryForMap("select * from test").get("name"));
    }
}
