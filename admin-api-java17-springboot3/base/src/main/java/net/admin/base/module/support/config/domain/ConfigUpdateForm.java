package net.admin.base.module.support.config.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConfigUpdateForm extends ConfigAddForm {

    @Schema(description = "configId")
    @NotNull(message = "configId不能为空")
    private Long configId;
}
