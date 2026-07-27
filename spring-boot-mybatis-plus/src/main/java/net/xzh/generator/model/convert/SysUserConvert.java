package net.xzh.generator.model.convert;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import net.xzh.generator.model.request.SysUserSaveReq;
import net.xzh.generator.model.response.SysUserDetailResp;
import net.xzh.generator.model.response.SysUserListResp;
import net.xzh.generator.model.entity.SysUserDO;

import java.util.List;

/**
 * 用户管理数据转换对象
 * 使用MapStruct实现对象之间的自动映射
 *
 * @author xzh
 * @date 2026-07-27 08:40:33
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysUserConvert {

    SysUserConvert INSTANCE = Mappers.getMapper(SysUserConvert.class);
    
    SysUserDO convert(SysUserSaveReq from);
    
    SysUserDetailResp convert(SysUserDO sysUser);
    
    List<SysUserListResp> convertList(List<SysUserDO> list);
}