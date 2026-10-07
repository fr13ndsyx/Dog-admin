package net.dogadmin.base.module.support.helpdoc.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HelpDocUpdateForm extends HelpDocAddForm {

    @Schema(description = "id")
    @NotNull(message = "通知id不能为空")
    private Long helpDocId;

}
