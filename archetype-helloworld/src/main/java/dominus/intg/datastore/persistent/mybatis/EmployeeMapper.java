package dominus.intg.datastore.persistent.mybatis;


import dominus.intg.datastore.testdata.Employee;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

public interface EmployeeMapper {

    @Results({
            @Result(property = "empNo", column = "emp_no"),
            @Result(property = "firstName", column = "first_name"),
            @Result(property = "lastName", column = "last_name"),
            @Result(property = "gender", column = "gender"),
            @Result(property = "birthDate", column = "birth_date"),
            @Result(property = "hireDate", column = "hire_date")
    })
    @Select("SELECT emp_no, first_name, last_name, gender, birth_date, hire_date from employees WHERE emp_no = #{empId}")
    Employee selectEmployee(int empId);

}
