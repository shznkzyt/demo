package com.demo.common.exception;

import com.demo.common.response.ApiResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局 REST 异常处理器。
 * 将 Controller 和后续调用链抛出的常见异常转换为统一的 {@link ApiResponse}，
 * 避免向客户端泄漏数据库细节或服务器堆栈信息。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 路径参数类型不匹配时返回友好提示。
     *
     * @param ex 参数类型转换异常
     * @return page/size 非数字时返回分页提示，其他类型不匹配时返回 id 提示，状态码均为 HTTP 400
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        // 根据发生转换错误的参数名，返回与具体接口匹配的操作提示。
        String name = ex.getName();
        if ("page".equals(name) || "size".equals(name)) {
            return badRequest("页码和每页条数必须是数字，例如 /students?page=1&size=10");
        }
        return badRequest("学生 id 必须是数字，例如 /students/1");
    }

    /**
     * 路径变量缺失或被转换成空时返回友好提示。
     *
     * @param ex 路径变量缺失异常
     * @return data 为空的 HTTP 400 统一失败响应
     */
    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingPathVariable(MissingPathVariableException ex) {
        // 不将 Spring 内部异常信息直接返回，改用调用方容易理解的业务提示。
        return badRequest("学生 id 不能为空，请传入数字 id，例如 /students/1");
    }

    /**
     * 访问不存在的路径时返回友好提示。
     *
     * @param ex 静态资源未找到异常
     * @return HTTP 404 和正确接口地址提示
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NoResourceFoundException ex) {
        // Spring 未匹配到 Controller 路由时会按静态资源查找失败处理，此处统一转换为接口提示。
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail("请求地址不正确，查询全部请访问 /students，按 id 查询请访问 /students/1"));
    }

    /**
     * 处理数据库连接、SQL 执行和数据约束等访问异常。
     * 详细异常只记录在服务端日志中，客户端仅收到通用提示。
     *
     * @param ex Spring 数据访问异常
     * @return HTTP 503，表示数据库服务当前不可用
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccess(DataAccessException ex) {
        // 记录完整堆栈，便于运维排查，同时防止敏感 SQL 信息出现在响应中。
        LOGGER.error("数据库访问失败", ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.fail("数据库暂时不可用，请稍后重试"));
    }

    /**
     * 捕获未被更具体处理器匹配的异常，作为接口的最后一道保护。
     *
     * @param ex 未预期异常
     * @return HTTP 500 和不包含内部实现细节的错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        // 未预期异常必须记录日志，否则统一响应会掩盖真实故障原因。
        LOGGER.error("处理请求时发生未预期异常", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("服务器内部错误，请稍后重试"));
    }

    /**
     * 构造统一的参数错误响应。
     *
     * @param message 参数错误说明
     * @return HTTP 400 失败响应
     */
    private static ResponseEntity<ApiResponse<Void>> badRequest(String message) {
        return ResponseEntity.badRequest().body(ApiResponse.fail(message));
    }
}
