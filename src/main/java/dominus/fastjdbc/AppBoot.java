package dominus.fastjdbc;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public class AppBoot {


    public static void main(String[] args) {
        ApplicationContext context =
                new ClassPathXmlApplicationContext(new String[]{"jdbc_context.xml"});

        JdbcTemplate template = new JdbcTemplate((DataSource) context.getBean("mysql_dataSource"));
        String total = template.queryForObject("select count(*) from employees", String.class);

        System.out.printf(total);


    }

}
