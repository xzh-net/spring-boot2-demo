package net.xzh.generator.service;

import net.xzh.generator.model.entity.SysUserDO;
import net.xzh.generator.model.response.SysUserDetailResp;
import net.xzh.generator.model.request.SysUserPageQuery;
import net.xzh.generator.model.request.SysUserSaveReq;
import net.xzh.generator.model.response.SysUserListResp;
import net.xzh.generator.common.vo.PageResult;
import net.xzh.generator.framework.service.SuperService;

import java.util.List;

/**
 * 用户管理服务接口
 *
 * @author xzh
 * @date 2026-07-27 08:40:33
 */
public interface SysUserService extends SuperService<SysUserDO> {

    void save(SysUserSaveReq form);

    void edit(Long id, SysUserSaveReq form);

    SysUserDetailResp get(Long id);

    PageResult<SysUserListResp> page(SysUserPageQuery query);

    List<SysUserListResp> listAll(SysUserPageQuery query);

}