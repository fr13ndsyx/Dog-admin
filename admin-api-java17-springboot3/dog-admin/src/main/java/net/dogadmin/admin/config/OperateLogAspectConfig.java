package net.dogadmin.admin.config;

import net.dogadmin.base.module.support.operatelog.core.OperateLogAspect;
import net.dogadmin.base.module.support.operatelog.core.OperateLogConfig;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OperateLogAspectConfig extends OperateLogAspect{

    /**
     * 配置信息
     */
    @Override
    public OperateLogConfig getOperateLogConfig() {
        return OperateLogConfig.builder().corePoolSize(1).queueCapacity(10000).build();
    }


}