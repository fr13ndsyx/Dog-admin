package net.admin.admin.module.system.login.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import net.admin.admin.constant.AdminSwaggerTagConst;
import net.admin.admin.module.system.login.domain.LoginForm;
import net.admin.admin.module.system.login.domain.LoginResultVO;
import net.admin.admin.module.system.login.service.LoginService;
import net.admin.admin.util.AdminRequestUtil;
import net.admin.base.common.annoation.NoNeedLogin;
import net.admin.base.common.constant.RequestHeaderConst;
import net.admin.base.common.domain.ResponseDTO;
import net.admin.base.common.util.RequestUtil;
import net.admin.base.module.support.apiencrypt.service.ApiEncryptService;
import net.admin.base.module.support.captcha.domain.CaptchaVO;
import net.admin.base.module.support.securityprotect.service.Level3ProtectConfigService;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = AdminSwaggerTagConst.System.SYSTEM_LOGIN)
public class LoginController {

    @Resource
    private LoginService loginService;

    @Resource
    private Level3ProtectConfigService level3ProtectConfigService;

    @Resource
    private ApiEncryptService apiEncryptService;

    @NoNeedLogin
    @PostMapping("/login")
    @Operation(summary = "登录")
    public ResponseDTO<LoginResultVO> login(@Valid @RequestBody LoginForm loginForm, HttpServletRequest request) {
        String ip = JakartaServletUtil.getClientIP(request);
        String userAgent = JakartaServletUtil.getHeaderIgnoreCase(request, RequestHeaderConst.USER_AGENT);
        return loginService.login(loginForm, ip, userAgent);
    }

    @GetMapping("/login/getLoginInfo")
    @Operation(summary = "获取登录结果信息")
    public ResponseDTO<LoginResultVO> getLoginInfo() {
        String tokenValue = StpUtil.getTokenValue();
        LoginResultVO loginResult = loginService.getLoginResult(AdminRequestUtil.getRequestUser(), tokenValue);
        loginResult.setToken(tokenValue);
        return ResponseDTO.ok(loginResult);
    }

    @Operation(summary = "退出登录")
    @GetMapping("/login/logout")
    public ResponseDTO<String> logout() {
        return loginService.logout(RequestUtil.getRequestUser());
    }

    @Operation(summary = "获取密码加密公钥")
    @GetMapping("/login/getEncryptPublicKey")
    @NoNeedLogin
    public ResponseDTO<String> getEncryptPublicKey() {
        return ResponseDTO.ok(apiEncryptService.getPublicKey());
    }

    @Operation(summary = "获取验证码")
    @GetMapping("/login/getCaptcha")
    @NoNeedLogin
    public ResponseDTO<CaptchaVO> getCaptcha() {
        return loginService.getCaptcha();
    }

    @NoNeedLogin
    @GetMapping("/login/sendEmailCode/{loginName}")
    @Operation(summary = "获取邮箱登录验证码")
    public ResponseDTO<String> sendEmailCode(@PathVariable String loginName) {
        return loginService.sendEmailCode(loginName);
    }


    @NoNeedLogin
    @GetMapping("/login/getTwoFactorLoginFlag")
    @Operation(summary = "获取双因子登录标识")
    public ResponseDTO<Boolean> getTwoFactorLoginFlag() {
        // 双因子登录
        boolean twoFactorLoginEnabled = level3ProtectConfigService.isTwoFactorLoginEnabled();
        return ResponseDTO.ok(twoFactorLoginEnabled);
    }
}
