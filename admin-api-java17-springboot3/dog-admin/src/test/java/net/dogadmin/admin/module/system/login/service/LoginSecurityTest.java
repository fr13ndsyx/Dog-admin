package net.dogadmin.admin.module.system.login.service;

import net.dogadmin.admin.module.system.employee.domain.entity.EmployeeEntity;
import net.dogadmin.admin.module.system.employee.service.EmployeeService;
import net.dogadmin.admin.module.system.login.domain.LoginForm;
import net.dogadmin.admin.module.system.login.manager.LoginManager;
import net.dogadmin.base.common.domain.ResponseDTO;
import net.dogadmin.base.common.enumeration.UserTypeEnum;
import net.dogadmin.base.module.support.apiencrypt.service.ApiEncryptService;
import net.dogadmin.base.module.support.captcha.CaptchaService;
import net.dogadmin.base.module.support.loginlog.LoginLogService;
import net.dogadmin.base.module.support.securityprotect.service.Level3ProtectConfigService;
import net.dogadmin.base.module.support.securityprotect.service.SecurityLoginService;
import net.dogadmin.base.module.support.securityprotect.service.SecurityPasswordService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LoginSecurityTest {

    @Test
    void rejectsRemovedAndMalformedLoginIds() {
        LoginService service = new LoginService();
        assertEquals(1L, service.getEmployeeIdByLoginId("1:1"));
        for (String id : new String[]{"S:legacy:1", "2:1", "1:", "1:0", "1:-1", "1:invalid", "1:1:2", "1:9223372036854775808"}) {
            assertNull(service.getEmployeeIdByLoginId(id));
        }
        assertNull(service.getEmployeeIdByLoginId(null));
    }

    @Test
    void wrongPasswordRecordsFailureWithoutCreatingSession() {
        LoginService service = new LoginService();
        EmployeeService employees = mock(EmployeeService.class);
        CaptchaService captcha = mock(CaptchaService.class);
        ApiEncryptService encrypt = mock(ApiEncryptService.class);
        SecurityLoginService security = mock(SecurityLoginService.class);
        Level3ProtectConfigService level3 = mock(Level3ProtectConfigService.class);
        LoginLogService logs = mock(LoginLogService.class);
        LoginManager manager = mock(LoginManager.class);
        ReflectionTestUtils.setField(service, "employeeService", employees);
        ReflectionTestUtils.setField(service, "captchaService", captcha);
        ReflectionTestUtils.setField(service, "apiEncryptService", encrypt);
        ReflectionTestUtils.setField(service, "securityLoginService", security);
        ReflectionTestUtils.setField(service, "level3ProtectConfigService", level3);
        ReflectionTestUtils.setField(service, "loginLogService", logs);
        ReflectionTestUtils.setField(service, "loginManager", manager);
        EmployeeEntity employee = new EmployeeEntity();
        employee.setEmployeeId(1L);
        employee.setEmployeeUid("test-uid");
        employee.setLoginName("test-account");
        employee.setActualName("test-user");
        employee.setDeletedFlag(false);
        employee.setDisabledFlag(false);
        employee.setLoginPwd(SecurityPasswordService.getEncryptPwd("valid-salted"));
        LoginForm form = new LoginForm();
        form.setLoginName("test-account");
        form.setPassword("encrypted-test");
        form.setLoginDevice(1);
        when(captcha.checkCaptcha(form)).thenReturn(ResponseDTO.ok());
        when(employees.getByLoginName(form.getLoginName())).thenReturn(employee);
        when(encrypt.decrypt(form.getPassword())).thenReturn("invalid-password");
        when(employees.generateSaltPassword("invalid-password", employee.getEmployeeUid())).thenReturn("invalid-salted");
        when(security.checkLogin(1L, UserTypeEnum.ADMIN_EMPLOYEE)).thenReturn(ResponseDTO.ok());
        assertFalse(service.login(form, "127.0.0.1", "test").getOk());
        verify(security).recordLoginFail(eq(1L), eq(UserTypeEnum.ADMIN_EMPLOYEE), eq("test-account"), isNull());
        verify(logs).log(any());
        verifyNoInteractions(manager);

        ResponseDTO locked = ResponseDTO.userErrorParam("locked");
        when(security.checkLogin(1L, UserTypeEnum.ADMIN_EMPLOYEE)).thenReturn(locked);
        clearInvocations(employees, security, logs);
        assertFalse(service.login(form, "127.0.0.1", "test").getOk());
        verify(employees, never()).generateSaltPassword(anyString(), anyString());
        verify(security, never()).recordLoginFail(anyLong(), any(), anyString(), any());
        verifyNoInteractions(manager);

        when(level3.isTwoFactorLoginEnabled()).thenReturn(true);
        clearInvocations(security);
        assertFalse(service.login(form, "127.0.0.1", "test").getOk());
        verifyNoInteractions(security, manager);
    }
}
