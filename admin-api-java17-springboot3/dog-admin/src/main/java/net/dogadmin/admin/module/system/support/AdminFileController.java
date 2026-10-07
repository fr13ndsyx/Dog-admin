package net.dogadmin.admin.module.system.support;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.dogadmin.base.common.controller.SupportBaseController;
import net.dogadmin.base.common.domain.PageResult;
import net.dogadmin.base.common.domain.ResponseDTO;
import net.dogadmin.base.constant.SwaggerTagConst;
import net.dogadmin.base.module.support.file.domain.form.FileQueryForm;
import net.dogadmin.base.module.support.file.domain.vo.FileVO;
import net.dogadmin.base.module.support.file.service.FileService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = SwaggerTagConst.Support.FILE)
public class AdminFileController extends SupportBaseController {

    @Resource
    private FileService fileService;

    @Operation(summary = "分页查询")
    @PostMapping("/file/queryPage")
    @SaCheckPermission("support:file:query")
    public ResponseDTO<PageResult<FileVO>> queryPage(@RequestBody @Valid FileQueryForm queryForm) {
        return ResponseDTO.ok(fileService.queryPage(queryForm));
    }

}
