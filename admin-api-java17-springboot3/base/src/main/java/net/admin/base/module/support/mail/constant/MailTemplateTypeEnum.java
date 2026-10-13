package net.admin.base.module.support.mail.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.admin.base.common.enumeration.BaseEnum;


@Getter
@AllArgsConstructor
public enum MailTemplateTypeEnum implements BaseEnum {

    STRING("string", "字符串替代器"),

    FREEMARKER("freemarker", "freemarker模板引擎");

    private String value;

    private String desc;


}