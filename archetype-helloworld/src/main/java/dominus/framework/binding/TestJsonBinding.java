package dominus.framework.binding;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dominus.framework.binding.bean.Employee;
import dominus.framework.junit.DominusJUnit4TestBase;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;

import static org.junit.Assert.assertEquals;

/**
 * Jackson 1.x – org.codehaus.jackson.map
 * Jackson 2.x – com.fasterxml.jackson.databind
 * https://github.com/FasterXML/jackson-databind/
 */
public class TestJsonBinding extends DominusJUnit4TestBase {

    @Test
    public void testBean2Json() throws IOException, ParseException {
        Employee employee = new Employee();
        employee.setFirstName("shawn");
        employee.setLastName("guo");
        employee.setBirthDate(simpleDateFormat.parse("19850318-09:30:30.000"));
        employee.setEmpNo(21086);
        employee.setGender('M');

        ObjectMapper mapper = new ObjectMapper();// create once, reuse
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        mapper.writeValue(out, employee);
        String output = new String(out.toByteArray());
        System.out.println(output);
        assertEquals("{\"empNo\":21086,\"birthDate\":\"1985-03-18T01:30:30.000+0000\",\"firstName\":\"shawn\",\"lastName\":\"guo\",\"gender\":\"M\",\"hireDate\":null}", output);
    }
}
