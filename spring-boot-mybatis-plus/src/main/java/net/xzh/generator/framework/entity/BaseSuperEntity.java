package net.xzh.generator.framework.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Getter;
import lombok.Setter;

/**
 * 持久层DO单主键基础实体
 *
 * @param <T> 泛型类型，必须继承自Model
 */
@Setter
@Getter
public class BaseSuperEntity<T extends Model<?>> extends Model<T> {
    /**
     * 序列化版本UID，用于Java序列化机制
     */
    private static final long serialVersionUID = 6391506203208757336L;
    /**
     * 主键ID，使用@TableId注解标记，类型为NONE表示跟随全局配置
     */
    @TableId(type = IdType.NONE)
    private Long id;

}
