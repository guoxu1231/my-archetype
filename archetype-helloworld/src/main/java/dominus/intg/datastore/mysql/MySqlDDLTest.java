package dominus.intg.datastore.mysql;


import dominus.framework.junit.DominusBaseTestCase;
import dominus.framework.junit.annotation.MySqlDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

@MySqlDataSource
public class MySqlDDLTest extends DominusBaseTestCase {

    JdbcTemplate stageDSTemplate;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        stageDSTemplate = new JdbcTemplate(stageMysqlDS);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        stageDSTemplate.execute("DROP TABLE IF EXISTS iops_schema.test_employees");
    }

    //CREATE TABLE ... LIKE Syntax
    public void testCreateStageTable() throws SQLException {
        stageDSTemplate.execute("DROP TABLE IF EXISTS iops_schema.test_employees");
        stageDSTemplate.execute("CREATE TABLE iops_schema.test_employees LIKE employees.employees");

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


}
