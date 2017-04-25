package dominus.intg.datastore.mysql;


import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class MySqlDDLTest extends MySqlZBaseTestCase {

    protected JdbcTemplate ddlTemplate;

    @Override
    protected void doSetUp() throws Exception {
        ddlTemplate = new JdbcTemplate(ddlDS);
    }

    @Override
    protected void doTearDown() throws Exception {
        ddlTemplate.execute("DROP TABLE IF EXISTS iops_schema.test_employees");
    }

    //CREATE TABLE ... LIKE Syntax
    @Test
    public void testCreateStageTable() throws SQLException {
        ddlTemplate.execute("DROP TABLE IF EXISTS iops_schema.test_employees");
        ddlTemplate.execute("CREATE TABLE iops_schema.test_employees LIKE employees.employees");

        //compare column metadata
        DatabaseMetaData sourceMetaData = sourceMysqlDS.getConnection().getMetaData();
        ResultSet sourceColumns = sourceMetaData.getColumns(TEST_SCHEMA, "", "employees", "%");

        DatabaseMetaData stageMetaData = stageMysqlDS.getConnection().getMetaData();
        ResultSet stageColumns = stageMetaData.getColumns(STAGE_SCHEMA, "", "test_employees", "%");

        while (sourceColumns.next() && stageColumns.next()) {
            assertTrue(sourceColumns.getString("TABLE_NAME").equals("employees"));
            assertTrue(stageColumns.getString("TABLE_NAME").equals("test_employees"));
            assertEquals(sourceColumns.getString("COLUMN_NAME"), stageColumns.getString("COLUMN_NAME"));
            assertEquals(sourceColumns.getString("ORDINAL_POSITION"), stageColumns.getString("ORDINAL_POSITION"));
            assertEquals(sourceColumns.getString("TYPE_NAME"), stageColumns.getString("TYPE_NAME"));
            assertEquals(sourceColumns.getString("COLUMN_SIZE"), stageColumns.getString("COLUMN_SIZE"));
        }
    }

    @Test
    public void testCreateDatabaseDDL() {
        String testDatabase = "TEST_DB_" + RandomStringUtils.randomAlphabetic(15);
        ddlTemplate.execute("CREATE DATABASE IF NOT EXISTS " + testDatabase);
        assertEquals(testDatabase, ddlTemplate.queryForObject(String.format("show databases like \'%s\'", testDatabase), String.class));
        ddlTemplate.execute("DROP DATABASE IF EXISTS " + testDatabase);
    }
}
