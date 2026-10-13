package net.admin.admin.module.system.menu.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class MenuTreeVO extends MenuVO{

    @Schema(description = "菜单子集")
    private List<MenuTreeVO> children;
}
