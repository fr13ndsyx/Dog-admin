package net.dogadmin.admin.module.system.menu.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MenuAddForm extends MenuBaseForm {

    @Schema(hidden = true)
    private Long createUserId;
}
