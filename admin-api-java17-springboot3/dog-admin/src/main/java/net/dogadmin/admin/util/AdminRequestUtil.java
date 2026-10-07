package net.dogadmin.admin.util;

import net.dogadmin.admin.module.system.login.domain.RequestEmployee;
import net.dogadmin.base.common.domain.RequestUser;
import net.dogadmin.base.common.util.DogRequestUtil;

public final class AdminRequestUtil {


    public static RequestEmployee getRequestUser() {
        return (RequestEmployee) DogRequestUtil.getRequestUser();
    }

    public static Long getRequestUserId() {
        RequestUser requestUser = getRequestUser();
        return null == requestUser ? null : requestUser.getUserId();
    }


}
