package net.admin.base.module.support.operatelog;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import net.admin.base.common.code.UserErrorCode;
import net.admin.base.common.domain.PageResult;
import net.admin.base.common.domain.ResponseDTO;
import net.admin.base.common.util.BeanUtil;
import net.admin.base.common.util.PageUtil;
import net.admin.base.module.support.operatelog.domain.OperateLogEntity;
import net.admin.base.module.support.operatelog.domain.OperateLogQueryForm;
import net.admin.base.module.support.operatelog.domain.OperateLogVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OperateLogService {

    @Resource
    private OperateLogDao operateLogDao;

    public ResponseDTO<PageResult<OperateLogVO>> queryByPage(OperateLogQueryForm queryForm) {
        Page page = PageUtil.convert2PageQuery(queryForm);
        List<OperateLogEntity> logEntityList = operateLogDao.queryByPage(page, queryForm);
        PageResult<OperateLogVO> pageResult = PageUtil.convert2PageResult(page, logEntityList, OperateLogVO.class);
        return ResponseDTO.ok(pageResult);
    }


    /**
     * 查询详情
     * @param operateLogId
     * @return
     */
    public ResponseDTO<OperateLogVO> detail(Long operateLogId) {
        OperateLogEntity operateLogEntity = operateLogDao.selectById(operateLogId);
        if(operateLogEntity == null){
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        OperateLogVO operateLogVO = BeanUtil.copy(operateLogEntity, OperateLogVO.class);
        return ResponseDTO.ok(operateLogVO);
    }
}
