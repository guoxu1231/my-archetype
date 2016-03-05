insert into employees
(emp_no,birth_date,first_name,last_name,gender,hire_date)
values
(:EMP_NO,:BIRTH_DATE,UPPER(:FIRST_NAME),:LAST_NAME,:GENDER,:HIRE_DATE)