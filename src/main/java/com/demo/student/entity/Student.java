package com.demo.student.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * students 表对应的学生数据库实体。
 * Lombok 根据字段自动生成 getter 和 setter，MyBatis 通过这些访问器完成结果映射和参数读取。
 */
@Getter
@Setter
public class Student {

    /** 数据库自增主键。 */
    private Long id;

    /** 学号，对应 student_no 唯一列。 */
    private String studentNo;

    /** 学生姓名。 */
    private String name;

    /** 性别。 */
    private String gender;

    /** 出生日期。 */
    private LocalDate birthDate;

    /** 所属班级名称。 */
    private String className;

    /** 原始手机号，仅在数据库实体层保存，对外响应需要脱敏。 */
    private String phone;

    /** 原始电子邮箱，仅在数据库实体层保存，对外响应需要脱敏。 */
    private String email;

    /** 记录创建时间，由数据库生成。 */
    private LocalDateTime createdAt;

    /** 记录最后更新时间，由数据库在更新时刷新。 */
    private LocalDateTime updatedAt;
}
