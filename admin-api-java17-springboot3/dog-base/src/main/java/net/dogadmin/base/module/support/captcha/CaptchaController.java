package net.dogadmin.base.module.support.captcha;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import net.dogadmin.base.common.controller.SupportBaseController;
import net.dogadmin.base.common.domain.ResponseDTO;
import net.dogadmin.base.constant.SwaggerTagConst;
import net.dogadmin.base.module.support.captcha.domain.CaptchaVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = SwaggerTagConst.Support.CAPTCHA)
@RestController
public class CaptchaController extends SupportBaseController {

    @Resource
    private CaptchaService captchaService;

    @Operation(summary = "获取图形验证码")
    @GetMapping("/captcha")
    public ResponseDTO<CaptchaVO> generateCaptcha() {
        return ResponseDTO.ok(captchaService.generateCaptcha());
    }

}
