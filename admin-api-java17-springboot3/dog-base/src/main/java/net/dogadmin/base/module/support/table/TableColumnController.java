package net.dogadmin.base.module.support.table;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.dogadmin.base.common.controller.SupportBaseController;
import net.dogadmin.base.common.domain.ResponseDTO;
import net.dogadmin.base.common.util.DogRequestUtil;
import net.dogadmin.base.constant.SwaggerTagConst;
import net.dogadmin.base.module.support.repeatsubmit.annoation.RepeatSubmit;
import net.dogadmin.base.module.support.table.domain.TableColumnUpdateForm;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = SwaggerTagConst.Support.TABLE_COLUMN)
public class TableColumnController extends SupportBaseController {

    @Resource
    private TableColumnService tableColumnService;

    @Operation(summary = "修改表格列")
    @PostMapping("/tableColumn/update")
    @RepeatSubmit
    public ResponseDTO<String> updateTableColumn(@RequestBody @Valid TableColumnUpdateForm updateForm) {
        return tableColumnService.updateTableColumns(DogRequestUtil.getRequestUser(), updateForm);
    }

    @Operation(summary = "恢复默认（删除）")
    @GetMapping("/tableColumn/delete/{tableId}")
    @RepeatSubmit
    public ResponseDTO<String> deleteTableColumn(@PathVariable Integer tableId) {
        return tableColumnService.deleteTableColumn(DogRequestUtil.getRequestUser(), tableId);
    }

    @Operation(summary = "查询表格列")
    @GetMapping("/tableColumn/getColumns/{tableId}")
    public ResponseDTO<String> getColumns(@PathVariable Integer tableId) {
        return ResponseDTO.ok(tableColumnService.getTableColumns(DogRequestUtil.getRequestUser(), tableId));
    }
}
