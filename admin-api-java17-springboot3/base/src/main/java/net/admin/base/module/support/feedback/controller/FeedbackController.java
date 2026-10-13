package net.admin.base.module.support.feedback.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.admin.base.common.controller.SupportBaseController;
import net.admin.base.common.domain.PageResult;
import net.admin.base.common.domain.RequestUser;
import net.admin.base.common.domain.ResponseDTO;
import net.admin.base.common.util.RequestUtil;
import net.admin.base.constant.SwaggerTagConst;
import net.admin.base.module.support.feedback.domain.FeedbackAddForm;
import net.admin.base.module.support.feedback.domain.FeedbackQueryForm;
import net.admin.base.module.support.feedback.domain.FeedbackVO;
import net.admin.base.module.support.feedback.service.FeedbackService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = SwaggerTagConst.Support.FEEDBACK)
@RestController
public class FeedbackController extends SupportBaseController {

    @Resource
    private FeedbackService feedbackService;

    @Operation(summary = "意见反馈-分页查询")
    @PostMapping("/feedback/query")
    public ResponseDTO<PageResult<FeedbackVO>> query(@RequestBody @Valid FeedbackQueryForm queryForm) {
        return feedbackService.query(queryForm);
    }

    @Operation(summary = "意见反馈-新增")
    @PostMapping("/feedback/add")
    public ResponseDTO<String> add(@RequestBody @Valid FeedbackAddForm addForm) {
        RequestUser employee = RequestUtil.getRequestUser();
        return feedbackService.add(addForm, employee);
    }
}
