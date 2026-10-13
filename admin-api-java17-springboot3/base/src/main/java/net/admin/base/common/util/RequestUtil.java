package net.admin.base.common.util;

import lombok.extern.slf4j.Slf4j;
import net.admin.base.common.domain.RequestUser;

@Slf4j
public class RequestUtil {

    private static final ThreadLocal<RequestUser> REQUEST_THREAD_LOCAL = new ThreadLocal<>();

    public static void setRequestUser(RequestUser requestUser) {
        if(requestUser == null){
            return;
        }
        REQUEST_THREAD_LOCAL.set(requestUser);
    }

    public static RequestUser getRequestUser() {
        return REQUEST_THREAD_LOCAL.get();
    }

    public static Long getRequestUserId() {
        RequestUser requestUser = getRequestUser();
        return null == requestUser ? null : requestUser.getUserId();
    }


    public static void remove() {
        REQUEST_THREAD_LOCAL.remove();
    }


}
