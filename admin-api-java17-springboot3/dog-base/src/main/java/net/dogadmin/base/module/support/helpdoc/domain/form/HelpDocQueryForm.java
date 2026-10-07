package net.dogadmin.base.module.support.helpdoc.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.dogadmin.base.common.domain.PageParam;

import java.time.LocalDate;

@Data
public class HelpDocQueryForm extends PageParam {

    @Schema(description = "分类")
    private Long helpDocCatalogId;

    @Schema(description = "标题")
    private String keywords;

    @Schema(description = "创建-开始时间")
    private LocalDate createTimeBegin;

    @Schema(description = "创建-截止时间")
    private LocalDate createTimeEnd;

}
