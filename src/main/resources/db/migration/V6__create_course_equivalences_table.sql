CREATE TABLE `course_equivalences` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `same_course_code` varchar(255) NOT NULL,
  `curi_no` varchar(255) NOT NULL,
  `curi_nm` varchar(255) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  `semester_at` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
