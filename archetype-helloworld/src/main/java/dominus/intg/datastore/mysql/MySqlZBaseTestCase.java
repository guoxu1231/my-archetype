package dominus.intg.datastore.mysql;


import dominus.framework.junit.DominusJUnit4TestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

import javax.sql.DataSource;


@ContextConfiguration(locations = {"classpath:spring-container/mysql_jdbc_context.xml"})
public class MySqlZBaseTestCase extends DominusJUnit4TestBase {

    @Autowired
    @Qualifier("mysql_dataSource")
    protected DataSource sourceMysqlDS;
    @Autowired
    @Qualifier("mysql_dataSource_local_iops")
    protected DataSource stageMysqlDS;

    @Autowired
    @Qualifier("mysql_ddl_dataSource")
    protected DataSource ddlDS;

}
