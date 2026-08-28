package com.demo.student.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "学号不能为空")
    @Size(max = 32, message = "学号长度不能超过 32 个字符")
    private String studentNo;

    /** 学生姓名，必填，最长 64 个字符。 */
    @NotBlank(message = "姓名不能为空")
    @Size(max = 64, message = "姓名长度不能超过 64 个字符")
    private String name;

    /** 性别，可选，最长 16 个字符。 */
    @Size(max = 16, message = "性别长度不能超过 16 个字符")
    private String gender;

    /** 出生日期，可选；JSON 格式固定为 yyyy-MM-dd。 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    /** 班级名称，可选，最长 64 个字符。 */
    @Size(max = 64, message = "班级名称长度不能超过 64 个字符")
    private String className;

    /** 手机号，可选，最长 32 个字符。 */
    @Size(max = 32, message = "手机号长度不能超过 32 个字符")
    private String phone;

    /** 电子邮箱，可选，最长 128 个字符。 */
    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱长度不能超过 128 个字符")
    private String email;
}
