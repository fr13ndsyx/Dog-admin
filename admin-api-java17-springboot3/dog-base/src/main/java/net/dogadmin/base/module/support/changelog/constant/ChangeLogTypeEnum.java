package net.dogadmin.base.module.support.changelog.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dogadmin.base.common.enumeration.BaseEnum;


@AllArgsConstructor
@Getter
public enum ChangeLogTypeEnum implements BaseEnum {

    /**
     * 重大更新
     */
    MAJOR_UPDATE(1, "重大更新"),

    /**
     * 功能更新
     */
    FUNCTION_UPDATE(2, "功能更新"),

    /**
     * Bug修复
     */
    BUG_FIX(3, "Bug修复"),

    ;

    private final Integer value;

    private final String desc;
}
