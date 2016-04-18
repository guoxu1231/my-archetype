package dominus.intg.datastore.persistent.mybatis;


import dominus.framework.junit.DominusJUnit4TestBase;
import dominus.intg.datastore.testdata.Employee;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static junit.framework.TestCase.assertEquals;


/**
 * Spring integration for MyBatis 3
 * https://github.com/mybatis/spring
 */
@ContextConfiguration(locations = {"classpath:spring-container/mybatis-context.xml"})
public class TestMyBatisDao extends DominusJUnit4TestBase {

    @Autowired
    EmployeeMapper employeeMapper;

    @Test
    public void testSelectEmployee() {
        Employee employee = employeeMapper.selectEmployee(10001);
        assertEquals("Georgi", employee.getFirstName());
        out.println(employee);
    }


}
