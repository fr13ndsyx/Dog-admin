package net.dogadmin.base.module.support.repeatsubmit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.dogadmin.base.common.code.UserErrorCode;
import net.dogadmin.base.common.domain.ResponseDTO;
import net.dogadmin.base.module.support.repeatsubmit.annoation.RepeatSubmit;
import net.dogadmin.base.module.support.repeatsubmit.ticket.AbstractRepeatSubmitTicket;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Slf4j
public class RepeatSubmitAspect {

    private final AbstractRepeatSubmitTicket repeatSubmitTicket;

    /**
     * 获取凭证信息
     */
    public RepeatSubmitAspect(AbstractRepeatSubmitTicket repeatSubmitTicket) {
        this.repeatSubmitTicket = repeatSubmitTicket;
    }

    /**
     * 定义切入点
     */
    @Around("@annotation(net.dogadmin.base.module.support.repeatsubmit.annoation.RepeatSubmit)")
    public Object around(ProceedingJoinPoint point) throws Throwable {


        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return point.proceed();
        }

        /**
         * 第一步：生成防重复提交的 ticket凭证
         * ticket 是根据 Request对象 自定义 生成的，可以加入请求user相关属性作为生成要素
         */

        HttpServletRequest request = attributes.getRequest();
        String ticket = this.repeatSubmitTicket.generateTicket(request);
        if (StringUtils.isEmpty(ticket)) {
            return point.proceed();
        }

        Method method = ((MethodSignature) point.getSignature()).getMethod();
        RepeatSubmit annotation = method.getAnnotation(RepeatSubmit.class);
        Long intervalMilliSecond = (long) annotation.intervalMilliSecond();

        /**
         * 第二步：根据 ticket 凭证进行 加锁
         * 能加锁，则可以执行
         * 若不能加锁，则证明还是时间间隔interval中
         */

        boolean lockSuccessFlag = this.repeatSubmitTicket.tryLock(ticket, System.currentTimeMillis(), intervalMilliSecond);
        if (!lockSuccessFlag) {
            return ResponseDTO.error(UserErrorCode.REPEAT_SUBMIT);
        }

        try {
            return point.proceed();
        } catch (Throwable throwable) {
            log.error(throwable.getMessage(), throwable);
            throw throwable;
        } finally {
            this.repeatSubmitTicket.unLock(ticket, intervalMilliSecond);
        }
    }

}
