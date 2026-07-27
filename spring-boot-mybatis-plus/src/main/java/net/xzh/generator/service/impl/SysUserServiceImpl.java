package net.xzh.generator.service.impl;

import org.springframework.stereotype.Service;
import net.xzh.generator.common.vo.PageResult;
import org.apache.commons.lang3.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.xzh.generator.framework.service.impl.SuperServiceImpl;
import net.xzh.generator.mapper.SysUserMapper;
import net.xzh.generator.service.SysUserService;
import net.xzh.generator.model.entity.SysUserDO;
import net.xzh.generator.model.response.SysUserDetailResp;
import net.xzh.generator.model.request.SysUserPageQuery;
import net.xzh.generator.model.response.SysUserListResp;
import net.xzh.generator.model.request.SysUserSaveReq;
import net.xzh.generator.model.convert.SysUserConvert;

import java.util.List;

/**
 * 用户管理服务接口实现
 *
 * @author xzh
 * @date 2026-07-27 09:21:37
 */
@Service
public class SysUserServiceImpl extends SuperServiceImpl<SysUserMapper, SysUserDO> implements SysUserService {

    @Override
    public void save(SysUserSaveReq form) {
        save(SysUserConvert.INSTANCE.convert(form));
    }

    @Override
    public void edit(Long id, SysUserSaveReq form) {
        SysUserDO sysUserDo = SysUserConvert.INSTANCE.convert(form);
        sysUserDo.setId(id);
        updateById(sysUserDo);
    }

    @Override
    public SysUserDetailResp get(Long id) {
        return SysUserConvert.INSTANCE.convert(getById(id));
    }

    @Override
    public PageResult<SysUserListResp> page(SysUserPageQuery query) {
        LambdaQueryWrapper<SysUserDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper                .like(StringUtils.isNotEmpty(query.getUsername()), SysUserDO::getUsername, query.getUsername())                .like(StringUtils.isNotEmpty(query.getPassword()), SysUserDO::getPassword, query.getPassword())                .like(StringUtils.isNotEmpty(query.getNickname()), SysUserDO::getNickname, query.getNickname())                .like(StringUtils.isNotEmpty(query.getHeadImgUrl()), SysUserDO::getHeadImgUrl, query.getHeadImgUrl())                .like(StringUtils.isNotEmpty(query.getMobile()), SysUserDO::getMobile, query.getMobile())                .like(StringUtils.isNotEmpty(query.getEmail()), SysUserDO::getEmail, query.getEmail())                .eq(query.getAccountBalance() != null, SysUserDO::getAccountBalance, query.getAccountBalance())                .eq(query.getStatus() != null, SysUserDO::getStatus, query.getStatus())                .eq(query.getRegistTime() != null, SysUserDO::getRegistTime, query.getRegistTime())                .eq(query.getSortBy() != null, SysUserDO::getSortBy, query.getSortBy())        ;
        Page<SysUserDO> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<SysUserDO> iPage = page(page, queryWrapper);
        return PageResult.restPage(iPage, SysUserConvert.INSTANCE.convertList(iPage.getRecords()));
    }

    @Override
    public List<SysUserListResp> listAll(SysUserPageQuery query) {
        LambdaQueryWrapper<SysUserDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper                .like(StringUtils.isNotEmpty(query.getUsername()), SysUserDO::getUsername, query.getUsername())                .like(StringUtils.isNotEmpty(query.getPassword()), SysUserDO::getPassword, query.getPassword())                .like(StringUtils.isNotEmpty(query.getNickname()), SysUserDO::getNickname, query.getNickname())                .like(StringUtils.isNotEmpty(query.getHeadImgUrl()), SysUserDO::getHeadImgUrl, query.getHeadImgUrl())                .like(StringUtils.isNotEmpty(query.getMobile()), SysUserDO::getMobile, query.getMobile())                .like(StringUtils.isNotEmpty(query.getEmail()), SysUserDO::getEmail, query.getEmail())                .eq(query.getAccountBalance() != null, SysUserDO::getAccountBalance, query.getAccountBalance())                .eq(query.getStatus() != null, SysUserDO::getStatus, query.getStatus())                .eq(query.getRegistTime() != null, SysUserDO::getRegistTime, query.getRegistTime())                .eq(query.getSortBy() != null, SysUserDO::getSortBy, query.getSortBy())        ;
        List<SysUserDO> list = list(queryWrapper);
        return SysUserConvert.INSTANCE.convertList(list);
    }
    
}