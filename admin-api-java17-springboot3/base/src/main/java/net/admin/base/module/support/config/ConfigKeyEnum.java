package net.admin.base.module.support.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.admin.base.common.enumeration.BaseEnum;

@Getter
@AllArgsConstructor
public enum ConfigKeyEnum implements BaseEnum {


    LEVEL3_PROTECT_CONFIG("level3_protect_config", "三级等保配置"),
    ;

    private final String value;

    private final String desc;
}
