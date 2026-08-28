package com.demo.common.response;

/**
 * REST 接口统一响应结构。
 *
 * @param <T> data 字段承载的业务数据类型
 */
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    /**
     * 构造查询成功的响应。
     *
     * @param data 业务数据
     * @param <T>  数据类型
     * 使用默认消息“查询成功”，适合普通查询接口。
     *
     * @return success 为 true 的成功响应
     */
    public static <T> ApiResponse<T> ok(T data) {
        // 委托给带消息的重载，集中维护响应对象的构造逻辑。
        return ok(data, "查询成功");
    }

    /**
     * 构造带自定义提示的成功响应。
     *
     * @param data    业务数据
     * @param message 提示信息
     * @param <T>     数据类型
     * @return success 为 true、包含指定数据和消息的成功响应
     */
    public static <T> ApiResponse<T> ok(T data, String message) {
        // 使用工厂方法统一设置三个响应字段，避免 Controller 重复创建和赋值。
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.message = message;
        response.data = data;
        return response;
    }

    /**
     * 构造失败或无数据时的响应。
     *
     * @param message 提示信息
     * @param <T>     数据类型
     * @return success 为 false 且 data 为 null 的失败响应
     */
    public static <T> ApiResponse<T> fail(String message) {
        // 失败响应不携带业务数据，客户端可统一依据 success 判断处理分支。
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        response.data = null;
        return response;
    }

    /**
     * 获取请求是否成功。
     *
     * @return 操作成功为 true，失败为 false
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 获取提示信息。
     *
     * @return 面向客户端的成功说明或错误原因
     */
    public String getMessage() {
        return message;
    }

    /**
     * 获取业务数据。
     *
     * @return 业务数据，无数据时为 null
     */
    public T getData() {
        return data;
    }
}
