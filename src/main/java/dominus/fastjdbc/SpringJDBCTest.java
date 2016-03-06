package dominus.fastjdbc;


import com.google.common.io.Files;
import dominus.framework.junit.DominusBaseTestCase;
import dominus.framework.junit.annotation.MySqlDataSource;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.ParsedSql;

import java.nio.charset.StandardCharsets;

@MySqlDataSource
public class SpringJDBCTest extends DominusBaseTestCase {

    String sampleSQL_1;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        sampleSQL_1 = Files.toString(resourceLoader.getResource("classpath:script/sql/sql_sample_1.sql").getFile(), StandardCharsets.UTF_8);
    }

    public void testNamedParameterUtils() {

        ParsedSql sql = NamedParameterUtils.parseSqlStatement(sampleSQL_1);
        out.println(sql);
    }


}
