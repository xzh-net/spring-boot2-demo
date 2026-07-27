package net.xzh.generator.framework.aspectj.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import net.xzh.generator.framework.aspectj.annotation.AuditLog;

@Aspect
@Component
public class AuditLogAspect {

    @Pointcut("@annotation(auditLog)")
    public void auditLogPointcut(AuditLog auditLog) {
    }

    @Pointcut("@within(auditLog)")
    public void auditLogTypePointcut(AuditLog auditLog) {
    }

    @Around("auditLogPointcut(auditLog) || auditLogTypePointcut(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        return joinPoint.proceed();
    }
}