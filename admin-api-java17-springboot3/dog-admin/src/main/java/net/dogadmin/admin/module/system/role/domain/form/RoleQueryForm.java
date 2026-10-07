package net.dogadmin.admin.module.system.role.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.dogadmin.base.common.domain.PageParam;

@Data
public class RoleQueryForm extends PageParam {

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色id")
    private String roleId;
}
