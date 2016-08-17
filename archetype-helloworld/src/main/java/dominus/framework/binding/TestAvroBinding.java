package dominus.framework.binding;


import dominus.framework.binding.bean.Employee;
import dominus.framework.junit.DominusJUnit4TestBase;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import static org.junit.Assert.assertTrue;

public class TestAvroBinding extends DominusJUnit4TestBase {

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
    }

    @Test
    public void testSerDe() throws IOException, ParseException {
        Employee employee1 = new Employee();
        employee1.setFirstName("shawn");
        employee1.setLastName("guo");
        employee1.setBirthDate(simpleDateFormat.parse("19850318-09:30:30.000"));
        employee1.setEmpNo(21086);
        employee1.setGender('M');
        employee1.setHireDate(new Date());

        Employee employee2 = new Employee();
        employee2.setFirstName("yuki");
        employee2.setLastName("ma");
        employee2.setBirthDate(simpleDateFormat.parse("19850529-14:30:30.000"));
        employee2.setEmpNo(66666);
        employee2.setGender('F');
        employee2.setHireDate(new Date());

        Schema empSchema = ReflectData.get().getSchema(Employee.class);
        out.println(empSchema.toString());

        DatumWriter<Employee> employeeDatumWriter = new ReflectDatumWriter<>(Employee.class);
        DataFileWriter<Employee> dataFileWriter = new DataFileWriter<>(employeeDatumWriter);
        dataFileWriter.create(empSchema, new File("/tmp/users.avro"));
        dataFileWriter.append(employee1);
        dataFileWriter.append(employee2);
        dataFileWriter.close();

        Schema empJsonSchema = new Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Employee\",\"namespace\":\"dominus.framework.binding.bean\",\"fields\":[{\"name\":\"empNo\",\"type\":\"int\"},{\"name\":\"birthDate\"," +
                "\"type\":{\"type\":\"long\",\"CustomEncoding\":\"DateAsLongEncoding\"}}," +
                "{\"name\":\"firstName\",\"type\":\"string\"},{\"name\":\"lastName\",\"type\":\"string\"},{\"name\":\"gender\",\"type\":{\"type\":\"int\",\"java-class\":\"java.lang.Character\"}},{\"name\":\"hireDate\",\"type\":{\"type\":\"long\",\"CustomEncoding\":\"DateAsLongEncoding\"}}]}");
        DatumReader<Employee> datumReader = new ReflectDatumReader<>(empJsonSchema);
        DataFileReader<Employee> dataFileReader = new DataFileReader<>(new File("/tmp/users.avro"), datumReader);

        Employee emp = null;
        // Reuse user object by passing it to next(). This saves us from allocating and garbage collecting many objects for files with many items.
        assertTrue(EqualsBuilder.reflectionEquals(employee1, dataFileReader.next(emp)));
        assertTrue(EqualsBuilder.reflectionEquals(employee2, dataFileReader.next(emp)));
    }
}


