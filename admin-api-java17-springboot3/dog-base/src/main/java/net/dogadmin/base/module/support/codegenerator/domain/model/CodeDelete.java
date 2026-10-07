package net.dogadmin.base.module.support.codegenerator.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.dogadmin.base.common.swagger.SchemaEnum;
import net.dogadmin.base.common.validator.enumeration.CheckEnum;
import net.dogadmin.base.module.support.codegenerator.constant.CodeDeleteEnum;


@Data
public class CodeDelete {

    @Schema(description = "是否支持删除 ")
    @NotNull(message = "4.删除 是否支持删除 不能为空")
    private Boolean isSupportDelete;

    @Schema(description = "是否为物理删除")
    @NotNull(message = "4.删除 是否为物理删除 不能为空")
    private Boolean isPhysicallyDeleted;

    @Schema(description = "删除类型")
    @NotBlank(message = "4.删除 删除类型 不能为空")
    @SchemaEnum(CodeDeleteEnum.class)
    @CheckEnum(value = CodeDeleteEnum.class, message = "删除 删除类型 枚举值错误")
    private String deleteEnum;


}
