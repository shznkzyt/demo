package com.demo.student.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 新增或完整修改学生信息时使用的请求对象。
 * Controller 负责校验必填项、字段长度以及文本规范化，避免直接使用数据库实体接收外部输入。
 */
@Getter
@Setter
public class StudentRequest {

    /** 学号，必填，系统内唯一，最长 32 个字符。 */
    private String studentNo;

    /** 学生姓名，必填，最长 64 个字符。 */
    private String name;

    /** 性别，可选，最长 16 个字符。 */
    private String gender;

    /** 出生日期，可选；JSON 格式固定为 yyyy-MM-dd。 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    /** 班级名称，可选，最长 64 个字符。 */
    private String className;

    /** 手机号，可选，最长 32 个字符。 */
    private String phone;

    /** 电子邮箱，可选，最长 128 个字符。 */
    private String email;
}
