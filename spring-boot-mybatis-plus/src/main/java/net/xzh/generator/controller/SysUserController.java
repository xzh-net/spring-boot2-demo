package net.xzh.generator.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import net.xzh.generator.model.request.SysUserPageQuery;
import net.xzh.generator.model.response.SysUserListResp;
import net.xzh.generator.model.request.SysUserSaveReq;
import net.xzh.generator.model.response.SysUserDetailResp;
import net.xzh.generator.service.SysUserService;
import net.xzh.generator.common.vo.PageResult;
import net.xzh.generator.common.vo.Result;
import net.xzh.generator.common.enums.BusinessType;
import net.xzh.generator.framework.aspectj.annotation.AuditLog;

/**
 * 用户管理控制层
 *
 * @author xzh
 * @date 2026-07-27 09:21:37
 */
@RestController
@RequestMapping("/sysuser")
@Tag(name = "用户管理", description = "用户管理API")
public class SysUserController {
    @Autowired
    private SysUserService sysUserService;

    @PostMapping("")
    @Operation(summary = "新增", description = "新增")
    @AuditLog(operation = "'新增用户管理:' + #form", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('sysUser:save')")
    public Result<?> save(@RequestBody @Validated SysUserSaveReq form) {
        sysUserService.save(form);
        return Result.success(null);
    }

    @PutMapping("{id}")
    @Operation(summary = "修改", description = "修改")
    @AuditLog(operation = "'修改用户管理:' + #form", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('sysUser:edit')")
    public Result<?> edit(@PathVariable Long id, @RequestBody SysUserSaveReq form) {
        sysUserService.edit(id, form);
        return Result.success(null);
    }

    @DeleteMapping("{id}")
    @Operation(summary = "删除", description = "删除")
    @AuditLog(operation = "'删除用户管理:' + #id", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('sysUser:remove')")
    public Result<?> remove(@PathVariable Long id) {
        sysUserService.removeById(id);
        return Result.success(null);
    }

    @DeleteMapping("batchRemove")
    @Operation(summary = "批量删除", description = "批量删除")
    @AuditLog(operation = "批量删除用户管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('sysUser:batchRemove')")
    public Result<?> batchRemove(@RequestBody Long[] ids) {
        sysUserService.removeByIds(Arrays.asList(ids));
        return Result.success(null);
    }

    @GetMapping("{id}")
    @Operation(summary = "查询详情", description = "查询详情")
    @AuditLog(operation = "'查询用户管理详情:' + #id", businessType = BusinessType.SELECT)
    @PreAuthorize("@ss.hasPermi('sysUser:get')")
    public Result<SysUserDetailResp> get(@PathVariable Long id) {
        return Result.success(sysUserService.get(id));
    }

    @GetMapping("")
    @Operation(summary = "分页查询列表", description = "分页查询列表")
    @AuditLog(operation = "'分页查询用户管理列表:' + #query", businessType = BusinessType.SELECT)
    @PreAuthorize("@ss.hasPermi('sysUser:list')")
    public Result<PageResult<SysUserListResp>> page(SysUserPageQuery query) {
        return Result.success(sysUserService.page(query));
    }

    @GetMapping("listAll")
    @Operation(summary = "查询全部数据", description = "查询全部数据")
    @AuditLog(operation = "'查询用户管理全部数据:' + #query", businessType = BusinessType.SELECT)
    @PreAuthorize("@ss.hasPermi('sysUser:listAll')")
    public Result<List<SysUserListResp>> listAll(SysUserPageQuery query) {
        return Result.success(sysUserService.listAll(query));
    }
}