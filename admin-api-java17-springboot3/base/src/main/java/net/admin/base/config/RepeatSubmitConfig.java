package net.admin.base.config;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import net.admin.base.common.constant.StringConst;
import net.admin.base.common.util.RequestUtil;
import net.admin.base.module.support.repeatsubmit.RepeatSubmitAspect;
import net.admin.base.module.support.repeatsubmit.ticket.RepeatSubmitRedisTicket;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RepeatSubmitConfig {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Bean
    public RepeatSubmitAspect repeatSubmitAspect() {
        RepeatSubmitRedisTicket ticket = new RepeatSubmitRedisTicket(redisTemplate, this::ticket);
        return new RepeatSubmitAspect(ticket);
    }

    /**
     * 获取指明某个用户的凭证
     */
    private String ticket(HttpServletRequest request) {
        Long userId = RequestUtil.getRequestUserId();
        if (null == userId) {
            return StringConst.EMPTY;
        }
        return request.getServletPath() + "_" + userId;
    }
}
