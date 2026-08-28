package com.demo.student.service.impl;

import com.demo.common.response.PageResult;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.demo.student.entity.Student;
import com.demo.student.mapper.StudentMapper;
import com.demo.student.service.StudentService;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 学生业务接口实现类。
 * <p>
 * 负责组织学生数据的查询和写入流程，并通过事务注解明确读写边界；
 * 具体 SQL 由 {@link StudentMapper} 执行。
 * </p>
 */
@Service
public class StudentServiceImpl implements StudentService {

    private final StudentMapper studentMapper;

    /**
     * 通过构造器注入学生 MyBatis Mapper。
     * 构造器注入可以保证实例创建后依赖始终可用，也便于单元测试替换依赖。
     *
     * @param studentMapper 学生 Mapper
     */
    public StudentServiceImpl(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }

    /**
     * 根据 id 查询学生。
     *
     * @param id 学生主键 id
     * @return 存在则返回学生信息，否则为空
     */
    @Transactional(readOnly = true)
    @Override
    public Optional<Student> findById(Long id) {
        // Mapper 在记录不存在时返回 null，转换为 Optional 后调用方无需直接处理 null。
        return Optional.ofNullable(studentMapper.findById(id));
    }

    /**
     * 分页查询全部学生。
     *
     * @param page 页码，从 1 开始
     * @param size 每页条数
     * PageHelper 会拦截紧随其后的查询并自动追加分页 SQL，同时执行总数统计。
     * REPEATABLE_READ 隔离级别用于尽量保证同一事务内分页数据与统计结果的一致性。
     *
     * @return 包含当前页数据、总记录数、页码和每页条数的分页结果
     */
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    @Override
    public PageResult<Student> findPage(int page, int size) {
        // 必须在列表查询之前调用，分页参数只对紧随其后的 MyBatis 查询生效。
        PageHelper.startPage(page, size);

        // PageInfo 从 PageHelper 返回的列表中提取当前页数据和分页元数据。
        PageInfo<Student> pageInfo = new PageInfo<>(studentMapper.findAll());

        // 转换为项目统一的分页响应对象，避免将第三方 PageInfo 直接暴露给 Controller。
        return PageResult.of(pageInfo.getList(), pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize());
    }

    /**
     * 根据学号查询学生，用于详情查询或写入前的学号唯一性检查。
     *
     * @param studentNo 已去除首尾空格的学号
     * @return 学号已存在时返回学生实体，否则返回空 Optional
     */
    @Transactional(readOnly = true)
    @Override
    public Optional<Student> findByStudentNo(String studentNo) {
        // 统一使用 Optional 表示“可能不存在”，避免上层进行 null 判断。
        return Optional.ofNullable(studentMapper.findByStudentNo(studentNo));
    }

    /**
     * 新增学生并返回数据库中的完整记录。
     * <p>
     * insert 执行后，MyBatis 会将数据库生成的主键回填到 student.id，
     * 再次查询可获得数据库自动生成的创建时间和更新时间。
     * </p>
     *
     * @param student 已完成参数校验和文本规范化的学生实体
     * @return 包含主键及数据库时间字段的完整学生实体
     */
    @Transactional
    @Override
    public Student create(Student student) {
        // 插入学生基础字段，生成的主键会由 MyBatis 回填到 student 对象。
        studentMapper.insert(student);

        // 根据回填主键重新查询，以取得 created_at、updated_at 等数据库生成字段。
        return studentMapper.findById(student.getId());
    }

    /**
     * 根据实体中的 id 完整更新学生信息。
     *
     * @param student 包含主键和全部待更新字段的学生实体
     * @return 更新成功时返回数据库中的最新记录；目标记录不存在时返回空 Optional
     */
    @Transactional
    @Override
    public Optional<Student> update(Student student) {
        // 受影响行数为 0 表示执行更新时目标学生已经不存在。
        if (studentMapper.update(student) == 0) {
            return Optional.empty();
        }

        // 更新完成后重新查询，确保返回 updated_at 等数据库生成的最新值。
        return Optional.ofNullable(studentMapper.findById(student.getId()));
    }

    /**
     * 根据主键删除学生。
     *
     * @param id 学生主键 id
     * @return SQL 实际删除一行时返回 true；没有匹配记录时返回 false
     */
    @Transactional
    @Override
    public boolean deleteById(Long id) {
        // 将 Mapper 的受影响行数转换成更清晰的业务布尔结果。
        return studentMapper.deleteById(id) > 0;
    }
}
