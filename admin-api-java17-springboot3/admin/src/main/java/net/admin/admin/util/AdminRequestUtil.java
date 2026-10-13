package net.admin.admin.util;

import net.admin.admin.module.system.login.domain.RequestEmployee;
import net.admin.base.common.domain.RequestUser;
import net.admin.base.common.util.RequestUtil;

public final class AdminRequestUtil {


    public static RequestEmployee getRequestUser() {
        return (RequestEmployee) RequestUtil.getRequestUser();
    }

    public static Long getRequestUserId() {
        RequestUser requestUser = getRequestUser();
        return null == requestUser ? null : requestUser.getUserId();
    }


}
