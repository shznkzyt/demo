package com.demo.student.service;

import com.demo.common.response.PageResult;
import com.demo.student.entity.Student;

import java.util.Optional;

/**
 * 学生信息业务接口。
 * 定义 Controller 可使用的查询和写入能力，并通过 Optional 明确表示记录可能不存在。
 */
public interface StudentService {

    /**
     * 根据 id 查询学生。
     *
     * @param id 已校验为正整数的学生主键 id
     * @return 存在则返回学生信息，否则为空
     */
    Optional<Student> findById(Long id);

    /**
     * 分页查询学生。
     *
     * @param page 页码，从 1 开始
     * @param size 每页条数
     * @return 包含当前页记录和统计信息的分页结果
     */
    PageResult<Student> findPage(int page, int size);

    /**
     * 新增学生。
     *
     * @param student 已完成校验和规范化、尚无主键的学生实体
     * @return 包含数据库生成主键和时间字段的学生信息
     */
    Student create(Student student);

    /**
     * 修改学生。
     *
     * @param student 包含主键及全部更新字段的学生实体
     * @return 修改后的学生信息；学生不存在时为空
     */
    Optional<Student> update(Student student);

    /**
     * 删除学生。
     *
     * @param id 已校验为正整数的学生主键 id
     * @return 删除成功为 true，学生不存在为 false
     */
    boolean deleteById(Long id);
}
