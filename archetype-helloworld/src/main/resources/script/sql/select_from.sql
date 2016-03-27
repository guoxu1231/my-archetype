select EMP_NO, UPPER(LAST_NAME) as LAST_NAME, FIRST_NAME, BIRTH_DATE, HIRE_DATE, GENDER
from employees
order by employees.emp_no asc