package com.demo.student.controller;

import com.demo.common.response.ApiResponse;
import com.demo.common.response.PageResult;
import com.demo.student.dto.StudentRequest;
import com.demo.student.dto.StudentResponse;
import com.demo.student.entity.Student;
import com.demo.student.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学生信息 REST 接口。
 * <p>
 * 负责接收和校验 HTTP 参数、调用学生业务服务，并将结果包装成统一的
 * {@link ApiResponse} 响应；数据库访问和事务处理交由 Service 层完成。
 * </p>
 */
@RestController
@RequestMapping("/students")
public class StudentController {

    private static final int DEFAULT_PAGE = 1;
    private static final int MAX_SIZE = 100;

    private final StudentService studentService;

    /**
     * 注入学生业务服务。
     *
     * @param studentService 学生业务服务
     */
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * 分页查询全部学生。
     *
     * @param page 页码，从 1 开始，默认 1
     * @param size 每页条数，默认 10，最大 100
     * 查询结果中的联系方式会通过 {@link StudentResponse#from(Student)} 自动脱敏。
     *
     * @return 参数合法时返回 HTTP 200 和分页数据；参数非法时返回 HTTP 400
     */
    @GetMapping({"", "/"})
    public ResponseEntity<ApiResponse<PageResult<StudentResponse>>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        // 页码从 1 开始，拒绝 null、0 和负数，避免生成无意义的分页 SQL。
        if (page == null || page < DEFAULT_PAGE) {
            return badRequest("页码必须从 1 开始，例如 /students?page=1&size=10");
        }
        // 每页至少查询一条数据。
        if (size == null || size < 1) {
            return badRequest("每页条数必须是正整数，例如 /students?page=1&size=10");
        }
        // 限制单次查询量，防止过大的请求占用数据库和网络资源。
        if (size > MAX_SIZE) {
            return badRequest("每页最多查询 " + MAX_SIZE + " 条");
        }
        // Service 返回数据库实体；在 Controller 层转换为脱敏的对外响应对象。
        PageResult<StudentResponse> result = studentService.findPage(page, size).map(StudentResponse::from);

        // 空结果仍返回成功状态，但使用更明确的业务提示。
        if (result.getTotal() == 0) {
            return ResponseEntity.ok(ApiResponse.ok(result, "暂无学生数据"));
        }
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * 根据学生 id 查询学生信息。
     *
     * @param id 学生主键 id，允许为空或非数字，由本方法校验后返回提示
     * @return 查询成功返回 HTTP 200；id 非法返回 HTTP 400；学生不存在返回 HTTP 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentResponse>> getById(@PathVariable String id) {
        // 路径变量使用 String 接收，以便自行返回清晰的非法参数提示。
        if (id == null || id.isBlank() || "null".equalsIgnoreCase(id)) {
            return badRequest("学生 id 不能为空，请传入数字 id，例如 /students/1");
        }
        // 将校验后的文本 id 转换为数据库主键类型。
        long studentId;
        try {
            studentId = Long.parseLong(id.trim());
        } catch (NumberFormatException ex) {
            return badRequest("学生 id 必须是数字，例如 /students/1");
        }
        // 数据库自增主键必须是正整数。
        if (studentId <= 0) {
            return badRequest("学生 id 必须是正整数，例如 /students/1");
        }
        // Optional 有值时转换并返回；无值时统一构造 404 响应。
        return studentService.findById(studentId)
                .map(StudentResponse::from)
                .map(student -> ResponseEntity.ok(ApiResponse.ok(student)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.fail("未找到 id 为 " + studentId + " 的学生信息")));
    }

    /**
     * 添加学生信息。
     *
     * @param request 请求体中的学生信息；学号和姓名必填，其余字段可为空
     * @return 创建成功返回 HTTP 201；参数非法返回 HTTP 400；学号重复返回 HTTP 409
     */
    @PostMapping({"", "/"})
    public ResponseEntity<ApiResponse<StudentResponse>> create(@RequestBody(required = false) StudentRequest request) {
        // 在进入业务层前统一校验必填项和数据库字段长度。
        String validationMessage = validate(request);
        if (validationMessage != null) {
            return badRequest(validationMessage);
        }
        // 将请求对象转换为持久化实体，同时清理所有文本字段的首尾空格。
        Student student = toStudent(request);

        // student_no 在数据库中具有唯一约束，提前检查可返回更友好的冲突提示。
        if (studentService.findByStudentNo(student.getStudentNo()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.fail("学号 " + student.getStudentNo() + " 已存在"));
        }
        // 新增后将完整实体转换为脱敏响应对象，不向客户端暴露原始联系方式。
        StudentResponse result = StudentResponse.from(studentService.create(student));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(result, "学生信息添加成功"));
    }

