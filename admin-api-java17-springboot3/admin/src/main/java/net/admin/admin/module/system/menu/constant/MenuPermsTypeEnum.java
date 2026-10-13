package net.admin.admin.module.system.menu.constant;


import net.admin.base.common.enumeration.BaseEnum;

public enum MenuPermsTypeEnum implements BaseEnum {
    /**
     * sa-token
     */
    SA_TOKEN(1, "Sa-Token模式"),

    ;

    private final Integer value;

    private final String desc;


    MenuPermsTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String getDesc() {
        return desc;
    }
}
