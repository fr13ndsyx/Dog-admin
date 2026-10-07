
package net.dogadmin.base.module.support.codegenerator.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class TableVO {

    @Schema(description = "表名")
    private String tableName;

    @Schema(description = "表备注")
    private String tableComment;

    @Schema(description = "配置时间")
    private LocalDateTime configTime;

}
