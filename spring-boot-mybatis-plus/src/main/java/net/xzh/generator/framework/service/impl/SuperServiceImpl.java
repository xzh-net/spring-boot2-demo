package net.xzh.generator.framework.service.impl;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import net.xzh.generator.framework.service.SuperService;

/**
 * SuperService 实现类（ 泛型：M 是 mapper 对象，T 是实体 ）
 * @author xzh
 *
 */
public class SuperServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> implements SuperService<T> {


}
