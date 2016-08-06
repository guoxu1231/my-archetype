package dominus.framework.parser.sql;


import dominus.framework.junit.DominusJUnit4TestBase;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;


/**
 * tddl: sql query-> calculate sharding rules ->re-write db name, table name
 */
public class TestJSqlParser extends DominusJUnit4TestBase {

    final String QUERY_SQL_1 = "select emp_no, birth_date, first_name, last_name from employees.employees e \n" +
            "where e.emp_no='110511' and date(e.birth_date) between '1950-01-01' and '1953-12-31'";

    @Test
    public void testSQLParsing() throws JSQLParserException {
        //parse sql query
        printf(ANSI_BLUE, "[Original SQL]\n%s\n", QUERY_SQL_1);
        Select selectStatement = (Select) CCJSqlParserUtil.parse(QUERY_SQL_1);
        PlainSelect body = (PlainSelect) selectStatement.getSelectBody();
        assertEquals(Arrays.toString(body.getSelectItems().toArray()), "[emp_no, birth_date, first_name, last_name]");
        Table t = (Table) body.getFromItem();
        //calculate sharding rules & re-write db name, table name
        t.setSchemaName("employees-s-00");
        t.setName("employees-t-00");
        printf(ANSI_BLUE, "[New SQL]\n%s\n", selectStatement.toString());
    }
}
