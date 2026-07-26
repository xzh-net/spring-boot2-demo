package net.xzh.generator.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import net.xzh.generator.framework.repository.SuperMapper;
import net.xzh.generator.model.entity.SysUserDO;

/**
 * 用户管理数据库访问对象
 * 
 * @author xzh
 * @date 2026-07-26 17:02:53
 */
@Mapper
@Repository
public interface SysUserMapper extends SuperMapper<SysUserDO> {
}
