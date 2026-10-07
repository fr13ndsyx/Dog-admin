package net.dogadmin.base.module.support.dict.domain.form;

import net.dogadmin.base.common.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
public class DictQueryForm extends PageParam {

    @Schema(description = "关键字")
    private String keywords;

    @Schema(description = "禁用状态")
    private Boolean disabledFlag;

}
