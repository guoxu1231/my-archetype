package dominus.intg.datastore.mongodb;


import org.apache.commons.lang3.builder.EqualsBuilder;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Document
//morphia client
@Entity("employees")
@Indexes(
        @Index(value = "emp_no", fields = @Field("emp_no"))
)
public class Employee {
    @Id
    private ObjectId id;
    private Integer emp_no;
    private Date birth_date;
    private String first_name;
    private String last_name;
    private String gender;
    private Date hire_date;

    @Embedded("flex_fields")
    private List<FlexField> flexFields = new ArrayList<FlexField>();
    @Transient
    private Map<String, String> flexFieldsMap = new HashMap<>();

    public String getFlexField(String fieldName) {
        return flexFieldsMap.get(fieldName);
    }

    public void addFlexField(String fieldName, String fieldValue) {
        if (getFlexField(fieldName) == null)
            this.flexFields.add(new FlexField(fieldName, fieldValue));
    }

    @PostLoad
    private void initialize() {
        flexFields.stream().forEach(flexField -> flexFieldsMap.put(flexField.getFieldName(), flexField.getFieldValue()));
    }

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
                "Employee[emp_no=%s, birth_date='%tF',birth_date_long='%d' first_name='%s', last_name='%s', gender='%s', hire_date='%tF'], hire_date_long='%d\n" +
                        "%s'",
                emp_no, birth_date, birth_date.getTime(), first_name, last_name, gender, hire_date, hire_date.getTime(),Arrays.toString(flexFields.toArray()));
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
}
