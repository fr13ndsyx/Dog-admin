package net.dogadmin.base.module.support.feedback.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.dogadmin.base.common.controller.SupportBaseController;
import net.dogadmin.base.common.domain.PageResult;
import net.dogadmin.base.common.domain.RequestUser;
import net.dogadmin.base.common.domain.ResponseDTO;
import net.dogadmin.base.common.util.DogRequestUtil;
import net.dogadmin.base.constant.SwaggerTagConst;
import net.dogadmin.base.module.support.feedback.domain.FeedbackAddForm;
import net.dogadmin.base.module.support.feedback.domain.FeedbackQueryForm;
import net.dogadmin.base.module.support.feedback.domain.FeedbackVO;
import net.dogadmin.base.module.support.feedback.service.FeedbackService;
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
        RequestUser employee = DogRequestUtil.getRequestUser();
        return feedbackService.add(addForm, employee);
    }
}
