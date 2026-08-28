package com.demo.student.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.demo.student.entity.Student;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 对外学生信息。联系方式仅返回脱敏值，避免直接暴露完整隐私数据。
 */
public record StudentResponse(
        Long id,
        String studentNo,
        String name,
        String gender,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate birthDate,
        String className,
        String phone,
        String email,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt) {

    /**
     * 将数据库实体转换为对外响应对象。
     * 手机号和邮箱在转换过程中完成脱敏，其他字段保持原值。
     *
     * @param student 非空的学生数据库实体
     * @return 可安全返回给客户端的学生响应对象
     */
    public static StudentResponse from(Student student) {
        // 联系方式必须经过专用脱敏方法处理，不可直接从实体复制到响应。
        return new StudentResponse(
                student.getId(),
                student.getStudentNo(),
                student.getName(),
                student.getGender(),
                student.getBirthDate(),
                student.getClassName(),
                maskPhone(student.getPhone()),
                maskEmail(student.getEmail()),
                student.getCreatedAt(),
                student.getUpdatedAt());
    }

    /**
     * 对手机号进行分段脱敏，同时兼容长度不足的非标准号码。
     *
     * @param phone 原始手机号
     * @return 空值原样返回；短号码大部分隐藏；标准号码保留前三位和后四位
     */
    private static String maskPhone(String phone) {
        // null 和空字符串没有可脱敏内容，保持原语义返回。
        if (phone == null || phone.isBlank()) {
            return phone;
        }
        // 去除首尾空格，防止空格影响长度判断和展示结果。
        String value = phone.trim();
        // 极短号码全部替换为星号，避免暴露任何有效数字。
        if (value.length() <= 4) {
            return "*".repeat(value.length());
        }
        // 中短号码仅保留首位和末两位。
        if (value.length() <= 7) {
            return value.charAt(0) + "***" + value.substring(value.length() - 2);
        }
        // 常规号码保留前三位和后四位，中间统一使用四个星号。
        return value.substring(0, 3) + "****" + value.substring(value.length() - 4);
    }

    /**
     * 对电子邮箱用户名部分进行脱敏，域名保留以便用户识别邮箱来源。
     *
     * @param email 原始电子邮箱
     * @return 合法结构保留用户名首字符和完整域名；结构异常时返回三个星号
     */
    private static String maskEmail(String email) {
        // null 和空字符串没有可脱敏内容，保持原语义返回。
        if (email == null || email.isBlank()) {
            return email;
        }
        // 统一清理首尾空格，并定位用户名与域名之间的分隔符。
        String value = email.trim();
        int separator = value.indexOf('@');
        // 缺少用户名、域名或 @ 时无法安全局部展示，因此隐藏整个值。
        if (separator <= 0 || separator == value.length() - 1) {
            return "***";
        }
        // 仅显示用户名首字符，域名部分保留用于辨识。
        return value.charAt(0) + "***" + value.substring(separator);
    }
}
