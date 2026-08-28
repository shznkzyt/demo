package com.demo.common.response;

import java.util.List;
import java.util.function.Function;

/**
 * 与具体分页组件无关的统一分页结果。
 *
 * @param <T> 当前页列表中的元素类型
 */
public class PageResult<T> {

    private List<T> list;
    private long total;
    private int page;
    private int size;

    /**
     * 构造分页结果。
     *
     * @param list  当前页数据
     * @param total 总记录数
     * @param page  当前页码，从 1 开始
     * @param size  每页条数
     * @param <T>   列表元素类型
     * @return 包含列表及全部分页元数据的新对象
     */
    public static <T> PageResult<T> of(List<T> list, long total, int page, int size) {
        // 通过静态工厂集中组装分页字段，调用方不需要逐项设置属性。
        PageResult<T> result = new PageResult<>();
        result.list = list;
        result.total = total;
        result.page = page;
        result.size = size;
        return result;
    }

    /**
     * 转换当前页元素，同时保留分页元数据。
     *
     * @param mapper 元素转换函数
     * @param <R>    目标元素类型
     * @return 元素类型已转换、分页元数据保持不变的新分页结果
     */
    public <R> PageResult<R> map(Function<? super T, R> mapper) {
        // 仅转换列表元素；total、page 和 size 直接沿用当前分页结果。
        return of(list.stream().map(mapper).toList(), total, page, size);
    }

    /**
     * 获取当前页数据。
     *
     * @return 当前页数据；无记录时为空列表
     */
    public List<T> getList() {
        return list;
    }

    /**
     * 获取总记录数。
     *
     * @return 总记录数
     */
    public long getTotal() {
        return total;
    }

    /**
     * 获取当前页码。
     *
     * @return 当前页码，从 1 开始
     */
    public int getPage() {
        return page;
    }

    /**
     * 获取每页条数。
     *
     * @return 请求或分页组件最终采用的每页条数
     */
    public int getSize() {
        return size;
    }

    /**
     * 获取总页数。
     *
     * 使用向上取整计算，例如 11 条数据、每页 10 条时结果为 2 页。
     *
     * @return 总页数；size 非正数时返回 0
     */
    public long getTotalPages() {
        // 防止除以 0，并为非法分页大小提供稳定返回值。
        if (size <= 0) {
            return 0;
        }
        // 整数除法向上取整，无需引入浮点数及其精度问题。
        return (total + size - 1) / size;
    }
}
