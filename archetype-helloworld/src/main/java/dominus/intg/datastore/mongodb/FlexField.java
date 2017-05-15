package dominus.intg.datastore.mongodb;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;

public class FlexField {
    @Property("field_name")
    private String fieldName;
    @Property("field_value")
    private String fieldValue;

    public FlexField() {
    }

    public FlexField(String fieldName, String fieldValue) {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    @Override
    public String toString() {
        return "FlexField{" +
                "fieldName='" + fieldName + '\'' +
                ", fieldValue='" + fieldValue + '\'' +
                '}';
    }
}
