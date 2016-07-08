DROP TABLE IF EXISTS `departments`;

CREATE TABLE `departments` (
  `dept_no` char(4) NOT NULL,
  `dept_name` varchar(40) NOT NULL,
  PRIMARY KEY (`dept_no`),
  UNIQUE KEY `dept_name` (`dept_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

SET autocommit=0;

BEGIN;
INSERT INTO `departments` VALUES ('d001','Marketing');
INSERT INTO `departments` VALUES ('d002','Finance');
INSERT INTO `departments` VALUES ('d003','Human Resources');
INSERT INTO `departments` VALUES ('d004','Production');
INSERT INTO `departments` VALUES ('d005','Development');
INSERT INTO `departments` VALUES ('d006','Quality Management');
INSERT INTO `departments` VALUES ('d007','Sales');
INSERT INTO `departments` VALUES ('d008','Research');
INSERT INTO `departments` VALUES ('d009','Customer Service');
UPDATE `departments` SET dept_name = 'DELL Alienware' WHERE dept_no = 'd009';
--SELECT DOES NOT generate binlog event
SELECT * FROM `departments`;
DELETE FROM `departments`;
COMMIT;