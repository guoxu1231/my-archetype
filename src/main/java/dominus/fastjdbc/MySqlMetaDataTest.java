package dominus.fastjdbc;


import dominus.junit.DominusBaseTestCase;
import dominus.junit.annotation.MySqlDataSource;
import org.springframework.util.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * EE: mysql test_db(local) is required
 * EE: database model reverse engineering
 * JDBC Standard: DatabaseMetaData / ResultSetMetaData
 * database medadata tables
 * This interface is implemented by driver vendors to let users know the capabilities of a Database Management System (DBMS) in combination with the driver based on JDBCTM technology ("JDBC driver") that is used with it;
 * A user for this interface is commonly a tool that needs to discover how to deal with the underlying DBMS.
 * <p/>
 * Works on any ISO database, as well as 'non-standard' technologies,
 * such as ODBC for Sage, Rdb, Datacom or any other JDBC or ODBC Driver.
 */
@MySqlDataSource
public class MySqlMetaDataTest extends DominusBaseTestCase {

    public void testDatabaseMetaData_getTables() throws SQLException {
        DatabaseMetaData metaData = localMysqlDS.getConnection().getMetaData();
        out.println(metaData);
        ResultSet tables = metaData.getTables(TEST_SCHEMA, "%", "%", new String[]{"TABLE"});
        List<String> tableList = new ArrayList<>();
        while (tables.next()) {
            String table_cat = tables.getString("TABLE_CAT");
            String table_schem = tables.getString("TABLE_SCHEM");
            String table_name = tables.getString("TABLE_NAME");
            String remarks = tables.getString("REMARKS");
            String ttype = tables.getString("TABLE_TYPE");

            tableList.add(table_name);
            out.printf("[TABLE_CAT] %s,[TABLE_SCHEM] %s [TABLE_NAME] %s [REMARKS] %s [TABLE_TYPE] %s\n",
                    table_cat, table_schem, table_name, remarks, ttype);
        }
        Collections.sort(tableList);
        assertEquals("departments,dept_emp,dept_manager,employees,salaries,titles",
                StringUtils.collectionToDelimitedString(tableList, ","));
    }

    public void testDatabaseMetaData_getColumns() throws SQLException {
        DatabaseMetaData metaData = localMysqlDS.getConnection().getMetaData();
        ResultSet columns = metaData.getColumns(TEST_SCHEMA, "", TEST_TABLE, "%");
        List<String> columnList = new ArrayList<>();
        while (columns.next()) {
            String coltable = columns.getString("TABLE_NAME");
            String colname = columns.getString("COLUMN_NAME");
            // coldt     = clst.getString("DATA_TYPE")
            String coltypename = columns.getString("TYPE_NAME");
            Integer colsize = Integer.valueOf(columns.getString("COLUMN_SIZE"));
            String colnull = columns.getString("NULLABLE");
            String colremark = columns.getString("REMARKS");
            Integer collen = columns.getInt("CHAR_OCTET_LENGTH");
            Integer colpos = columns.getInt("ORDINAL_POSITION");

            columnList.add(colname);
            out.printf("[TABLE_NAME] %s, [COLUMN_NAME] %s, [ORDINAL_POSITION] %s [TYPE_NAME] %s, [COLUMN_SIZE] %s [NULLABLE] %s  [CHAR_OCTET_LENGTH] %s\n",
                    coltable, colname, colpos, coltypename, colsize, colnull, collen);
        }
        assertEquals("emp_no,birth_date,first_name,last_name,gender,hire_date",
                StringUtils.collectionToDelimitedString(columnList, ","));
    }


    /**
     * klst = self.metadata.getPrimaryKeys(self.srccatalog, self.srcschema, tablename)
     * <p/>
     * sqlpk = "insert into SNP_REV_KEY (MOD_GUID,TABLE_NAME,KEY_NAME,CONS_TYPE,IND_ACTIVE,CHECK_FLOW,CHECK_STAT) values (?,?,?,'PK','1','1','1')"
     * sqlpkc = "insert into SNP_REV_KEY_COL (MOD_GUID,TABLE_NAME,KEY_NAME,COL_NAME,POS) values (?,?,?,?,?)"
     * <p/>
     * pspk = self.repoCx.prepareStatement(sqlpk)
     * pspkc = self.repoCx.prepareStatement(sqlpkc)
     * <p/>
     * pkname=''
     * while klst.next():
     * previouspkname=pkname
     * tablename = klst.getString("TABLE_NAME")
     * colname   = klst.getString("COLUMN_NAME")
     * keyseq    = klst.getShort("KEY_SEQ")
     * pkname    = klst.getString("PK_NAME")
     * if not (pkname): pkname='PK_%s' % tablename
     * <p/>
     * # If a new PK was detected
     * if pkname<>previouspkname:
     * pspk.clearParameters()
     * self.logger.log(java.util.logging.Level.INFO, '  Primary Key: %s' %  pkname)
     * pspk.setString(1,self.iMod)
     * pspk.setString(2,tablename)
     * pspk.setString(3,pkname)
     * pspk.executeUpdate()
     * self.logger.log(java.util.logging.Level.INFO, '     Primary Column: %d, %s' %  (keyseq, colname))
     */
    public void testDatabaseMetaData_getKeys() {

    }

    public void testResultSetMetaData() throws SQLException {

        Connection conn = localMysqlDS.getConnection();
        Statement stmt = null;
        String query = "select birth_date, first_name, last_name, gender, hire_date, emp_no from employees";

        stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        ResultSetMetaData rsm = rs.getMetaData();

        for (int i = 1; i <= rsm.getColumnCount(); i++) {
            out.printf("[Column Label] %s, [Column Name] %s, [Column Type] %s\n", rsm.getColumnLabel(i), rsm.getColumnName(i), rsm.getColumnType(i));
        }
        assertEquals(rsm.getColumnLabel(rsm.getColumnCount()), "emp_no");
    }
}
