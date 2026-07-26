package net.xzh.generator.framework.service;


import com.baomidou.mybatisplus.extension.service.IService;

/**
 * Service 接口（ T 是实体 ）
 * @author xzh
 */
public interface SuperService<T> extends IService<T> {

	// 幂等新增
	// 幂等更新
}
