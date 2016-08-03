package dominus.framework.binding;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dominus.framework.binding.bean.Employee;
import dominus.framework.junit.DominusJUnit4TestBase;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Jackson 1.x – org.codehaus.jackson.map
 * Jackson 2.x – com.fasterxml.jackson.databind
 * https://github.com/FasterXML/jackson-databind/
 */
public class TestJsonBinding extends DominusJUnit4TestBase {
    ObjectMapper mapper;

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        mapper = new ObjectMapper();// create once, reuse
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setDateFormat(simpleDateFormat);
    }

    @Test
    public void testBean2Json() throws IOException, ParseException {
        Employee employee = new Employee();
        employee.setFirstName("shawn");
        employee.setLastName("guo");
        employee.setBirthDate(simpleDateFormat.parse("19850318-09:30:30.000"));
        employee.setEmpNo(21086);
        employee.setGender('M');

        String json = convertToJSONString(employee);
        System.out.println(json);
        assertEquals("{\"empNo\":21086,\"birthDate\":\"19850318-09:30:30.000\",\"firstName\":\"shawn\",\"lastName\":\"guo\",\"gender\":\"M\",\"hireDate\":null}", json);
    }

    @Test
    public void testMap2Json() throws ParseException, IOException {
        Map<String, Object> empMap = new HashMap<>();
        empMap.put("firstName", "shawn");
        empMap.put("birthDate", simpleDateFormat.parse("19850318-09:30:30.000"));

        String json = convertToJSONString(empMap);
        System.out.println(json);
        assertEquals("{\"firstName\":\"shawn\",\"birthDate\":\"19850318-09:30:30.000\"}", json);
    }

    public String convertToJSONString(Object obj) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            mapper.writeValue(out, obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(out.toByteArray());
    }
}
