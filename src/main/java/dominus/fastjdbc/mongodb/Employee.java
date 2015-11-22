package dominus.fastjdbc.mongodb;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document
public class Employee {
    @Id
    private Integer emp_no;
    private Date birth_date;
    private String first_name;
    private String last_name;
    private String gender;
    private Date hire_date;

    public Employee() {

    }

    public Employee(Integer emp_no, Date birth_date, String first_name, String last_name, String gender, Date hire_date) {
        this.emp_no = emp_no;
        this.birth_date = birth_date;
        this.first_name = first_name;
        this.last_name = last_name;
        this.gender = gender;
        this.hire_date = hire_date;
    }

    @Override
    public String toString() {
        return String.format(
                "Employee[emp_no=%s, birth_date='%t', first_name='%s', last_name='%s', gender='%c', hire_date='%t']",
                emp_no, birth_date, first_name, last_name, gender, hire_date);
    }
}
