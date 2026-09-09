package com.demo.student.mapper;

import com.demo.student.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * students 表的 MyBatis 数据访问接口。
 * 方法名与 StudentMapper.xml 中的 SQL statement id 一一对应。
 */
@Mapper
public interface StudentMapper {

    /**
     * 根据 id 查询学生。
     *
     * @param id 学生主键 id，通过 @Param 显式命名为 SQL 中的 id 参数
     * @return 学生信息，不存在时为 null
     */
    Student findById(@Param("id") Long id);

    /**
     * 查询学生列表，分页由 PageHelper 拦截器完成。
     *
     * @return 按主键升序排列的学生列表；无数据时返回空列表
     */
    List<Student> findAll();

    /**
     * 新增学生。
     *
     * MyBatis 会将数据库生成的主键回填到 student.id。
     *
     * @param student 待持久化的学生实体
     * @return 插入成功时通常为 1
     */
    int insert(Student student);

    /**
     * 根据 id 修改学生。
     *
     * @param student 包含主键和全部待更新字段的学生实体
     * @return 找到并更新记录时为 1，否则为 0
     */
    int update(Student student);

    /**
     * 根据 id 删除学生。
     *
     * @param id 学生主键 id
     * @return 删除记录时为 1，目标不存在时为 0
     */
    int deleteById(@Param("id") Long id);
}