    /**
     * 修改学生信息。
     *
     * PUT 使用完整更新语义，请求体需要提供学号和姓名，未提供的可选字段将保存为 null。
     *
     * @param id 路径中的学生主键 id，必须是正整数
     * @param request 完整的学生信息
     * @return 修改成功返回 HTTP 200；参数非法返回 HTTP 400；学生不存在返回 HTTP 404；学号冲突返回 HTTP 409
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentResponse>> update(
            @PathVariable String id, @RequestBody(required = false) StudentRequest request) {
        // 统一解析 id，非法值用 null 表示并转换为 400 响应。
        Long studentId = parsePositiveId(id);
        if (studentId == null) {
            return badRequest("学生 id 必须是正整数，例如 /students/1");
        }
        // 修改接口与新增接口共用相同的字段规则，保持数据质量一致。
        String validationMessage = validate(request);
        if (validationMessage != null) {
            return badRequest(validationMessage);
        }
        // 先确认目标存在，以区分“目标不存在”和“学号冲突”两类错误。
        if (studentService.findById(studentId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("未找到 id 为 " + studentId + " 的学生信息"));
        }
        // 请求体不接收 id，路径参数是待修改记录主键的唯一来源。
        Student student = toStudent(request);
        student.setId(studentId);

        // 当前学生继续使用自己的原学号是合法的，只有被其他学生占用时才冲突。
        boolean studentNoUsed = studentService.findByStudentNo(student.getStudentNo())
                .filter(existing -> !studentId.equals(existing.getId()))
                .isPresent();
        if (studentNoUsed) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.fail("学号 " + student.getStudentNo() + " 已存在"));
        }
        // 更新与查询之间记录可能被并发删除，因此 Service 仍以 Optional 表示更新结果。
        return studentService.update(student)
                .map(StudentResponse::from)
                .map(result -> ResponseEntity.ok(ApiResponse.ok(result, "学生信息修改成功")))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.fail("未找到 id 为 " + studentId + " 的学生信息")));
    }

    /**
     * 删除学生信息。
     *
     * @param id 路径中的学生主键 id，必须是正整数
     * @return 删除成功返回 HTTP 200；id 非法返回 HTTP 400；学生不存在返回 HTTP 404
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        // 删除前先将字符串路径参数安全地转换为正整数主键。
        Long studentId = parsePositiveId(id);
        if (studentId == null) {
            return badRequest("学生 id 必须是正整数，例如 /students/1");
        }
        // 删除受影响行数为 0 表示目标学生不存在。
        if (!studentService.deleteById(studentId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("未找到 id 为 " + studentId + " 的学生信息"));
        }
        return ResponseEntity.ok(ApiResponse.ok(null, "学生信息删除成功"));
    }

    /**
     * 将字符串形式的路径参数解析为正整数学生 id。
     *
     * @param id 原始路径参数
     * @return 合法时返回正整数 id；为空、非数字、0 或负数时返回 null
     */
    private static Long parsePositiveId(String id) {
        // 将空白值和字符串 "null" 统一视为未提供 id。
        if (id == null || id.isBlank() || "null".equalsIgnoreCase(id)) {
            return null;
        }
        try {
            // trim 允许客户端无意携带首尾空格，但不接受小数或其他非数字字符。
            long value = Long.parseLong(id.trim());
            return value > 0 ? value : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * 校验新增和修改学生时共用的请求字段。
     * 字段长度与 students 表定义保持一致，避免由数据库截断或抛出异常。
     *
     * @param request 学生请求对象
     * @return 校验通过返回 null，否则返回第一条可直接展示给客户端的错误信息
     */
    private static String validate(StudentRequest request) {
        // 请求体缺失时，Spring 会因为 required=false 传入 null。
        if (request == null) {
            return "请求体不能为空";
        }
        // 学号对应数据库非空列，并承担学生的业务唯一标识。
        if (isBlank(request.getStudentNo())) {
            return "学号不能为空";
        }
        if (request.getStudentNo().trim().length() > 32) {
            return "学号长度不能超过 32 个字符";
        }
        // 姓名对应数据库非空列，不能只包含空白字符。
        if (isBlank(request.getName())) {
            return "姓名不能为空";
        }
        if (request.getName().trim().length() > 64) {
            return "姓名长度不能超过 64 个字符";
        }
        // 以下可选字段为空时长度按 0 处理，非空时按去除首尾空格后的长度校验。
        if (length(request.getGender()) > 16) {
            return "性别长度不能超过 16 个字符";
        }
        if (length(request.getClassName()) > 64) {
            return "班级名称长度不能超过 64 个字符";
        }
        if (length(request.getPhone()) > 32) {
            return "手机号长度不能超过 32 个字符";
        }
        if (length(request.getEmail()) > 128) {
            return "邮箱长度不能超过 128 个字符";
        }
        return null;
    }

    /**
     * 将接口请求对象转换为数据库实体。
     *
     * @param request 已通过 {@link #validate(StudentRequest)} 校验的请求对象
     * @return 规范化后的学生实体；主键和数据库时间字段由后续流程设置
     */
    private static Student toStudent(StudentRequest request) {
        Student student = new Student();
        // 必填字段已通过校验，可以安全去除首尾空格。
        student.setStudentNo(request.getStudentNo().trim());
        student.setName(request.getName().trim());
        // 可选文本统一将空字符串转换为 null，避免数据库同时保存 null 和空串。
        student.setGender(trimToNull(request.getGender()));
        student.setBirthDate(request.getBirthDate());
        student.setClassName(trimToNull(request.getClassName()));
        student.setPhone(trimToNull(request.getPhone()));
        student.setEmail(trimToNull(request.getEmail()));
        return student;
    }

    /**
     * 判断字符串是否为 null、空字符串或仅包含空白字符。
     *
     * @param value 待判断字符串
     * @return 为空白值返回 true，否则返回 false
     */
    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * 获取字符串去除首尾空格后的长度。
     *
     * @param value 待计算字符串
     * @return null 返回 0，否则返回 trim 后的字符数
     */
    private static int length(String value) {
        return value == null ? 0 : value.trim().length();
    }

    /**
     * 规范化可选文本字段。
     *
     * @param value 原始文本
     * @return 空白值返回 null，否则返回去除首尾空格后的文本
     */
    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    /**
     * 构造项目统一格式的 HTTP 400 响应。
     *
     * @param message 可展示给客户端的参数错误说明
     * @param <T> 响应业务数据类型
     * @return data 为 null 的失败响应
     */
    private static <T> ResponseEntity<ApiResponse<T>> badRequest(String message) {
        return ResponseEntity.badRequest().body(ApiResponse.fail(message));
    }
}
