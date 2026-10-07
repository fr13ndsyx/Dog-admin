package net.dogadmin.base.module.support.loginlog;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.dogadmin.base.common.domain.PageResult;
import net.dogadmin.base.common.domain.ResponseDTO;
import net.dogadmin.base.common.enumeration.UserTypeEnum;
import net.dogadmin.base.common.util.DogPageUtil;
import net.dogadmin.base.module.support.loginlog.domain.LoginLogEntity;
import net.dogadmin.base.module.support.loginlog.domain.LoginLogQueryForm;
import net.dogadmin.base.module.support.loginlog.domain.LoginLogVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class LoginLogService {

    @Resource
    private LoginLogDao loginLogDao;

    public ResponseDTO<PageResult<LoginLogVO>> queryByPage(LoginLogQueryForm queryForm) {
        Page page = DogPageUtil.convert2PageQuery(queryForm);
        List<LoginLogVO> logList = loginLogDao.queryByPage(page, queryForm);
        PageResult<LoginLogVO> pageResult = DogPageUtil.convert2PageResult(page, logList);
        return ResponseDTO.ok(pageResult);
    }

    public void log(LoginLogEntity loginLogEntity) {
        try {
            loginLogDao.insert(loginLogEntity);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }


    public LoginLogVO queryLastByUserId(Long userId, UserTypeEnum userTypeEnum, LoginLogResultEnum loginLogResultEnum) {
        return loginLogDao.queryLastByUserId(userId,userTypeEnum.getValue(), loginLogResultEnum.getValue());
    }

}
