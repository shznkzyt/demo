package com.demo.student.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(statements = {
        "DELETE FROM students",
        "INSERT INTO students (id, student_no, name, gender, birth_date, class_name, phone, email) "
                + "VALUES (1, 'S001', '张三', '男', '2005-01-01', '一班', '13812345678', 'zhangsan@example.com')",
        "INSERT INTO students (id, student_no, name, gender, birth_date, class_name, phone, email) "
                + "VALUES (2, 'S002', '李四', '女', '2005-02-02', '一班', '13987654321', 'lisi@example.com')",
        "INSERT INTO students (id, student_no, name, gender, birth_date, class_name, phone, email) "
                + "VALUES (3, 'S003', '王五', '男', '2005-03-03', '二班', '13711112222', 'wangwu@example.com')"
})
class StudentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsPageFromPageHelper() throws Exception {
        mockMvc.perform(get("/students?page=2&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.page").value(2))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].studentNo").value("S003"));
    }

    @Test
    void returnsMaskedStudentDetails() throws Exception {
        mockMvc.perform(get("/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("张三"))
                .andExpect(jsonPath("$.data.phone").value("138****5678"))
                .andExpect(jsonPath("$.data.email").value("z***@example.com"))
                .andExpect(jsonPath("$.data.birthDate").value("2005-01-01"))
                .andExpect(jsonPath("$.data.createdAt").value(org.hamcrest.Matchers.matchesPattern(
                        "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")))
                .andExpect(jsonPath("$.data.updatedAt").value(org.hamcrest.Matchers.matchesPattern(
                        "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")));
    }

    @Test
    void returnsBadRequestForInvalidPage() throws Exception {
        mockMvc.perform(get("/students?page=0&size=10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void returnsNotFoundForMissingStudent() throws Exception {
        mockMvc.perform(get("/students/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void returnsNotFoundForUnknownPath() throws Exception {
        mockMvc.perform(get("/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createsStudent() throws Exception {
        mockMvc.perform(post("/students")
                        .contentType("application/json")
                        .content("""
                                {
                                  "studentNo": "S004",
                                  "name": "赵六",
                                  "gender": "女",
                                  "birthDate": "2005-04-04",
                                  "className": "二班",
                                  "phone": "13612345678",
                                  "email": "zhaoliu@example.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("学生信息添加成功"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.studentNo").value("S004"))
                .andExpect(jsonPath("$.data.phone").value("136****5678"));
    }

    @Test
    void rejectsDuplicateStudentNo() throws Exception {
        mockMvc.perform(post("/students")
                        .contentType("application/json")
                        .content("""
                                {"studentNo":"S001","name":"重复学号"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updatesStudent() throws Exception {
        mockMvc.perform(put("/students/1")
                        .contentType("application/json")
                        .content("""
                                {
                                  "studentNo": "S001",
                                  "name": "张三丰",
                                  "gender": "男",
                                  "birthDate": "2005-01-01",
                                  "className": "三班",
                                  "phone": "13800001111",
                                  "email": "new@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("学生信息修改成功"))
                .andExpect(jsonPath("$.data.name").value("张三丰"))
                .andExpect(jsonPath("$.data.className").value("三班"))
                .andExpect(jsonPath("$.data.phone").value("138****1111"));
    }

    @Test
    void deletesStudent() throws Exception {
        mockMvc.perform(delete("/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("学生信息删除成功"));

        mockMvc.perform(get("/students/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsIncompleteStudentRequest() throws Exception {
        mockMvc.perform(post("/students")
                        .contentType("application/json")
                        .content("{\"studentNo\":\"S004\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("姓名不能为空"));
    }

    @Test
    void rejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/students")
                        .contentType("application/json")
                        .content("{not-json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("请求体格式不正确，请检查 JSON 字段和日期格式"));
    }

    @Test
    void rejectsInvalidEmail() throws Exception {
        mockMvc.perform(post("/students")
                        .contentType("application/json")
                        .content("""
                                {"studentNo":"S004","name":"赵六","email":"invalid"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("邮箱格式不正确"));
    }
}
