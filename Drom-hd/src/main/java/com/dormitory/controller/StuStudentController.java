package com.dormitory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.dto.StudentQueryDTO;
import com.dormitory.dto.StudentUpdateDTO;
import com.dormitory.service.IStuStudentService;
import com.dormitory.utils.R;
import com.dormitory.vo.StudentVO;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 学生详细信息管理控制器
 */
@Tag(name = "学生信息管理", description = "Admin/辅导员管理学生学籍、住宿等信息")
@RestController
@RequestMapping("/api/stu/student")
public class StuStudentController {
    
    @Autowired private IStuStudentService studentService;
    
    /**
     * 分页查询学生列表
     */
    @Operation(summary = "学生分页查询", description = "获取学生详细信息列表，包含关联数据")
    // @SaCheckPermission("stu:student:query")
    // 所有人（已登录用户）均可查询，但全局登录拦截器 SaTokenConfigure 仍会生效
    @GetMapping("/list")
    public R<Page<StudentVO>> list(@Valid StudentQueryDTO queryDTO) {
        Page<StudentVO> page = studentService.selectStudentPage(queryDTO);
        return R.ok(page);
    }
    
    /**
     * 获取单个学生详细信息
     */
    @Operation(summary = "获取学生详情", description = "根据学生 ID 获取详细信息")
    // @SaCheckPermission("stu:student:query")
    // 所有人（已登录用户）均可查询，但全局登录拦截器 SaTokenConfigure 仍会生效
    @GetMapping("/{studentId}")
    public R<StudentVO> getInfo(@PathVariable Long studentId) {
        StudentVO vo = studentService.getStudentVoById(studentId);
        return R.ok(vo);
    }
    
    /**
     * 修改学生信息 (管理员操作)
     */
    @Operation(summary = "修改学生信息", description = "更新学生学籍、住宿意愿等信息")
    @SaCheckPermission("stu:student:edit")
    @PutMapping
    public R<Void> update(@Valid @RequestBody StudentUpdateDTO updateDTO) {
        studentService.updateStudentInfo(updateDTO);
        return R.ok("学生信息更新成功");
    }
}