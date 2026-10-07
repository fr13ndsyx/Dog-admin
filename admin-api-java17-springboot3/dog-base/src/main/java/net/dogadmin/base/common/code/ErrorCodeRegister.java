package net.dogadmin.base.common.code;

import static net.dogadmin.base.common.code.ErrorCodeRangeContainer.register;

public class ErrorCodeRegister {

    static {

        // 系统 错误码
        register(SystemErrorCode.class, 10001, 20000);

        // 意外 错误码
        register(UnexpectedErrorCode.class, 20001, 30000);

        // 用户 通用错误码
        register(UserErrorCode.class, 30001, 40000);

    }


    public static int initialize() {
        return ErrorCodeRangeContainer.initialize();
    }

    public static void main(String[] args) {
        ErrorCodeRegister.initialize();
    }

}
