package dominus.framework.dao.jooq;


import dominus.intg.datastore.mysql.MySqlZBaseTestCase;
import dominus.intg.datastore.testdata.Employee;
import org.jooq.impl.DSL;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static dominus.framework.dao.jooq.db.Tables.EMPLOYEES_;
import static org.junit.Assert.assertEquals;

//https://www.jooq.org/doc/3.9/manual/getting-started/jooq-and-java-8/
public class TestJooQDSL extends MySqlZBaseTestCase {

    @Test
    public void testSimpleQuery() throws SQLException {
        try (Connection c = sourceMysqlDS.getConnection()) {
            String sql = "select * from employees";

            assertEquals(300024, DSL.using(c)
                    .fetch(sql)
                    // We can use lambda expressions to map jOOQ Records
                    .map(rs -> new Employee(
                            rs.getValue(EMPLOYEES_.EMP_NO),
                            rs.getValue(EMPLOYEES_.FIRST_NAME),
                            rs.getValue(EMPLOYEES_.FIRST_NAME)
                    )).size());
        }
    }


}
