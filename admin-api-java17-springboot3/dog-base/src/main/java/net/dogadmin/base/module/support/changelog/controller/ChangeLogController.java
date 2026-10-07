package net.dogadmin.base.module.support.changelog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.dogadmin.base.common.controller.SupportBaseController;
import net.dogadmin.base.common.domain.PageResult;
import net.dogadmin.base.common.domain.ResponseDTO;
import net.dogadmin.base.constant.SwaggerTagConst;
import net.dogadmin.base.module.support.changelog.domain.form.ChangeLogQueryForm;
import net.dogadmin.base.module.support.changelog.domain.vo.ChangeLogVO;
import net.dogadmin.base.module.support.changelog.service.ChangeLogService;
import org.springframework.web.bind.annotation.*;


@RestController
@Tag(name = SwaggerTagConst.Support.CHANGE_LOG)
public class ChangeLogController extends SupportBaseController {

    @Resource
    private ChangeLogService changeLogService;

    @Operation(summary = "分页查询")
    @PostMapping("/changeLog/queryPage")
    public ResponseDTO<PageResult<ChangeLogVO>> queryPage(@RequestBody @Valid ChangeLogQueryForm queryForm) {
        return ResponseDTO.ok(changeLogService.queryPage(queryForm));
    }


    @Operation(summary = "变更内容详情")
    @GetMapping("/changeLog/getDetail/{changeLogId}")
    public ResponseDTO<ChangeLogVO> getDetail(@PathVariable Long changeLogId) {
        return ResponseDTO.ok(changeLogService.getById(changeLogId));
    }
}