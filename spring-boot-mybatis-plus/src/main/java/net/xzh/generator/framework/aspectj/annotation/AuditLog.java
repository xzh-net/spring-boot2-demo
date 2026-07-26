package net.xzh.generator.framework.aspectj.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.xzh.generator.common.model.BusinessType;

/**
 * 自定义注解：用于标记需要审计日志的方法或类 该注解可以用于类和方法上，运行时保留，并且会被包含在JavaDoc中
 * 
 * @date 2021年10月05日
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {
	/**
	 * 操作信息
	 * 
	 * @return 返回操作的具体描述信息
	 */
	String operation();

	/**
	 * 业务类型
	 * 
	 * @return 返回业务类型，默认为OTHER类型
	 */
	BusinessType businessType() default BusinessType.OTHER; // 设置默认业务类型为OTHER

}
