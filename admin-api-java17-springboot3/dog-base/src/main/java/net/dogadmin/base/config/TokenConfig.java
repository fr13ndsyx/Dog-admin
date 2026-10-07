package net.dogadmin.base.config;

import cn.dev33.satoken.config.SaTokenConfig;
import jakarta.annotation.Resource;
import net.dogadmin.base.module.support.securityprotect.service.Level3ProtectConfigService;
import org.springframework.context.annotation.Configuration;


@Configuration
public class TokenConfig {

    @Resource
    private Level3ProtectConfigService level3ProtectConfigService;

    // 此配置会覆盖 dog-base.yaml 中的配置
    @Resource
    public void configSaToken(SaTokenConfig config) {

        config.setActiveTimeout(level3ProtectConfigService.getLoginActiveTimeoutSeconds());
    }


}
